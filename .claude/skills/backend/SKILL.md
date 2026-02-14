---
name: backend
description: 포인트 룰렛 서비스 백엔드 에이전트. Spring Boot 3.x + Kotlin 기반 API 서버의 설정, 구현, 테스트를 담당한다. 백엔드 코드 작성, API 개발, DB 설계, 테스트 작성 요청 시 사용한다.
---

# Backend Agent

포인트 룰렛 서비스 백엔드 (Spring Boot 3.x + Kotlin) 개발 에이전트.

## 기술 스택

| 항목 | 기술 |
|---|---|
| 언어 | Kotlin |
| 프레임워크 | Spring Boot 3.x |
| ORM | Spring Data JPA |
| DB (개발) | H2 (in-memory) |
| DB (운영) | PostgreSQL |
| 마이그레이션 | Flyway |
| 인증 | JWT (Access Token, 24시간) |
| API 문서 | springdoc-openapi (Swagger UI) |
| 테스트 | JUnit 5 + MockK |
| 빌드 | Gradle (Kotlin DSL) |
| 민감 정보 | git-secret |

## 프로젝트 구조

```
apps/backend/
├── build.gradle.kts
├── settings.gradle.kts
├── src/
│   ├── main/
│   │   ├── kotlin/com/example/roulette/
│   │   │   ├── RoulettteApplication.kt
│   │   │   ├── auth/
│   │   │   │   ├── AuthController.kt
│   │   │   │   ├── AuthService.kt
│   │   │   │   ├── JwtProvider.kt
│   │   │   │   ├── JwtAuthenticationFilter.kt
│   │   │   │   ├── SecurityConfig.kt
│   │   │   │   ├── dto/
│   │   │   │   └── entity/
│   │   │   │       └── Member.kt
│   │   │   ├── roulette/
│   │   │   │   ├── RouletteController.kt
│   │   │   │   ├── RouletteService.kt
│   │   │   │   ├── RouletteRepository.kt
│   │   │   │   ├── dto/
│   │   │   │   └── entity/
│   │   │   │       └── RouletteHistory.kt
│   │   │   ├── point/
│   │   │   │   ├── PointController.kt
│   │   │   │   ├── PointService.kt
│   │   │   │   ├── PointRepository.kt
│   │   │   │   ├── dto/
│   │   │   │   └── entity/
│   │   │   │       └── Point.kt
│   │   │   ├── budget/
│   │   │   │   ├── BudgetController.kt
│   │   │   │   ├── BudgetService.kt
│   │   │   │   ├── BudgetRepository.kt
│   │   │   │   ├── dto/
│   │   │   │   └── entity/
│   │   │   │       └── DailyBudget.kt
│   │   │   ├── product/
│   │   │   │   ├── ProductController.kt
│   │   │   │   ├── ProductService.kt
│   │   │   │   ├── ProductRepository.kt
│   │   │   │   ├── dto/
│   │   │   │   └── entity/
│   │   │   │       └── Product.kt
│   │   │   ├── order/
│   │   │   │   ├── OrderController.kt
│   │   │   │   ├── OrderService.kt
│   │   │   │   ├── OrderRepository.kt
│   │   │   │   ├── dto/
│   │   │   │   └── entity/
│   │   │   │       └── Order.kt
│   │   │   └── common/
│   │   │       ├── dto/
│   │   │       │   └── ApiResponse.kt
│   │   │       ├── exception/
│   │   │       │   ├── BusinessException.kt
│   │   │       │   ├── ErrorCode.kt
│   │   │       │   └── GlobalExceptionHandler.kt
│   │   │       └── config/
│   │   │           └── SwaggerConfig.kt
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/
│   └── test/
│       └── kotlin/com/example/roulette/
│           ├── auth/
│           │   └── AuthServiceTest.kt
│           ├── roulette/
│           │   └── RouletteServiceTest.kt
│           ├── point/
│           │   └── PointServiceTest.kt
│           ├── budget/
│           │   └── BudgetServiceTest.kt
│           ├── product/
│           │   └── ProductServiceTest.kt
│           └── order/
│               └── OrderServiceTest.kt
└── .gitattributes (git-secret 설정)
```

## API 응답 형식

모든 API는 `ResponseEntity<ApiResponse<T>>` 형태로 반환한다.

```kotlin
data class ApiResponse<T>(
    val status: String,
    val data: T? = null,
    val errorCode: String? = null,
    val message: String? = null,
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> =
            ApiResponse(status = "SUCCESS", data = data)

        fun error(errorCode: String, message: String): ApiResponse<Nothing> =
            ApiResponse(status = "ERROR", errorCode = errorCode, message = message)
    }
}
```

사용 예시:
```kotlin
return ResponseEntity.ok(ApiResponse.success(product))
return ResponseEntity.status(400).body(ApiResponse.error("ROULETTE_ALREADY_PLAYED", "오늘 이미 룰렛을 돌렸습니다."))
```

## 예외 처리

```kotlin
// 에러 코드 enum
enum class ErrorCode(val status: Int, val message: String) {
    ROULETTE_ALREADY_PLAYED(400, "오늘 이미 룰렛을 돌렸습니다."),
    BUDGET_EXCEEDED(400, "오늘 일일 예산이 소진되었습니다."),
    POINT_NOT_ENOUGH(400, "포인트가 부족합니다."),
    PRODUCT_NOT_FOUND(404, "상품을 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(404, "회원을 찾을 수 없습니다."),
    UNAUTHORIZED(401, "인증이 필요합니다."),
    FORBIDDEN(403, "권한이 없습니다."),
}

// 비즈니스 예외
class BusinessException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)

// 글로벌 핸들러
@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity
            .status(e.errorCode.status)
            .body(ApiResponse.error(e.errorCode.name, e.errorCode.message))
    }
}
```

## 인증 (JWT)

- 로그인: 닉네임 입력 → 존재하면 로그인, 없으면 자동 생성 → JWT 발급
- Access Token만 사용 (만료: 24시간)
- 역할: `USER` / `ADMIN` (Member entity의 role 필드)
- Spring Security 필터에서 JWT 검증
- 어드민 API는 `@PreAuthorize("hasRole('ADMIN')")` 적용

## 환경 설정

```yaml
# application.yml — 공통
spring:
  profiles:
    active: dev

# application-dev.yml
spring:
  datasource:
    url: jdbc:h2:mem:roulette
  h2:
    console:
      enabled: true
  jpa:
    hibernate:
      ddl-auto: validate
springdoc:
  swagger-ui:
    path: /api-docs

# application-prod.yml (git-secret으로 암호화)
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/roulette
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
springdoc:
  swagger-ui:
    enabled: false
jwt:
  secret: ${JWT_SECRET}
```

## git-secret 적용 대상

- `application-prod.yml`
- 기타 민감 설정 파일

## DB 마이그레이션 (Flyway)

파일 위치: `src/main/resources/db/migration/`

네이밍: `V{번호}__{설명}.sql`
```
V1__create_members.sql
V2__create_daily_budgets.sql
V3__create_roulette_histories.sql
V4__create_points.sql
V5__create_products.sql
V6__create_orders.sql
```

## 핵심 엔티티

```
Member        (id, nickname, role, createdAt)
DailyBudget   (id, date, totalBudget, usedBudget)
RouletteHistory (id, memberId, point, playedAt)
Point         (id, memberId, amount, remainingAmount, earnedAt, expiresAt)
Product       (id, name, description, price, stock, isActive)
Order         (id, memberId, productId, usedPoint, status, orderedAt)
```

## Swagger 태그

| 태그 | 설명 |
|---|---|
| 인증 | 로그인 |
| 룰렛 | 룰렛 참여, 참여 여부 확인 |
| 포인트 | 포인트 조회, 잔액, 만료 예정 |
| 예산 | 일일 예산 조회/설정 (어드민) |
| 상품 | 상품 목록/등록/수정 (어드민) |
| 주문 | 상품 구매, 주문 내역, 주문 취소 (어드민) |

## 구현 순서

기능 완성 후 반드시 테스트 코드를 작성하고, 커밋한다.

| 순서 | 기능 | 테스트 |
|---|---|---|
| 1 | 프로젝트 초기 설정 (빌드, 공통 모듈, Swagger, 예외 처리) | 빌드 확인 |
| 2 | 인증 (Member 엔티티, JWT, 로그인 API, Security 설정) | AuthServiceTest |
| 3 | 일일 예산 조회/설정 (어드민 API) | BudgetServiceTest |
| 4 | 룰렛 참여 (1일 1회, 100~1000p, 예산 차감) | RouletteServiceTest |
| 5 | 포인트 조회/관리 (잔액, 유효기간, 만료 예정) | PointServiceTest |
| 6 | 상품 CRUD (어드민 등록/수정, 사용자 목록 조회) | ProductServiceTest |
| 7 | 상품 주문 (포인트 차감, 주문 내역) | OrderServiceTest |
| 8 | 취소 기능 (주문 취소→포인트 환불, 룰렛 취소→포인트 회수) | 취소 관련 테스트 추가 |

## 테스트 규칙

- **Service**: JUnit 5 + MockK (모든 Service 필수)
- **Repository**: @DataJpaTest + H2 (주요 쿼리)
- **API 통합**: @SpringBootTest + MockMvc (주요 엔드포인트)
- 테스트 네이밍: 한국어 백틱

```kotlin
@Test
fun `하루 예산 초과 시 BusinessException을 던진다`() {
    // given
    every { budgetRepository.findByDate(today) } returns DailyBudget(totalBudget = 10000, usedBudget = 10000)

    // when & then
    assertThrows<BusinessException> {
        rouletteService.spin(memberId)
    }.also {
        assertEquals(ErrorCode.BUDGET_EXCEEDED, it.errorCode)
    }
}
```

## 비즈니스 규칙

- 룰렛: 사용자당 1일 1회, 100~1000p 랜덤 지급
- 일일 예산: 하루 지급 총액이 예산 초과 시 참여 불가
- 포인트 유효기간: 획득일 + 30일, 만료 예정 포인트는 7일 이내 만료 건
- 포인트 차감: 만료일 임박한 포인트부터 우선 차감 (FIFO)
- 주문 취소: 어드민만 가능, 차감된 포인트 환불
- 룰렛 취소: 어드민만 가능, 지급된 포인트 회수
