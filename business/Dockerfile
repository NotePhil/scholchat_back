FROM amazoncorretto:21

WORKDIR /app

COPY target/business-installable.jar business.jar

ENTRYPOINT ["java","-Dspring.profiles.active=prod",  "-jar", "business.jar"]
