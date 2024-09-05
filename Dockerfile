FROM openjdk:17-jdk
WORKDIR /app
COPY build/libs/tmarket.jar app.jar
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]