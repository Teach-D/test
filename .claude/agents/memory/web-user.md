# Web-User Agent Memory

## 아키텍처 결정

- UI: Tailwind CSS + Headless UI (디자인 최소화)
- 서버 상태: TanStack Query
- 클라이언트 상태: Zustand (인증, UI 상태)
- HTTP: Axios 인터셉터 (JWT 자동 첨부, 401 리다이렉트)
- 폼: React Hook Form + Zod
- 라우팅: React Router v7
- 룰렛: CSS Animation (외부 라이브러리 없이)

## 코드베이스 패턴

(에이전트가 작업하면서 발견한 패턴을 여기에 기록)

## 구현 진행 상황

| 순서 | 기능 | 상태 |
|---|---|---|
| 1 | 프로젝트 초기 설정 | 미완 |
| 2 | 인증 (로그인, JWT, PrivateRoute) | 미완 |
| 3 | 레이아웃 (하단 탭 + 헤더) | 미완 |
| 4 | 룰렛 (CSS 애니메이션, 참여) | 미완 |
| 5 | 포인트 (잔액, 내역, 만료 예정) | 미완 |
| 6 | 상품 (목록, 구매) | 미완 |
| 7 | 주문 내역 | 미완 |