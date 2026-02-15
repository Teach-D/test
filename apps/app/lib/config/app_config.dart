/// 앱 설정 상수
class AppConfig {
  AppConfig._();

  /// 현재 환경 (빌드 시 --dart-define=ENV=prod 로 전환)
  static const String env = String.fromEnvironment('ENV', defaultValue: 'dev');

  /// WebView URL
  /// - dev: Android 에뮬레이터 10.0.2.2 (호스트 머신 localhost에 해당)
  /// - prod: 배포된 web-user URL
  static const String _devUrl = 'http://10.0.2.2:5174';
  static const String _prodUrl = String.fromEnvironment(
    'WEB_URL',
    defaultValue: 'https://test-tau-rust-26.vercel.app',
  );

  static String get webUrl => env == 'prod' ? _prodUrl : _devUrl;

  /// 앱 이름
  static const String appName = '포인트 룰렛';

  /// 웹에서 앱을 식별하기 위한 User-Agent 접미사
  static const String userAgentSuffix = 'PointRouletteApp';
}
