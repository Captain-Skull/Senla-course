FROM maven:3.9.12-eclipse-temurin-25 AS builder

WORKDIR /app

COPY pom.xml .
COPY hotel-core/pom.xml hotel-core/pom.xml

RUN mvn -B -q -DskipTests dependency:go-offline

COPY . .

RUN mvn clean package

FROM tomcat:11-jdk25

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=builder /app/hotel-core/target/hotel.war \
    /usr/local/tomcat/webapps/hotel.war

EXPOSE 8080