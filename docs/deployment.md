# 배포 가이드

## 배포 구성

| 서비스 | 플랫폼 | 설명 |
|--------|--------|------|
| 백엔드 API | [Render](https://render.com) | Spring Boot Docker 배포 |
| PostgreSQL | [Neon](https://neon.tech) | 서버리스 PostgreSQL |
| 사용자 웹 | [Vercel](https://vercel.com) | React SPA |
| 관리자 웹 | [Vercel](https://vercel.com) | React SPA |
| 모바일 앱 | APK 빌드 | Flutter WebView |

---

## 1. Neon (PostgreSQL)

### 1.1 프로젝트 생성

1. [neon.tech](https://neon.tech) 가입
2. **New Project** 클릭
3. 설정:
   - Project name: `point-roulette`
   - Database name: `roulette`
   - Region: `Asia Pacific (Singapore)` 또는 가까운 리전
4. **Create Project** 클릭

### 1.2 연결 정보 확인

대시보드에서 연결 정보를 복사합니다:

```
postgresql://<user>:<password>@<host>.neon.tech/roulette?sslmode=require
```

이 값을 Render 환경변수에 사용합니다.

---

## 2. Render (백엔드)

### 2.1 서비스 생성

1. [render.com](https://render.com) 가입
2. **New** → **Web Service** 클릭
3. GitHub 리포지토리 연결
4. 설정:
   - **Name**: `point-roulette-api`
   - **Region**: `Oregon (US West)` 또는 `Singapore`
   - **Root Directory**: `apps/backend`
   - **Runtime**: `Docker`
   - **Instance Type**: `Free`

### 2.2 환경변수 설정

| 변수 | 값 | 설명 |
|------|------|------|
| `SPRING_PROFILES_ACTIVE` | `prod` | 운영 프로필 |
| `DATABASE_URL` | `jdbc:postgresql://<host>.neon.tech/roulette?sslmode=require` | Neon DB URL |
| `DB_USERNAME` | Neon에서 복사 | DB 사용자명 |
| `DB_PASSWORD` | Neon에서 복사 | DB 비밀번호 |
| `JWT_SECRET` | 랜덤 64자 이상 문자열 | JWT 서명 키 |
| `CORS_ALLOWED_ORIGINS` | `https://your-web-user.vercel.app,https://your-web-admin.vercel.app` | CORS 허용 도메인 |

> JWT_SECRET 생성: `openssl rand -base64 64`

### 2.3 배포 확인

배포 완료 후:
- API: `https://point-roulette-api.onrender.com`
- Swagger: `https://point-roulette-api.onrender.com/swagger-ui/index.html`

> Render 무료 tier는 15분 비활동 시 슬립됩니다. 첫 요청 시 ~30초 대기가 발생할 수 있습니다.

---

## 3. Vercel (프론트엔드)

### 3.1 사용자 웹 (web-user)

1. [vercel.com](https://vercel.com) 가입
2. **Add New** → **Project** 클릭
3. GitHub 리포 연결
4. 설정:
   - **Root Directory**: `apps/web-user` (Edit 클릭하여 변경)
   - **Framework Preset**: `Vite`
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
5. **Environment Variables** 추가:
   - `VITE_API_URL` = `https://point-roulette-api.onrender.com`
6. **Deploy** 클릭

### 3.2 관리자 웹 (web-admin)

1. 같은 리포에서 **Add New** → **Project** 클릭
2. 설정:
   - **Root Directory**: `apps/web-admin`
   - **Framework Preset**: `Vite`
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
3. **Environment Variables** 추가:
   - `VITE_API_URL` = `https://point-roulette-api.onrender.com`
4. **Deploy** 클릭

### 3.3 배포 확인

- 사용자 웹: `https://point-roulette-user.vercel.app`
- 관리자 웹: `https://point-roulette-admin.vercel.app`

---

## 4. APK 빌드 (모바일 앱)

### 4.1 디버그 APK (테스트용)

```bash
cd apps/app
flutter build apk --debug
```

출력: `build/app/outputs/flutter-apk/app-debug.apk`

### 4.2 릴리스 APK (배포용)

```bash
cd apps/app

# 앱 아이콘 & 스플래시 생성 (최초 1회)
dart run flutter_launcher_icons:main
dart run flutter_native_splash:create

# 릴리스 APK 빌드 (배포된 web-user URL 지정)
flutter build apk --release \
  --dart-define=ENV=prod \
  --dart-define=WEB_URL=https://point-roulette-user.vercel.app
```

출력: `build/app/outputs/flutter-apk/app-release.apk`

### 4.3 App Bundle (Google Play용)

```bash
flutter build appbundle --release \
  --dart-define=ENV=prod \
  --dart-define=WEB_URL=https://point-roulette-user.vercel.app
```

출력: `build/app/outputs/bundle/release/app-release.aab`

---

## 5. 배포 후 체크리스트

### 5.1 Render CORS 업데이트

Vercel 배포 후 실제 URL이 확정되면, Render 환경변수를 업데이트합니다:

```
CORS_ALLOWED_ORIGINS=https://실제-web-user-url.vercel.app,https://실제-web-admin-url.vercel.app
```

### 5.2 기능 검증

- [ ] 백엔드 Swagger 접속 확인
- [ ] 사용자 웹 로그인 → 룰렛 → 포인트 → 상품 → 주문
- [ ] 관리자 웹 로그인 → 대시보드 → 예산 → 상품 → 주문
- [ ] APK 설치 → WebView 정상 로드 확인

### 5.3 제출물

| 항목 | URL/파일 |
|------|----------|
| 사용자 웹 | `https://your-web-user.vercel.app` |
| 관리자 웹 | `https://your-web-admin.vercel.app` |
| Swagger | `https://your-api.onrender.com/swagger-ui/index.html` |
| APK | `apps/app/build/app/outputs/flutter-apk/app-release.apk` |

---

## 환경변수 요약

### 백엔드 (Render)

| 변수 | 필수 | 설명 |
|------|------|------|
| `SPRING_PROFILES_ACTIVE` | O | `prod` |
| `DATABASE_URL` | O | Neon JDBC URL |
| `DB_USERNAME` | O | DB 사용자명 |
| `DB_PASSWORD` | O | DB 비밀번호 |
| `JWT_SECRET` | O | JWT 서명 키 (64자+) |
| `CORS_ALLOWED_ORIGINS` | O | 허용 도메인 (쉼표 구분) |
| `PORT` | - | Render 자동 제공 |

### 프론트엔드 (Vercel)

| 변수 | 필수 | 설명 |
|------|------|------|
| `VITE_API_URL` | O | Render 백엔드 URL |

### 앱 (빌드 시)

| 변수 | 필수 | 설명 |
|------|------|------|
| `ENV` | O | `prod` (--dart-define) |
| `WEB_URL` | O | Vercel web-user URL (--dart-define) |
