# === 1단계: 빌드 ===
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Gradle Wrapper 복사
COPY gradlew .
COPY gradle gradle
RUN chmod +x gradlew

# 의존성 캐싱을 위해 빌드 설정 파일 먼저 복사
COPY build.gradle.kts settings.gradle.kts gradle.properties ./

# 의존성 다운로드 (소스 변경 시 캐시 활용)
RUN ./gradlew dependencies --no-daemon || true

# 소스 복사 및 빌드
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# === 2단계: 실행 ===
FROM eclipse-temurin:17-jre

WORKDIR /app

# 빌드된 JAR 복사
COPY --from=build /app/build/libs/*.jar app.jar

# Render는 PORT 환경변수를 제공
EXPOSE 8080

# JVM 메모리 최적화 (Render 무료 tier: 512MB)
ENTRYPOINT ["java", "-Xmx256m", "-Xms128m", "-jar", "app.jar"]
