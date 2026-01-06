FROM amazoncorretto:21

WORKDIR /app

COPY target/business-installable.jar business.jar

ENTRYPOINT ["java", "-jar", "business.jar"]
