# Build stage
FROM gradle:jdk17-corretto AS build

# Настройка Gradle для оптимизации
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true -Dorg.gradle.caching=true -Xmx2048m"

WORKDIR /app

COPY build.gradle settings.gradle ./

# Скачиваем зависимости (этот слой будет кэшироваться Docker layer cache)
# Если build.gradle не изменился, зависимости не будут скачиваться заново
RUN gradle dependencies --no-daemon || true

# Копируем исходники (этот слой пересобирается только при изменении кода)
COPY src ./src

# Собираем проект
RUN gradle build -x test --no-daemon

# Package stage
FROM amazoncorretto:17
WORKDIR /
COPY --from=build /app/build/libs/*.jar /product-service.jar
EXPOSE 8081
ENTRYPOINT exec java -jar product-service.jar
