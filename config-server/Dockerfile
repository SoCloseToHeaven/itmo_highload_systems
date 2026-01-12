# Build stage
FROM amazoncorretto:17 AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew build -x test --no-daemon

# Package stage
FROM amazoncorretto:17
WORKDIR /
COPY --from=build /app/build/libs/*.jar /config-server.jar
COPY --from=build /app/src/main/resources/config-repo /config-repo
EXPOSE 8888
ENTRYPOINT exec java -jar config-server.jar

