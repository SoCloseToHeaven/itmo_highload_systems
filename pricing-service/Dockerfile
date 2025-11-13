FROM amazoncorretto:17
WORKDIR /
# Копируем готовый JAR файл из контекста сборки
COPY build/libs/*.jar /pricing-service.jar
ENTRYPOINT ["java","-jar","pricing-service.jar"]