FROM gradle:8.5-jdk25-alpine AS builder
WORKDIR /build

COPY build.gradle settings.gradle ./

RUN gradle clean build -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /build/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]