FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/ticketing_spring_sample-0.0.1-SNAPSHOT.jar ticketing_spring_sample.jar
EXPOSE 8443
ENTRYPOINT ["java", "-jar", "ticketing_spring_sample.jar"]