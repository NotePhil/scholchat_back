@echo off
echo Starting SchoolChat Backend with H2 profile...
cd business
mvn spring-boot:run -Dspring-boot.run.profiles=h2 -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=h2"
pause