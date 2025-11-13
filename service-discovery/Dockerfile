FROM amazoncorretto:17
WORKDIR /
# Копируем готовый JAR файл из контекста сборки
COPY build/libs/*.jar /service-discovery.jar
ENTRYPOINT ["java","-jar","service-discovery.jar"]