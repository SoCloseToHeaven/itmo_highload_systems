# Build stage
FROM amazoncorretto:17 AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew build -x test --no-daemon

# Package stage
FROM amazoncorretto:17
WORKDIR /
COPY --from=build /app/build/libs/*.jar /price-service.jar
EXPOSE 8082
ENTRYPOINT exec java -jar price-service.jar

