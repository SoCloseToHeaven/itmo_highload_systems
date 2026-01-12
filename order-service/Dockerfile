# Build stage
FROM amazoncorretto:17 AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew build -x test --no-daemon

# Package stage
FROM amazoncorretto:17
WORKDIR /
COPY --from=build /app/build/libs/*.jar /order-service.jar
EXPOSE 8084
ENTRYPOINT exec java -jar order-service.jar

