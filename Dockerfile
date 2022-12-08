FROM openjdk:17-jdk-slim
VOLUME /exchange-aggregation-service
ARG JAR_FILE=target/exchange-aggregation-service-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} exchange-aggregation-service-0.0.1-SNAPSHOT.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/exchange-aggregation-service-0.0.1-SNAPSHOT.jar"]
