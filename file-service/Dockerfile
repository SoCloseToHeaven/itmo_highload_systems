FROM gradle:jdk17-corretto AS build
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true -Dorg.gradle.caching=true -Xmx2048m"

WORKDIR /app
COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon || true
COPY src ./src
RUN gradle build -x test --no-daemon

FROM amazoncorretto:17
WORKDIR /
COPY --from=build /app/build/libs/*.jar /file-service.jar
EXPOSE 8086
ENTRYPOINT exec java -jar file-service.jar
