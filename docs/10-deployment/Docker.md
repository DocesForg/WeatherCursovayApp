# Docker

## Назначение Docker в проекте

Docker используется для воспроизводимого локального запуска backend-инфраструктуры Bura. Вместо ручной установки PostgreSQL и настройки Java-окружения проект поднимается через `docker-compose.yml`, где описаны база данных, Spring Boot server, тома, переменные окружения и порядок старта сервисов.

Такой подход позволяет:

- быстро развернуть backend на новом рабочем месте;
- запускать сервер с теми же переменными окружения, которые используются в deployment-сценариях;
- отделить состояние PostgreSQL в persistent volume;
- не хранить секреты в исходном коде, а передавать их через `.env`;
- проверять интеграцию сервера с реальной PostgreSQL, а не только с in-memory окружением.

## Состав Docker Compose

В проекте используется файл `docker-compose.yml` в корне репозитория. Он поднимает два основных сервиса.

### `postgres`

Сервис базы данных использует образ `postgres:16-alpine`.

Основные настройки:

- имя контейнера: `bura-postgres`;
- порт: `5432:5432`;
- база, пользователь и пароль берутся из `.env`;
- данные сохраняются в volume `postgres_data`;
- healthcheck выполняет `pg_isready`, чтобы сервер стартовал только после готовности базы.

PostgreSQL хранит серверные данные приложения:

- учетные записи пользователей;
- избранные города;
- историю тестов радиосигнала;
- сообщения службы поддержки;
- данные, используемые административной панелью.

### `server`

Сервис backend использует образ `gradle:8.14.3-jdk21`. Это позволяет запускать Spring Boot приложение без отдельной установки Gradle и JDK на хосте.

Основные настройки:

- имя контейнера: `bura-server`;
- рабочая директория: `/workspace`;
- исходный код монтируется в контейнер как volume;
- Gradle cache вынесен в отдельный volume `gradle_cache`;
- команда запуска: `./gradlew :server:bootRun --no-daemon`;
- порт backend: `8080:8080`;
- запуск зависит от успешного healthcheck PostgreSQL.

## Переменные окружения

Перед запуском создается файл `.env` на основе `.env.example`:

```bash
cp .env.example .env
```

Основные группы переменных:

```dotenv
POSTGRES_DB=bura
POSTGRES_USER=bura
POSTGRES_PASSWORD=bura

SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080

SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/bura
SPRING_DATASOURCE_USERNAME=bura
SPRING_DATASOURCE_PASSWORD=bura

APP_JWT_SECRET=change-me-to-at-least-32-characters
APP_JWT_EXPIRATION_SECONDS=86400
APP_SUPPORT_MAILBOX=support@bura.app
APP_ADMIN_EMAILS=admin@example.com
```

Для production-окружения обязательно заменить значения по умолчанию:

- `POSTGRES_PASSWORD` — на надежный пароль базы данных;
- `APP_JWT_SECRET` — на случайный секрет длиной не менее 32 символов;
- `APP_ADMIN_EMAILS` — на реальные email администраторов;
- `SPRING_PROFILES_ACTIVE` — на `prod`.

## Профили Spring Boot

В проекте используются профили `dev` и `prod`.

### `dev`

Профиль разработки удобен для локального запуска:

- база по умолчанию: `jdbc:postgresql://localhost:5432/bura`;
- `ddl-auto: update`, чтобы Hibernate мог обновлять схему во время разработки;
- включено форматирование SQL;
- задан fallback JWT secret для разработки.

При запуске через Docker Compose datasource переопределяется переменной:

```dotenv
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/bura
```

### `prod`

Профиль production рассчитан на более строгий режим:

- datasource, username, password и JWT secret должны приходить из окружения;
- `ddl-auto: validate`, чтобы приложение проверяло схему, но не меняло ее автоматически;
- SQL форматирование отключено;
- список администраторов и почтовый ящик поддержки задаются через переменные окружения.

## Запуск

1. Создать `.env`:

   ```bash
   cp .env.example .env
   ```

2. При необходимости изменить секреты и учетные данные.

3. Запустить контейнеры:

   ```bash
   docker compose up --build
   ```

4. Проверить доступность backend:

   ```text
   http://localhost:8080
   ```

5. Открыть Swagger UI:

   ```text
   http://localhost:8080/swagger-ui.html
   ```

6. Открыть административную панель:

   ```text
   http://localhost:8080/admin/panel
   ```

## Остановка и очистка

Остановить сервисы без удаления данных PostgreSQL:

```bash
docker compose down
```

Остановить сервисы и удалить volume с базой данных:

```bash
docker compose down -v
```

Команду с `-v` следует использовать только для тестовой среды, потому что она удаляет данные PostgreSQL.

## Эксплуатационные рекомендации

- Не добавлять `.env` в Git.
- Для production использовать `SPRING_PROFILES_ACTIVE=prod`.
- Регулярно делать backup volume `postgres_data` или самой PostgreSQL базы.
- Не публиковать порт `5432` наружу без необходимости.
- Ограничить доступ к порту `8080` reverse proxy, firewall или внутренней сетью.
- Перед обновлением backend выполнять `./gradlew :server:check` и `./gradlew :server:bootJar`.
