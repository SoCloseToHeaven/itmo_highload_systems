# Build stage
FROM amazoncorretto:17 AS build
WORKDIR /
COPY . .
# Для тестов нужен докер; мы не будем запускать докер в докере
RUN ./gradlew build -x test --no-daemon

# Package stage
FROM amazoncorretto:17
ENV JAR_NAME=service-discovery.jar
WORKDIR /
# Копируем готовый JAR файл из контекста сборки
COPY --from=build build/libs/*.jar /service-discovery.jar
EXPOSE 8080
ENTRYPOINT exec java -jar service-discovery.jar