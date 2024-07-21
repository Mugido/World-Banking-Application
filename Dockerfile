FROM openjdk:17-jdk

WORKDIR /app

COPY target/worldbanking-1.0.0.jar /app/worldbanking.jar

EXPOSE 8080

CMD ["java", "-jar", "worldbanking.jar"]