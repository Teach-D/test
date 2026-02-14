# 포인트 룰렛 서비스

## 프로젝트 개요
사용자가 매일 룰렛을 돌려 랜덤 포인트를 획득하고, 적립된 포인트로 상품을 구매할 수 있는 서비스.

## 주요 기능
- **일일 예산**: 하루 총 지급 포인트 한도 설정
- **1일 1회**: 사용자당 하루 한 번 룰렛 참여
- **랜덤 포인트**: 룰렛 결과에 따라 차등 포인트 지급
- **유효기간**: 포인트에 만료 기한 적용
- **상품 구매**: 적립 포인트로 상품 교환

## 모노레포 구조
```
apps/
  backend/       # API 서버
  web-admin/     # 관리자 웹
  web-user/      # 사용자 웹
  app/           # 모바일 앱
packages/        # 공유 패키지
```

---

## 문서 규칙

- 모든 문서는 **한국어**로 작성한다

### 필수 문서
| 문서 | 위치 | 설명 |
|---|---|---|
| README.md | 루트 + 각 앱 | 루트: 프로젝트 소개·전체 구조, 앱별: 설정·실행 방법 |
| Swagger | `/api-docs` | springdoc-openapi 기반 자동 생성, 개발 환경만 노출 |
| CHANGELOG.md | 루트 | Keep a Changelog 형식, 릴리스 단위 수동 작성 |
| .env.example | 각 앱 | 환경 변수 목록과 설명, Git 추적 대상 |

### README.md 섹션 구조
```
# 프로젝트명
## 소개
## 기술 스택
## 시작하기 (Prerequisites → 설치 → 실행)
## 프로젝트 구조
## 환경 변수
## 스크립트 목록
```

### ADR (Architecture Decision Records)
- 위치: `docs/adr/`
- 기술 선택, 구조 변경 시 "왜 이렇게 했는지" 기록

### 코드 주석
- 한국어로 작성
- backend: 공개 API에 KDoc 작성
- web: exported 함수/클래스에 JSDoc/TSDoc 작성
- 컨트롤러는 Swagger 어노테이션/데코레이터가 문서 역할을 대체

### 설계 문서
- 위치: `docs/`
- ERD, 시퀀스 다이어그램 등은 Mermaid로 작성 (GitHub 렌더링 지원)

---

## 코드 컨벤션

> 모든 코드는 프로젝트를 처음 보는 주니어 개발자도 이해할 수 있도록 가독성 우선으로 작성한다.

### Formatter / Linter

**backend (Kotlin):**
- Formatter: ktlint (Kotlin 공식 코딩 컨벤션)
- Linter: detekt

**web (TypeScript/React):**
- Formatter: Prettier (2칸, 세미콜론, 홑따옴표, 100자, 후행 쉼표 all)
- Linter: ESLint flat config + `@typescript-eslint`
- 핵심 규칙: `no-any`, `explicit-function-return-type`, `no-console: warn`, `unused-imports`

**app (Flutter/Dart):**
- Formatter: dart format
- Linter: flutter_lints

### 네이밍 컨벤션

**공통:**
| 대상 | 규칙 | 예시 |
|---|---|---|
| 클래스 | PascalCase | `RouletteService` |
| 함수/변수 | camelCase | `getRandomPoint()` |
| 상수 | UPPER_SNAKE_CASE | `MAX_DAILY_BUDGET` |
| Boolean | is/has/can 접두사 | `isExpired`, `hasPlayed` |
| DB 테이블 | snake_case (복수형) | `point_histories` |
| DB 컬럼 | snake_case | `created_at` |

**backend (Kotlin):**
| 대상 | 규칙 | 예시 |
|---|---|---|
| 파일 | PascalCase | `RouletteService.kt` |
| Enum | PascalCase + UPPER_SNAKE | `PointStatus.EXPIRED` |

**web (TypeScript):**
| 대상 | 규칙 | 예시 |
|---|---|---|
| 파일/디렉토리 | kebab-case | `roulette-service.ts` |
| 타입/인터페이스 | PascalCase | `RouletteResult` |
| Enum | PascalCase + PascalCase | `PointStatus.Expired` |

**app (Dart):**
| 대상 | 규칙 | 예시 |
|---|---|---|
| 파일 | snake_case | `roulette_service.dart` |
| Enum | camelCase | `PointStatus.expired` |

### 디렉토리 구조
- **기능 기반 (Feature-based)**: 기능별로 관련 파일 응집

**backend (Kotlin + Spring Boot):**
```
apps/backend/src/main/kotlin/com/example/
  roulette/
    RouletteController.kt
    RouletteService.kt
    RouletteRepository.kt
    dto/
    entity/
  point/
    PointController.kt
    PointService.kt
    ...
```

**web (React):**
```
apps/web-user/src/
  features/
    roulette/
      components/
      hooks/
      api/
    point/
      components/
      hooks/
      api/
```

### Import 정렬 순서

**web (TypeScript):**
```
1. Node.js 내장 모듈
2. 외부 패키지
3. 내부 공유 패키지 (@packages)
4. 같은 앱 내 다른 모듈
5. 같은 모듈 내 파일
```

**backend (Kotlin):** IDE 기본 정렬 사용

### 에러 처리
- backend: `@RestControllerAdvice` + 커스텀 예외 (RuntimeException 상속)
- web: axios interceptor 통합 처리
- app: Result 패턴 또는 try-catch
- 에러 메시지는 한국어, 커스텀 에러 코드 병행

### 타입 사용 규칙

**web (TypeScript):**
| 항목 | 규칙 |
|---|---|
| `any` | 사용 금지, `unknown` 사용 |
| 객체 형태 | `interface` 사용 |
| 유니온/유틸리티 | `type` 사용 |
| 매직 넘버 | 상수 또는 Enum으로 추출 |

**backend (Kotlin):**
| 항목 | 규칙 |
|---|---|
| DTO | `data class` + Jakarta Validation |
| Entity | `@Entity` JPA 클래스 |
| 매직 넘버 | const val 또는 enum class로 추출 |

---

## Git 컨벤션

### Commit Message (Conventional Commits)
```
<type>(<scope>): <description>

feat:     새로운 기능
fix:      버그 수정
docs:     문서 변경
style:    코드 포맷팅 (동작 변경 없음)
refactor: 리팩토링
test:     테스트 추가/수정
chore:    빌드, 설정 등 기타 변경
```

### 브랜치 네이밍
- `main` — 프로덕션
- `develop` — 개발 통합
- `feat/<설명>` — 기능 개발
- `fix/<설명>` — 버그 수정
- `chore/<설명>` — 기타 작업
