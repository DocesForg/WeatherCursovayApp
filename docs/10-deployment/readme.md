# Развертывание и эксплуатация Bura

Раздел описывает, как в проекте Bura использовались контейнеризация, автоматизированная сборка/проверка и административные функции серверной части.

## Структура раздела

- [Docker](Docker.md) — локальный запуск инфраструктуры через Docker Compose, состав контейнеров и переменные окружения.
- [CI/CD](CD.md) — автоматизация сборки, тестирования и подготовки релиза на базе Gradle и типового pipeline.
- [Администрирование](administr.md) — назначение администраторов, защищенные endpoints, панель администратора и эксплуатационные действия.

## Краткая схема deployment-подхода

Проект состоит из Android-клиента и backend-сервера на Spring Boot. Для backend-части выделена отдельная инфраструктура запуска:

1. PostgreSQL хранит учетные записи, избранные города, результаты тестов радиосигнала и обращения в поддержку.
2. Spring Boot server запускается с профилем `dev` или `prod`, читает настройки из переменных окружения и подключается к PostgreSQL.
3. Docker Compose поднимает базу данных и сервер в одной локальной среде, а Gradle используется для сборки, тестирования и контроля качества.
4. Административные функции доступны только пользователям с ролью `ADMIN`, которая назначается через конфигурацию `APP_ADMIN_EMAILS`.

## Основные команды

```bash
cp .env.example .env
docker compose up --build
./gradlew :server:check
./gradlew :server:bootJar
```

После запуска backend доступен по адресу `http://localhost:8080`, Swagger UI — по адресу `http://localhost:8080/swagger-ui.html`, административная панель — по адресу `http://localhost:8080/admin/panel`.


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



# CI/CD

## Назначение CI/CD в проекте

CI/CD-подход в Bura используется для того, чтобы каждое изменение проходило одинаковые автоматизированные проверки перед выпуском: компиляцию, unit-тесты, проверку покрытия и сборку backend-артефакта. Центральным инструментом автоматизации является Gradle, а серверный модуль вынесен в отдельный project `:server`.

В репозитории нет привязки к конкретной облачной CI-системе, поэтому pipeline описан как переносимый сценарий. Его можно запускать локально, в GitHub Actions, GitLab CI, TeamCity или другой CI/CD-среде.

## Что проверяет pipeline

Типовой pipeline проекта состоит из следующих этапов.

### 1. Checkout

CI-система получает исходный код из репозитория и подготавливает рабочую директорию.

### 2. Подготовка JDK и Gradle

Backend использует Java 21 и Spring Boot 3.5.0. Для ускорения сборки рекомендуется кэшировать Gradle dependencies и wrapper cache.

В Docker-сценарии эта же идея используется через volume `gradle_cache`.

### 3. Сборка и тесты backend

Основная команда проверки серверной части:

```bash
./gradlew :server:check
```

Она запускает:

- компиляцию Java-кода;
- тесты JUnit Platform;
- генерацию JaCoCo отчета;
- проверку минимального покрытия тестами.

В `server/build.gradle.kts` настроено требование JaCoCo: минимальное покрытие — `0.40`, то есть 40%.

### 4. Сборка backend-артефакта

После успешных проверок собирается исполняемый Spring Boot jar:

```bash
./gradlew :server:bootJar
```

Результат появляется в директории:

```text
server/build/libs/
```

Артефакт можно передать на сервер, использовать в Docker image или сохранить как artifact CI-сборки.

### 5. Deployment

Для локального и учебного deployment используется Docker Compose:

```bash
docker compose up --build -d
```

В production-подходе pipeline должен:

1. собрать jar или Docker image;
2. передать секреты через защищенные переменные CI/CD;
3. развернуть приложение с профилем `prod`;
4. выполнить smoke-check доступности API и Swagger/OpenAPI endpoints;
5. сохранить логи сборки и результаты тестов.

## Рекомендуемый pipeline

Ниже приведен универсальный сценарий, который можно перенести в любую CI-систему.

```yaml
stages:
  - validate
  - test
  - package
  - deploy

validate:
  script:
    - ./gradlew :server:compileJava

test:
  script:
    - ./gradlew :server:check
  artifacts:
    paths:
      - server/build/reports/tests/test
      - server/build/reports/jacoco/test/html

package:
  script:
    - ./gradlew :server:bootJar
  artifacts:
    paths:
      - server/build/libs/*.jar

deploy:
  script:
    - docker compose up --build -d
  only:
    - main
```

## Контроль качества

В проекте настроены следующие элементы контроля качества:

- JUnit Platform для запуска backend-тестов;
- Spring Security Test для проверки защищенных endpoints;
- JaCoCo для отчета по покрытию;
- `jacocoTestCoverageVerification` с порогом 40%;
- Gradle task `check`, зависящий от проверки покрытия.

Это означает, что если тесты падают или покрытие становится ниже порога, команда `./gradlew :server:check` завершится ошибкой и CI/CD pipeline должен остановиться.

## Управление секретами

Секреты не должны храниться в репозитории. Для CI/CD их нужно задавать через protected variables/secrets конкретной платформы:

- `POSTGRES_PASSWORD`;
- `SPRING_DATASOURCE_URL`;
- `SPRING_DATASOURCE_USERNAME`;
- `SPRING_DATASOURCE_PASSWORD`;
- `APP_JWT_SECRET`;
- `APP_ADMIN_EMAILS`;
- `APP_SUPPORT_MAILBOX`.

Файл `.env.example` используется только как шаблон для локального запуска.

## Smoke-check после deployment

После развертывания рекомендуется выполнить базовые проверки:

```bash
curl -f http://localhost:8080/v3/api-docs
curl -f http://localhost:8080/swagger-ui.html
```

Для защищенных endpoints дополнительно нужен JWT-токен, полученный через `/api/auth/register` или `/api/auth/login`.

## Rollback

При неуспешном deployment рекомендуется:

1. остановить новый контейнер или процесс backend;
2. вернуть предыдущий jar/Docker image;
3. перезапустить сервис с прежними переменными окружения;
4. проверить логи Spring Boot и PostgreSQL;
5. выполнить smoke-check API.

Для PostgreSQL rollback должен выполняться осторожно: перед миграциями и изменениями схемы необходимо делать backup.


# Администрирование

## Роль администрирования в проекте

Администрирование в Bura реализовано на backend-уровне через Spring Security, JWT-аутентификацию и роль `ADMIN`. Администратор получает доступ к отдельным API endpoints и встроенной web-панели, где можно просматривать статистику, управлять пользователями и отвечать на обращения в поддержку.

## Назначение администратора

Роль администратора назначается по email. Список email задается переменной окружения:

```dotenv
APP_ADMIN_EMAILS=admin@example.com,owner@example.com
```

Механика работы:

1. Пользователь регистрируется или выполняет вход.
2. Backend сравнивает email пользователя со списком `APP_ADMIN_EMAILS`.
3. Если email есть в списке, пользователю назначается роль `ADMIN`.
4. Если email отсутствует, пользователь получает роль `USER`.

Важно: при следующем входе роль может быть пересчитана по текущей конфигурации. Поэтому постоянных администраторов нужно хранить в `APP_ADMIN_EMAILS`.

## Безопасность доступа

В проекте используется Spring Security:

- публично доступны `/api/auth/**`, Swagger UI и OpenAPI JSON;
- остальные endpoints требуют JWT-токен;
- административные endpoints требуют роль `ADMIN`;
- пароли хранятся в виде BCrypt hash;
- JWT secret передается через переменную окружения `APP_JWT_SECRET`.

Для защищенных запросов используется заголовок:

```http
Authorization: Bearer <token>
```

## Административные API endpoints

### Dashboard

```http
GET /api/admin/dashboard
Authorization: Bearer <admin-token>
```

Endpoint возвращает агрегированную статистику:

- количество пользователей;
- количество администраторов;
- количество избранных городов;
- количество тестов радиосигнала;
- количество обращений в поддержку.

### Список аккаунтов

```http
GET /api/admin/accounts
Authorization: Bearer <admin-token>
```

Ответ содержит список аккаунтов с полями:

- `id`;
- `email`;
- `displayName`;
- `role`.

### Изменение роли аккаунта

```http
PATCH /api/admin/accounts/{accountId}/role
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "role": "ADMIN"
}
```

Поддерживаемые роли:

- `USER`;
- `ADMIN`.

Если указать неподдерживаемую роль, backend возвращает ошибку `400 Bad Request`.

## Панель администратора

Встроенная web-панель доступна по адресу:

```text
http://localhost:8080/admin/panel
```

Панель используется для работы с обращениями пользователей в поддержку. Она получает JWT-токен администратора одним из способов:

- из query-параметра `token`;
- из `localStorage` браузера по ключу `bura_admin_token`.

Пример открытия панели с токеном:

```text
http://localhost:8080/admin/panel?token=<admin-token>
```

## Администрирование поддержки

Администратор может просматривать список диалогов поддержки:

```http
GET /api/admin/support/conversations
Authorization: Bearer <admin-token>
```

Открыть конкретный диалог:

```http
GET /api/admin/support/accounts/{accountId}/messages
Authorization: Bearer <admin-token>
```

Ответить пользователю:

```http
POST /api/admin/support/accounts/{accountId}/messages
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "message": "Здравствуйте! Мы проверили ваше обращение."
}
```

При открытии диалога сообщения пользователя помечаются как просмотренные администратором.

## Управление аккаунтами пользователей

Пользовательские endpoints аккаунта доступны владельцу аккаунта или администратору при соблюдении правил доступа:

```http
GET /api/accounts/{accountId}
PATCH /api/accounts/{accountId}/name
PATCH /api/accounts/{accountId}/password
DELETE /api/accounts/{accountId}
```

При удалении аккаунта backend также удаляет связанные данные:

- избранные города;
- тесты радиосигнала;
- сообщения поддержки.

Удаление администратора требует явного подтверждения параметром:

```http
DELETE /api/accounts/{accountId}?allowAdminDelete=true
```

## Эксплуатационные обязанности администратора

Администратор проекта отвечает за:

- хранение актуального списка `APP_ADMIN_EMAILS`;
- контроль безопасного значения `APP_JWT_SECRET`;
- проверку обращений пользователей в support-панели;
- контроль количества пользователей, избранных городов и радиотестов через dashboard;
- регулярное обновление секретов и паролей;
- проверку логов backend при ошибках авторизации, базы данных или поддержки;
- создание backup PostgreSQL перед опасными изменениями.

## Рекомендации по безопасности

- Не использовать development JWT secret в production.
- Не передавать admin JWT token через незащищенные каналы.
- Ограничить доступ к `/admin/panel` на уровне reverse proxy или внутренней сети, если приложение развернуто публично.
- Регулярно пересматривать список администраторов.
- Не назначать роль `ADMIN` временным или тестовым аккаунтам в production.
- Использовать HTTPS перед публичным доступом к backend.
