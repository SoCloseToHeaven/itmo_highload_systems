FROM amazoncorretto:17
WORKDIR /
# Копируем готовый JAR файл из контекста сборки
COPY build/libs/*.jar /catalog-service.jar
ENTRYPOINT ["java","-jar","catalog-service.jar"]