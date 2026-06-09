FROM eclipse-temurin:11-jre

WORKDIR /app

COPY sky-server/target/sky-server-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
