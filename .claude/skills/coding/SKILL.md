---
name: coding
description: 코드 작성 시 품질을 유지하기 위한 규칙 스킬. 코드를 작성하거나 수정할 때 항상 이 규칙을 따른다. 코드 작성, 함수 설계, 커밋, 테스트, 에러 처리, 보안 관련 작업 시 자동으로 적용한다.
---

# Coding Rules

코드 작성 시 항상 이 규칙을 따른다. 모든 코드는 프로젝트를 처음 보는 주니어 개발자도 이해할 수 있도록 가독성 우선으로 작성한다.

## 코드 작성 원칙

- **KISS**: 단순하게 유지한다. 복잡한 구현보다 읽기 쉬운 코드를 우선한다.
- **DRY**: 같은 로직이 3회 이상 반복되면 추출한다.
- **YAGNI**: 지금 필요한 것만 구현한다. 미래 요구사항을 추측하지 않는다.
- **SRP**: 하나의 클래스/함수는 하나의 책임만 갖는다.
- **DI**: 의존성은 주입받는다. 직접 생성하지 않는다.

## 함수/메서드 규칙

| 항목 | 규칙 |
|---|---|
| 길이 | 30줄 이내, 초과 시 분리 검토 |
| 파라미터 | 3개 이내, 초과 시 객체로 묶기 |
| 중첩 깊이 | 2단계 이내, early return 활용 |
| 역할 | 한 가지만 수행 |

## 커밋 규칙

### 커밋 단위
- **기능 단위**로 커밋한다 (하나의 의미 있는 변경 = 하나의 커밋)
- 커밋 크기는 **300줄 이내**, 초과 시 커밋 분리를 검토한다

### 커밋 메시지
Conventional Commits 형식을 따른다:
```
<type>(<scope>): <description>
```

### 커밋 전 체크리스트
커밋 전 반드시 확인한다:
1. 빌드 에러 없는가?
2. 타입 에러 없는가?
3. lint 경고/에러 없는가?
4. 불필요한 디버깅 코드(console.log, println, print 등) 제거했는가?
5. .env나 민감 정보가 포함되지 않았는가?

### 커밋 워크플로우
기능 구현 완료 시 아래 순서로 진행한다:
1. 변경 파일 확인 (`git status`, `git diff`)
2. 관련 파일만 `git add`
3. 커밋 메시지 작성 후 `git commit`

## 테스트 규칙

### 테스트 도구

| 앱 | 프레임워크 | 테스트 도구 |
|---|---|---|
| backend | Kotlin + Spring Boot | JUnit 5 + MockK |
| web-admin / web-user | React | Vitest + React Testing Library |
| app | Flutter | flutter_test + integration_test |

### 테스트 범위

| 앱 | 단위 테스트 | 통합/E2E |
|---|---|---|
| backend | Service 레이어 필수 | 주요 API (MockMvc) |
| web-admin / web-user | 커스텀 훅, 유틸 함수 | 주요 사용자 흐름 |
| app | 비즈니스 로직 (Provider/Bloc) | 주요 화면 흐름 |

### 테스트 작성 시점
- 기능 완성 후 작성한다

### 테스트 네이밍
- 한국어로 테스트 의도를 명확하게 작성한다

```kotlin
// backend
@Test
fun `하루 예산 초과 시 에러를 던진다`() { }

@Test
fun `오늘 이미 참여한 사용자는 참여할 수 없다`() { }
```

```typescript
// web
describe('RouletteWheel', () => {
  it('하루 예산 초과 시 비활성화된다', () => { });
});
```

```dart
// app
test('하루 예산 초과 시 에러를 던진다', () { });
```

### 테스트 파일 위치

```
# backend — src/test 표준 구조
src/main/kotlin/com/example/roulette/RouletteService.kt
src/test/kotlin/com/example/roulette/RouletteServiceTest.kt

# web — 같은 디렉토리
components/roulette-wheel.tsx
components/roulette-wheel.test.tsx

# app — SDK 표준 구조
test/roulette_service_test.dart
integration_test/roulette_flow_test.dart
```

## 에러 처리

### 에러 메시지
- 한국어로 작성한다 (사용자에게 직접 노출)

### 에러 코드
- HTTP 상태 코드 + 커스텀 에러 코드 병행

### 에러 응답 형식 (통일)
```json
{
  "statusCode": 400,
  "errorCode": "ROULETTE_ALREADY_PLAYED",
  "message": "오늘 이미 룰렛을 돌렸습니다."
}
```

### 앱별 에러 처리

**backend (Spring Boot):**
- `@RestControllerAdvice`로 글로벌 예외 처리
- 비즈니스 예외는 RuntimeException 상속 커스텀 예외 사용

**web (React):**
- API 에러는 axios interceptor에서 통합 처리
- 사용자에게 보여줄 에러는 toast/snackbar로 표시

**app (Flutter):**
- Result 패턴 또는 try-catch로 에러 핸들링
- 사용자에게 보여줄 에러는 SnackBar/Dialog로 표시

## 보안 기본 규칙

| 항목 | 규칙 |
|---|---|
| 입력 검증 | backend: Jakarta Validation (@Valid), web: zod/yup |
| SQL 인젝션 | ORM/JPA 사용, raw query 금지 |
| 민감 정보 | .env / application.yml로 관리, 코드에 하드코딩 금지 |
| 비밀번호 | bcrypt 해싱 |
| API 인증 | Spring Security 필터 기반 |
