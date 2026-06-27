# Этап 1: Сборка (Builder)
FROM amazoncorretto:21-alpine AS builder
WORKDIR /app

# Копируем обертку Gradle (теперь пути короткие!)
COPY gradlew .
COPY gradle gradle

# Копируем все файлы .gradle
COPY *.gradle ./

# Копируем папку с исходным кодом
COPY src src

# Даем права на выполнение и собираем проект (без тестов)
RUN chmod +x ./gradlew
RUN ./gradlew bootJar -x test

# Этап 2: Запуск (Runtime)
FROM amazoncorretto:21-alpine
WORKDIR /app

# Копируем готовый JAR из первого этапа
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]