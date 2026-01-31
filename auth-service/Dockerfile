# Build stage
FROM amazoncorretto:17 AS build

ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true -Dorg.gradle.caching=true -Xmx2048m"

WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./
RUN chmod +x ./gradlew

RUN ./gradlew dependencies --no-daemon || true

COPY src ./src

RUN ./gradlew build -x test --no-daemon

# Package stage
FROM amazoncorretto:17
WORKDIR /
COPY --from=build /app/build/libs/*.jar /auth-service.jar
EXPOSE 8085
ENTRYPOINT exec java -jar auth-service.jar
