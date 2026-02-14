# App Agent Memory

## 아키텍처 결정

- WebView: flutter_inappwebview (쿠키/에러 콜백 풍부)
- 상태 관리: 없음 (StatefulWidget만, WebView 래핑이라 상태 거의 없음)
- JWT: 웹에서 완전 관리 (앱 관여 없음)
- 네트워크: connectivity_plus (실시간 감지)
- 스플래시: flutter_native_splash
- 아이콘: flutter_launcher_icons

## 코드베이스 패턴

(에이전트가 작업하면서 발견한 패턴을 여기에 기록)

## 구현 진행 상황

| 순서 | 기능 | 상태 |
|---|---|---|
| 1 | 프로젝트 초기 설정 | 미완 |
| 2 | WebView 기본 화면 | 미완 |
| 3 | 로딩 인디케이터 | 미완 |
| 4 | 뒤로가기 처리 | 미완 |
| 5 | 네트워크 에러 처리 | 미완 |
| 6 | 스플래시 스크린 | 미완 |
| 7 | 앱 아이콘 & 이름 변경 | 미완 |
