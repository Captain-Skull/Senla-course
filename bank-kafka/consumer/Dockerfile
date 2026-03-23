FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml /app/parent-pom.xml
COPY producer/pom.xml /app/producer/pom.xml
COPY consumer/pom.xml /app/consumer/pom.xml
RUN mvn -f /app/parent-pom.xml install -N -q
COPY consumer/src /app/consumer/src
RUN mvn -f /app/consumer/pom.xml package -DskipTests -q

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/consumer/target/consumer-1.0.0.jar app.jar
CMD ["java", "-jar", "app.jar"]