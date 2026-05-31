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
