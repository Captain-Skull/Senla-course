FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

FROM tomcat:11.0-jre17-temurin-jammy
WORKDIR /usr/local/tomcat

RUN rm -rf webapps/*

COPY --from=build /app/target/personal-ads-system.war webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]