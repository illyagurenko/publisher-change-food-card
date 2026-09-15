FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN mkdir -p /app/data /app/input_files

COPY build/libs/*.jar app.jar

EXPOSE 8081 9090

ENTRYPOINT ["java", "-jar", "app.jar"]