# App Agent Memory

## 프로젝트 정보
- 위치: `C:/test/apps/app/`
- Flutter 프로젝트명: `point_roulette`
- 패키지 ID: `com.example.roulette.point_roulette`
- 앱 이름: "포인트 룰렛"

## Android SDK 설정 (build.gradle.kts)
- `compileSdk = 35`
- `minSdk = 21` (flutter_inappwebview 요구사항)
- `targetSdk = 35`

## 의존성 (pubspec.yaml)
- `flutter_inappwebview: ^6.1.5` — WebView 렌더링
- `connectivity_plus: ^6.1.1` — 네트워크 감지
- `flutter_launcher_icons: ^0.14.3` (dev) — 앱 아이콘
- `flutter_native_splash: ^2.4.4` (dev) — 스플래시 스크린

## 플랫폼별 설정 완료 항목
- Android: INTERNET 권한, usesCleartextTraffic=true, 앱 이름 "포인트 룰렛"
- iOS: CFBundleDisplayName = "포인트 룰렛"

## Windows 환경 주의사항
- `flutter pub get` 시 symlink 관련 경고 발생 (Developer Mode 미활성화) — 패키지 해석은 정상 동작
- 경고 메시지: "Building with plugins requires symlink support. Please enable Developer Mode"
- 실제 빌드/분석에는 영향 없음 (flutter analyze 정상 통과)
