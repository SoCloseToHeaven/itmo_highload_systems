# Build stage
FROM amazoncorretto:17 AS build
WORKDIR /
COPY . .
# Для тестов нужен докер; мы не будем запускать докер в докере
RUN ./gradlew build -x test --no-daemon

# Package stage
FROM amazoncorretto:17
ENV JAR_NAME=tobacco_shop.jar
WORKDIR /
# Копируем готовый JAR файл из контекста сборки
COPY --from=build build/libs/*.jar /tobacco_shop.jar
EXPOSE 8080
ENTRYPOINT exec java -jar tobacco_shop.jar