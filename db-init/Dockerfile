FROM amazoncorretto:21

WORKDIR /app

COPY target/db-init-installable.jar app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
