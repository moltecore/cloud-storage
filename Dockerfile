FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY target/*.jar app.jar

ENTRYPOINT ["java", "-Xms128m", "-Xmx256m", "-jar", "app.jar"]