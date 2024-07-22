FROM openjdk:17-jdk

WORKDIR /app

COPY target/worldbanking-0.0.1.jar /app/worldbanking.jar

EXPOSE 8080

CMD ["java", "-jar", "worldbanking.jar"]