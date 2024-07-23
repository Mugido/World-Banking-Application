FROM openjdk:17-jdk

WORKDIR /app

COPY target/World-Banking-Application-0.0.1-SNAPSHOT.jar /app/worldbanking.jar

EXPOSE 8080

CMD ["java", "-jar", "worldbanking.jar"]