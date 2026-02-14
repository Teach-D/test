# Backend Agent Memory

## 아키텍처 결정

- API 응답: ResponseEntity<ApiResponse<T>> 래퍼 패턴
- 인증: 닉네임 간편 로그인 + JWT (Access Token, 24시간)
- 역할: USER / ADMIN enum
- 예외: ErrorCode enum + BusinessException + @RestControllerAdvice
- DB: 개발 H2, 운영 PostgreSQL
- 마이그레이션: Flyway

## 코드베이스 패턴

(에이전트가 작업하면서 발견한 패턴을 여기에 기록)

## 구현 진행 상황

| 순서 | 기능 | 상태 |
|---|---|---|
| 1 | 프로젝트 초기 설정 | 미완 |
| 2 | 인증 (JWT + 간편 로그인) | 미완 |
| 3 | 일일 예산 조회/설정 | 미완 |
| 4 | 룰렛 참여 | 미완 |
| 5 | 포인트 조회/관리 | 미완 |
| 6 | 상품 CRUD | 미완 |
| 7 | 상품 주문 | 미완 |
| 8 | 취소 기능 | 미완 |
