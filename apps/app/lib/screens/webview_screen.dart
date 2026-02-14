import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_inappwebview/flutter_inappwebview.dart';
import 'package:point_roulette/config/app_config.dart';
import 'package:point_roulette/screens/network_error_screen.dart';
import 'package:point_roulette/widgets/loading_overlay.dart';

/// 메인 WebView 화면
///
/// - web-user 앱을 InAppWebView로 로드
/// - 뒤로가기: 웹 히스토리 → 앱 종료 확인
/// - 로딩 중: 상단 선형 프로그레스 바 + 오버레이
/// - 네트워크 오류: 에러 화면 + 재시도
class WebViewScreen extends StatefulWidget {
  const WebViewScreen({super.key});

  @override
  State<WebViewScreen> createState() => _WebViewScreenState();
}

class _WebViewScreenState extends State<WebViewScreen> {
  /// WebView 컨트롤러
  InAppWebViewController? _webViewController;

  /// 로딩 상태 (페이지 로드 중 여부)
  bool _isLoading = true;

  /// 로딩 진행률 (0.0 ~ 1.0)
  double _loadingProgress = 0;

  /// 네트워크 오류 발생 여부
  bool _hasError = false;

  /// InAppWebView 초기 설정
  final InAppWebViewSettings _webViewSettings = InAppWebViewSettings(
    // JavaScript 활성화
    javaScriptEnabled: true,
    // DOM Storage (localStorage, sessionStorage) 허용
    domStorageEnabled: true,
    // 웹에서 앱 감지를 위한 User-Agent 설정
    userAgent: 'Mozilla/5.0 (Linux; Android) AppleWebKit/537.36 '
        '(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 '
        '${AppConfig.userAgentSuffix}',
    // HTTP 트래픽 허용 (개발 환경 로컬 서버용)
    clearCache: false,
    // iOS: 인라인 미디어 재생 허용
    allowsInlineMediaPlayback: true,
  );

  @override
  void initState() {
    super.initState();
    _checkInitialConnectivity();
  }

  /// 앱 시작 시 네트워크 연결 상태 확인
  Future<void> _checkInitialConnectivity() async {
    final result = await Connectivity().checkConnectivity();
    final isOffline = result.contains(ConnectivityResult.none);

    if (isOffline && mounted) {
      setState(() => _hasError = true);
    }
  }

  /// 재시도: 네트워크 확인 후 WebView 리로드
  Future<void> _retry() async {
    final result = await Connectivity().checkConnectivity();
    final isOffline = result.contains(ConnectivityResult.none);

    if (isOffline) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('네트워크에 연결되지 않았습니다.')),
        );
      }
      return;
    }

    setState(() {
      _hasError = false;
      _isLoading = true;
    });

    await _webViewController?.reload();
  }

  /// Android 뒤로가기 처리
  ///
  /// 1. WebView 히스토리 있으면 → 웹 뒤로가기
  /// 2. 히스토리 없으면 → 앱 종료 확인 다이얼로그
  Future<bool> _handleBackButton() async {
    if (_webViewController == null) return true;

    final canGoBack = await _webViewController!.canGoBack();
    if (canGoBack) {
      await _webViewController!.goBack();
      return false; // 뒤로가기 처리 완료, 앱 종료 안 함
    }

    // 히스토리 없으면 종료 확인 다이얼로그 표시
    if (mounted) {
      final shouldExit = await _showExitDialog();
      return shouldExit;
    }

    return false;
  }

  /// 앱 종료 확인 다이얼로그
  Future<bool> _showExitDialog() async {
    final result = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('앱 종료'),
        content: const Text('앱을 종료하시겠습니까?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('취소'),
          ),
          TextButton(
            onPressed: () => Navigator.of(context).pop(true),
            child: const Text('종료'),
          ),
        ],
      ),
    );

    return result ?? false;
  }

  @override
  Widget build(BuildContext context) {
    // 네트워크 오류 시 에러 화면 표시
    if (_hasError) {
      return NetworkErrorScreen(onRetry: _retry);
    }

    return PopScope(
      // false: 뒤로가기를 직접 처리 (onPopInvokedWithResult에서 제어)
      canPop: false,
      onPopInvokedWithResult: (didPop, result) async {
        if (didPop) return;

        final shouldExit = await _handleBackButton();
        if (shouldExit && mounted) {
          // 앱 종료
          SystemNavigator.pop();
        }
      },
      child: Scaffold(
        body: SafeArea(
          child: Stack(
            children: [
              // WebView 본체
              InAppWebView(
                initialUrlRequest: URLRequest(
                  url: WebUri(AppConfig.webUrl),
                ),
                initialSettings: _webViewSettings,
                onWebViewCreated: (controller) {
                  _webViewController = controller;
                },
                onLoadStart: (controller, url) {
                  setState(() {
                    _isLoading = true;
                    _hasError = false;
                  });
                },
                onLoadStop: (controller, url) {
                  setState(() => _isLoading = false);
                },
                onProgressChanged: (controller, progress) {
                  setState(() => _loadingProgress = progress / 100);
                },
                onReceivedError: (controller, request, error) {
                  // 메인 프레임 요청 오류만 에러 화면으로 처리
                  if (request.isForMainFrame ?? false) {
                    setState(() {
                      _isLoading = false;
                      _hasError = true;
                    });
                  }
                },
              ),

              // 상단 선형 프로그레스 바 (로딩 중에만 표시)
              if (_isLoading)
                Positioned(
                  top: 0,
                  left: 0,
                  right: 0,
                  child: LinearProgressIndicator(
                    value: _loadingProgress > 0 ? _loadingProgress : null,
                    minHeight: 3,
                  ),
                ),

              // 반투명 로딩 오버레이 (초기 로딩 중에만 표시)
              if (_isLoading && _loadingProgress < 0.1)
                const Positioned.fill(
                  child: LoadingOverlay(isLoading: true),
                ),
            ],
          ),
        ),
      ),
    );
  }
}
