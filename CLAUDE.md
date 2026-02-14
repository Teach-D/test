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
| Swagger | `/api-docs` | `@nestjs/swagger` 데코레이터 기반 자동 생성, 개발 환경만 노출 |
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
- 공개 API(exported 함수/클래스)에만 JSDoc/TSDoc 작성
- 컨트롤러는 Swagger 데코레이터가 문서 역할을 대체

### 설계 문서
- 위치: `docs/`
- ERD, 시퀀스 다이어그램 등은 Mermaid로 작성 (GitHub 렌더링 지원)

---

## 코드 컨벤션

> 모든 코드는 프로젝트를 처음 보는 주니어 개발자도 이해할 수 있도록 가독성 우선으로 작성한다.

### Formatter (Prettier)
| 항목 | 값 |
|---|---|
| 들여쓰기 | 2칸 (spaces) |
| 세미콜론 | 사용 |
| 따옴표 | 홑따옴표 |
| 줄 길이 | 100자 |
| 후행 쉼표 | all |

### Linter (ESLint)
- ESLint flat config + `@typescript-eslint`
- 핵심 규칙:
  - `no-any`: any 사용 금지 → unknown 사용
  - `explicit-function-return-type`: 공개 함수 반환 타입 명시
  - `no-console`: warn (logger 사용 유도)
  - `unused-imports`: 자동 제거

### 네이밍 컨벤션
| 대상 | 규칙 | 예시 |
|---|---|---|
| 파일/디렉토리 | kebab-case | `roulette-service.ts` |
| 클래스 | PascalCase | `RouletteService` |
| 함수/변수 | camelCase | `getRandomPoint()` |
| 상수 | UPPER_SNAKE_CASE | `MAX_DAILY_BUDGET` |
| 타입/인터페이스 | PascalCase | `RouletteResult` |
| Enum | PascalCase + PascalCase | `PointStatus.Expired` |
| Boolean | is/has/can 접두사 | `isExpired`, `hasPlayed` |
| DB 테이블 | snake_case (복수형) | `point_histories` |
| DB 컬럼 | snake_case | `created_at` |

### 디렉토리 구조
- **기능 기반 (Feature-based)**: 기능별로 관련 파일 응집
```
apps/backend/src/
  roulette/
    roulette.controller.ts
    roulette.service.ts
    roulette.repository.ts
    roulette.module.ts
    dto/
    entities/
  point/
    point.controller.ts
    point.service.ts
    ...
```

### Import 정렬 순서
```
1. Node.js 내장 모듈
2. 외부 패키지
3. 내부 공유 패키지 (@packages)
4. 같은 앱 내 다른 모듈
5. 같은 모듈 내 파일
```

### 에러 처리
- NestJS 내장 HttpException 기반
- 비즈니스 예외는 HttpException 상속 커스텀 예외 사용
- 글로벌 예외 필터로 통합 처리

### 타입 사용 규칙
| 항목 | 규칙 |
|---|---|
| `any` | 사용 금지, `unknown` 사용 |
| 객체 형태 | `interface` 사용 |
| 유니온/유틸리티 | `type` 사용 |
| DTO | `class` + `class-validator` |
| 매직 넘버 | 상수 또는 Enum으로 추출 |

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
