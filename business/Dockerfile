FROM amazoncorretto:21

WORKDIR /app

COPY target/business-1.0-SNAPSHOT.jar business.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "business.jar"]
