/// 앱 설정 상수
class AppConfig {
  AppConfig._();

  /// WebView URL (개발 환경)
  /// Android 에뮬레이터: 10.0.2.2 (호스트 머신 localhost에 해당)
  /// iOS 시뮬레이터: localhost
  static const String webUrl = 'http://10.0.2.2:5174';

  /// 앱 이름
  static const String appName = '포인트 룰렛';

  /// 웹에서 앱을 식별하기 위한 User-Agent 접미사
  static const String userAgentSuffix = 'PointRouletteApp';
}
