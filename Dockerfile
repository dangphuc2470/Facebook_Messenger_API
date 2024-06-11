FROM ubuntu:latest AS build

RUN apt-get update
RUN apt-get install openjdk-17-jdk -y
COPY . .

RUN chmod +x ./mvnw
RUN ./mvnw package -DskipTests

FROM openjdk:17-jdk-slim

EXPOSE 8080

COPY --from=build /target/Facebook_demo.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]