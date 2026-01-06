# ---------- build stage ----------
FROM gradle:8.10-jdk21 AS build
WORKDIR /src

# 캐시 최적화: 래퍼/스크립트 먼저
COPY gradle/ ./gradle/
COPY gradlew settings.gradle build.gradle ./
RUN chmod +x gradlew

# 의존성 다운로드만 먼저(캐시)
RUN ./gradlew --no-daemon dependencies || true

# 실제 소스
COPY . .

# 테스트 스킵은 필요시 바꾸세요: -x test 제거하면 테스트 수행
#RUN ./gradlew clean bootJar -x test --no-daemon

# ---------- run stage ----------
FROM eclipse-temurin:21-jre
ENV TZ=Asia/Seoul \
    JAVA_TOOL_OPTIONS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"
WORKDIR /app
COPY --from=build /src/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
