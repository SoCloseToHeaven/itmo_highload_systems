# Build stage
FROM amazoncorretto:17 AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew build -x test --no-daemon

# Package stage
FROM amazoncorretto:17
WORKDIR /
COPY --from=build /app/build/libs/*.jar /product-service.jar
EXPOSE 8081
ENTRYPOINT exec java -jar product-service.jar

