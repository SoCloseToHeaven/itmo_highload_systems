FROM amazoncorretto:17
WORKDIR /
# Копируем готовый JAR файл из контекста сборки
COPY build/libs/*.jar /api-gateway.jar
ENTRYPOINT ["java","-jar","api-gateway.jar"]