FROM amazoncorretto:21

WORKDIR /app

COPY target/db-init-1.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
