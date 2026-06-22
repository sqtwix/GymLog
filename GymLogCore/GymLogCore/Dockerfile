# Этап 1: Сборка (Builder)
FROM amazoncorretto:25-alpine AS builder
WORKDIR /app

# Копируем обертку Gradle из вложенной папки
COPY GymLogCore/GymLogCore/gradlew .
COPY GymLogCore/GymLogCore/gradle gradle

# Копируем все файлы .gradle (build.gradle и settings.gradle, если он есть)
COPY GymLogCore/GymLogCore/*.gradle ./

# Копируем папку с исходным кодом
COPY GymLogCore/GymLogCore/src src

# Даем права на выполнение и собираем проект (без тестов)
RUN chmod +x ./gradlew
RUN ./gradlew bootJar -x test

# Этап 2: Запуск (Runtime)
FROM amazoncorretto:25-alpine
WORKDIR /app

# Копируем готовый JAR из первого этапа
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]