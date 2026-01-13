# Build stage
FROM amazoncorretto:17 AS build

# Настройка Gradle для оптимизации
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true -Dorg.gradle.caching=true -Xmx2048m"

WORKDIR /app

# Копируем только файлы Gradle для кэширования зависимостей
# Этот слой будет кэшироваться, если build.gradle не изменился
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./
RUN chmod +x ./gradlew

# Скачиваем зависимости (этот слой будет кэшироваться Docker layer cache)
RUN ./gradlew dependencies --no-daemon || true

# Копируем исходники (этот слой пересобирается только при изменении кода)
COPY src ./src

# Собираем проект
RUN ./gradlew build -x test --no-daemon

# Package stage
FROM amazoncorretto:17
WORKDIR /
COPY --from=build /app/build/libs/*.jar /discount-service.jar
EXPOSE 8083
ENTRYPOINT exec java -jar discount-service.jar
