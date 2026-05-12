# Personal Ads System

REST API системы размещения частных объявлений (Java 17, Spring MVC/Security, Hibernate, Liquibase, PostgreSQL, Docker).

## 1. Возможности

- Регистрация и вход (JWT)
- Профиль пользователя
- Объявления: CRUD, поиск, фильтрация, сортировка
- Комментарии к объявлениям
- Личные чаты и сообщения
- Платежи за продвижение объявлений
- Рейтинг продавцов
- История продаж и покупок
- OpenAPI JSON и Swagger UI

## 2. Технологический стек

- Java 17
- Maven
- Spring Framework 6 (Web MVC, Security, ORM)
- Hibernate (JPA)
- Liquibase
- PostgreSQL
- JWT (`jjwt`)
- MapStruct
- JUnit 5 + Mockito
- Testcontainers (интеграционные тесты)
- Docker / Docker Compose

## 3. Предварительные требования

Установите:

1. JDK 17
2. Maven 3.9+
3. Docker + Docker Compose
4. (Опционально для локального запуска без Docker) PostgreSQL 15+

Проверка:

```bash
java -version
mvn -version
docker --version
docker compose version
```

## 4. Переменные окружения

Приложение использует:

- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION_MS`

### Быстрый старт с `.env`

`.env` уже исключен из git (`.gitignore`), поэтому безопасно хранить локально.

```bash
cp .env.example .env
```

Пример:

```env
JWT_SECRET=YOUR_BASE64_SECRET
JWT_EXPIRATION_MS=3600000
```

Сгенерировать `JWT_SECRET` (base64):

```bash
openssl rand -base64 64 | tr -d '\n'
```

## 5. Запуск приложения (рекомендуемый путь: Docker Compose)

Этот путь поднимает PostgreSQL и приложение в контейнерах.

```bash
docker compose up --build -d
```

Проверить состояние:

```bash
docker compose ps
docker compose logs -f app
```

Остановить:

```bash
docker compose down
```

Остановить и удалить volume БД:

```bash
docker compose down -v
```

### Адреса после запуска

- API: `http://localhost:8081`
- Health: `http://localhost:8081/api/health`
- OpenAPI: `http://localhost:8081/api/docs/openapi.json`
- Swagger UI: `http://localhost:8081/api/docs/swagger-ui.html`

Проверка:

```bash
curl http://localhost:8081/api/health
```

## 6. Локальный запуск без Docker (Tomcat через Maven Cargo)

Проект собирается как `war` и запускается через плагин Cargo (Tomcat 11).

### 6.1 Подготовьте PostgreSQL

Вариант A: локальный PostgreSQL на `localhost:5432` (по умолчанию в `database.properties`).

Вариант B: только БД в Docker:

```bash
docker compose up -d db
```

Тогда для локального запуска приложения укажите порт `5433`:

```bash
export DB_URL=jdbc:postgresql://localhost:5433/pas_db
export DB_USER=pas_user
export DB_PASSWORD=pas_password
export JWT_SECRET="$(openssl rand -base64 64 | tr -d '\n')"
export JWT_EXPIRATION_MS=3600000
```

### 6.2 Запуск приложения

```bash
mvn clean package cargo:run
```

Приложение будет доступно на:

- `http://localhost:8080`

Остановка: `Ctrl+C`.

## 7. Сборка и артефакты

Собрать `war`:

```bash
mvn clean package
```

Артефакт:

- `target/personal-ads-system.war`

## 8. Тестирование

В проекте используется разделение:

- Unit-тесты: Surefire (исключает группу `integration`)
- Integration-тесты: Failsafe (группа `integration`)

### Только unit-тесты

```bash
mvn test
```

### Полный прогон (unit + integration)

```bash
mvn verify
```

### Интеграционные тесты и Testcontainers

Интеграционные тесты всегда поднимают PostgreSQL через Testcontainers и применяют тестовый Liquibase changelog.

```bash
mvn verify
```

Если нужен быстрый запуск только unit-тестов:

```bash
mvn test
```

## 9. Основные API маршруты

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/users/me`
- `PUT /api/users/me`
- `GET /api/ads`
- `POST /api/ads`
- `PUT /api/ads/{adId}`
- `DELETE /api/ads/{adId}`
- `GET /api/ads/{adId}/comments`
- `POST /api/ads/{adId}/comments`
- `GET /api/chats`
- `POST /api/chats/ad/{adId}`
- `GET /api/chats/{chatId}/messages`
- `POST /api/chats/{chatId}/messages`
- `PATCH /api/chats/{chatId}/messages/{messageId}/read`
- `GET /api/payments/my`
- `POST /api/payments`
- `GET /api/sales/my-sales`
- `GET /api/sales/my-purchases`
- `POST /api/sales/ad/{adId}/buy`
- `POST /api/sales/chat/{chatId}/buy`
- `GET /api/docs/openapi.json`
- `GET /api/docs/swagger-ui.html`

## 10. Структура проекта (кратко)

```text
src/main/java/com/senla/pas
  controller/   # REST endpoints
  service/      # бизнес-логика
  dao/          # доступ к БД (JPA/Hibernate)
  entity/       # JPA сущности
  mapper/       # MapStruct DTO <-> Entity
  config/       # Spring, Security, DB, OpenAPI
  exception/    # единая обработка ошибок

src/main/resources
  db/changelog/ # Liquibase миграции
  *.properties  # app/db/security конфиги

src/test/java
  service/      # unit тесты
  integration/  # интеграционные тесты
```

## 11. Частые проблемы и решения

### 11.1 `JWT_SECRET` не задан

Симптом: ошибка старта security/jwt.

Решение: задайте `JWT_SECRET` в `.env` или через `export`.

### 11.2 Порт занят

- Приложение в Docker: `8081`
- Локальный Cargo Tomcat: `8080`
- PostgreSQL Docker: `5433`

Проверьте/освободите порт или измените маппинг в `docker-compose.yaml`.

### 11.3 Интеграционные тесты падают из-за Docker

Запустите verify без Testcontainers и укажите тестовую БД:

```bash
mvn -Dit.use.testcontainers=false \
    -Dtest.db.url=jdbc:postgresql://localhost:5433/pas_db \
    -Dtest.db.username=pas_user \
    -Dtest.db.password=pas_password \
    verify
```
