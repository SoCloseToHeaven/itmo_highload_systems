FROM amazoncorretto:17
WORKDIR /
# Копируем готовый JAR файл из контекста сборки
COPY build/libs/*.jar /config-server.jar
ENTRYPOINT ["java","-jar","config-server.jar"]