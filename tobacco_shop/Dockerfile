FROM amazoncorretto:17
WORKDIR /
# Копируем готовый JAR файл из контекста сборки
COPY build/libs/*.jar /tobacco_shop.jar
ENTRYPOINT ["java","-jar","tobacco_shop.jar"]