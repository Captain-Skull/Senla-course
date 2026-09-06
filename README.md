# Senla Java Course

Coursework and projects from the [Senla](https://senla.ru/) Java backend development course. It moved from core Java syntax through multithreading, servlets, and Kafka, and ended with two full backend web applications built on Spring, Hibernate, and PostgreSQL.

## Featured projects

### [Hotel](./Hotel): hotel booking system

A hotel management REST API. It started as a plain in-memory Java program and grew into a layered Spring MVC service backed by a real database, with security added later.

- **Stack:** Java 25, Spring 6 (MVC, ORM, Security), Hibernate 6, PostgreSQL, Liquibase, JWT auth, Log4j2
- **Features:** hotels, rooms, guests and services management, JWT-based authentication, role-based access control, global exception handling
- **Testing:** JUnit 5, Mockito, Spring MockMvc, Spring Security Test
- Packaged as a WAR (`hotel-core`), deployable to a servlet container

### [PersonalAdsSystem](./PersonalAdsSystem): classifieds platform

A REST API for a classifieds platform, roughly comparable to a small OLX or Craigslist backend. This was the course's final project.

- **Stack:** Java 17, Spring 6 (Web MVC, Security, ORM), Hibernate, PostgreSQL, Liquibase, HikariCP, JWT, MapStruct, Lombok, Docker/Docker Compose
- **Features:** JWT auth, user profiles, ad CRUD with search/filtering/sorting, comments, private chat/messaging, promoted-ad payments, seller ratings, sale history, OpenAPI/Swagger docs
- **Testing:** JUnit 5, Mockito, Testcontainers, AssertJ, unit and integration test suites
- Runs via `docker compose up`; see [PersonalAdsSystem/README.md](./PersonalAdsSystem/README.md) for full setup instructions (in Russian)

## Other coursework exercises

Smaller, self-contained exercises from earlier in the course, kept for reference:

| Folder | Topic |
|---|---|
| `task-2` | Java basics (OOP fundamentals) |
| `task-3` | Interfaces, polymorphism, assembly-line simulation |
| `task-4` | File/console I/O exercise |
| `task-9` | Multithreading (thread states, producer-consumer) |
| `task-10`, `task-11` | Later course exercises |
| `hello-servlet` | Minimal Java Servlet ("Hello World") |
| `bank-kafka` | Producer/consumer services exchanging bank transfer events via Apache Kafka, with Spring, JPA and Liquibase |

## Tech stack overview

Java, Spring (MVC, Security, ORM), Hibernate/JPA, PostgreSQL, Liquibase, Apache Kafka, JWT, Docker, Maven, JUnit 5, Mockito, and Testcontainers all show up somewhere in this repository.

## Repository structure

```
Senla/
├── Hotel/               # Hotel booking system (Spring MVC + Hibernate + PostgreSQL)
├── PersonalAdsSystem/   # Classifieds platform, the final project
├── bank-kafka/          # Kafka producer/consumer exercise
├── hello-servlet/       # Minimal servlet exercise
├── task-2 .. task-11/   # Early coursework exercises
```

## Getting started

Each project builds and runs on its own with Maven.

- **Hotel:** `cd Hotel && mvn clean package`, then deploy the resulting `hotel-core/target/hotel.war` to a servlet container such as Tomcat, with PostgreSQL configured using the scripts in `Hotel/scripts`.
- **PersonalAdsSystem:** see [PersonalAdsSystem/README.md](./PersonalAdsSystem/README.md) for full setup instructions, including a one-command Docker Compose setup.

## Author

**George Mkrtchyan** - [goshamkrtchian@gmail.com](mailto:goshamkrtchian@gmail.com)
