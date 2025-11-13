FROM amazoncorretto:17
WORKDIR /
# Копируем готовый JAR файл из контекста сборки
COPY build/libs/*.jar /order-service.jar
ENTRYPOINT ["java","-jar","order-service.jar"]