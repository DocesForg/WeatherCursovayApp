# Bura

**Автор:** Виталий Мальцев  
**Траектория:** Mobile (Android) + Backend (Spring Boot)  
**Дата начала:** 09.01.2026  
**Дата сдачи:** 31.05.2026

## Описание проекта

**Bura** — Android-приложение для просмотра погодных данных, графиков и персональных настроек пользователя. Приложение позволяет выбирать города, хранить избранные локации, просматривать расширенные метеопараметры, обращаться в поддержку и запускать тест радиосигнала между двумя городами.

Клиентская часть построена на Kotlin, Jetpack Compose, Room и Retrofit. Серверная часть реализована на Spring Boot и отвечает за регистрацию, JWT-аутентификацию, роли USER/ADMIN, хранение пользовательских данных, избранных городов, обращений в поддержку, истории радиотестов и статистики.

## Траектория выполнения

- [ ] Десктоп
- [ ] Веб-разработка
- [x] **Мобильная** (Android + Spring Boot backend)
- [ ] Enterprise

## Технологический стек

| Компонент | Технологии |
|-----------|------------|
| Android | Kotlin, Jetpack Compose, Material 3, Navigation Compose |
| Локальное хранение | Room Database, SharedPreferences, файловый кэш прогноза |
| Сетевой клиент | Retrofit, OkHttp, Kotlinx Serialization |
| Backend | Java 21, Spring Boot 3.5.0, Spring Web, Spring Security |
| Данные backend | PostgreSQL 16, Spring Data JPA |
| API | REST, SpringDoc OpenAPI UI |
| Безопасность | JWT Bearer token, BCrypt, Spring Security |
| Внешние сервисы | Open-Meteo Forecast API, Open-Meteo Geocoding API |
| Сборка и качество | Gradle 8.14.x, KSP, JUnit, JaCoCo, SonarQube |
| Контейнеризация | Docker, Docker Compose |

## Требования к окружению

| Требование | Версия |
|------------|--------|
| Java JDK | 21+ |
| Android Studio | актуальная версия с поддержкой compileSdk 36 |
| Android SDK | minSdk 28, targetSdk 36 |
| PostgreSQL | 16+ или контейнер из `docker-compose.yml` |
| Docker / Docker Compose | для локального запуска PostgreSQL и инфраструктуры |
| Gradle | используется wrapper `./gradlew` |

## Установка и запуск

### 1. Клонирование репозитория

```bash
git clone https://github.com/username/Bura.git
cd Bura
```

### 2. Подготовка переменных окружения

```bash
cp .env.example .env
```

Проверьте значения подключения к базе данных и JWT-секрет в `.env` перед запуском backend.

### 3. Запуск инфраструктуры

```bash
docker-compose up -d
```

Команда поднимает PostgreSQL для серверной части проекта.

### 4. Запуск backend

```bash
./gradlew :server:bootRun
```

Backend запускается на `http://localhost:8080`.

Полезные адреса:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Admin panel: `http://localhost:8080/admin/panel`

### 5. Запуск Android-приложения

1. Откройте проект в Android Studio.
2. Выберите конфигурацию модуля `app`.
3. Запустите приложение на эмуляторе или Android-устройстве.

Для CLI-сборки debug APK можно использовать:

```bash
./gradlew :app:assembleDebug
```

## API Endpoints

Базовый URL backend: `http://localhost:8080`.

| Метод | Эндпоинт | Описание | Доступ |
|-------|----------|----------|--------|
| POST | `/api/auth/register` | Регистрация пользователя | Публичный |
| POST | `/api/auth/login` | Вход и получение JWT | Публичный |
| GET | `/api/accounts/{accountId}` | Профиль аккаунта | USER владелец / ADMIN |
| PATCH | `/api/accounts/{accountId}/name` | Изменение имени пользователя | USER владелец / ADMIN |
| PATCH | `/api/accounts/{accountId}/password` | Изменение пароля | USER владелец / ADMIN |
| DELETE | `/api/accounts/{accountId}` | Удаление аккаунта | USER владелец / ADMIN |
| GET | `/api/accounts/{accountId}/favorites` | Список избранных городов | USER владелец / ADMIN |
| POST | `/api/accounts/{accountId}/favorites` | Добавление города в избранное | USER владелец / ADMIN |
| DELETE | `/api/accounts/{accountId}/favorites/{favoriteId}` | Удаление города из избранного | USER владелец / ADMIN |
| GET | `/api/accounts/{accountId}/support/messages` | История поддержки | USER владелец / ADMIN |
| POST | `/api/accounts/{accountId}/support/messages` | Отправка сообщения в поддержку | USER владелец / ADMIN |
| GET | `/api/accounts/{accountId}/radio-tests` | История тестов радиосигнала | USER владелец / ADMIN |
| POST | `/api/accounts/{accountId}/radio-tests` | Расчет радиосигнала между городами | USER владелец / ADMIN |
| GET | `/api/accounts/{accountId}/stats` | Пользовательская статистика | USER владелец / ADMIN |
| GET | `/api/admin/**` | Администрирование пользователей и поддержки | ADMIN |

Полная документация API доступна через [Swagger UI](http://localhost:8080/swagger-ui/index.html), а описание интерфейсов хранится в [`docs/02-architecture/interface.md`](docs/02-architecture/interface.md).

## Структура документации

Вся проектная документация находится в папке [`docs/`](docs/):

| Раздел | Содержание |
|--------|------------|
| [`00-project-charter/`](docs/00-project-charter/) | Паспорт проекта, IDEF0, BUC, SWOT, ROI |
| [`01-requirements/`](docs/01-requirements/) | Use Case, Domain Model, трассировка |
| [`02-architecture/`](docs/02-architecture/) | PCMEF, ADR, интерфейсы |
| [`03-database/`](docs/03-database/) | ER-диаграмма, DDL, ORM |
| [`04-detailed-design/`](docs/04-detailed-design/) | Sequence диаграммы, спецификация методов |
| [`05-implementation/`](docs/05-implementation/) |  Реализация слоёв |
| [`06-testing/`](docs/06-testing/) | Тест-планы, JaCoCo, Postman |
| [`07-refactoring/`](docs/07-refactoring/) | «Запахи кода», Data Mapper, Identity Map |
| [`08-ui/`](docs/08-ui/) | Скриншоты интерфейсов |
| [`09-api/`](docs/09-api/) | OpenAPI, Swagger |
| [`10-deployment/`](docs/10-deployment/) | Docker, CI/CD, администрирование |
| [`11-user-guide/`](docs/11-user-guide/) | Руководство пользователя |
| [`12-final-report/`](docs/12-final-report/) | Пояснительная записка, презентация |

## Архитектура (PCMEF)

Система построена на архитектурном паттерне **PCMEF** (Presentation-Control-Mediator-Entity-Foundation). Android-приложение отвечает за UI, навигацию, состояние экранов, локальный кэш и интеграции с REST/Open-Meteo, а backend реализует защищенные HTTP-контракты, бизнес-операции и постоянное хранение пользовательских данных.

| Слой | Android | Backend | Ответственность |
|------|---------|---------|-----------------|
| Presentation | Compose screens/destinations | Admin HTML panel, Swagger UI | Отображение данных и ввод пользователя |
| Control | ViewModel, navigation destinations | REST controllers, security filter | Обработка пользовательских действий, HTTP-запросов и состояний |
| Mediator | Repositories/use-cases | Services, calculation logic | Бизнес-сценарии и координация источников данных |
| Entity | Domain models, Room entities, DTO | JPA entities, DTO records | Данные предметной области и обменные модели |
| Foundation | Retrofit API, Room DAO, SharedPreferences, Open-Meteo client | Spring Data repositories, PostgreSQL, external HTTP clients | Инфраструктура доступа к данным и внешним сервисам |

Подробное описание архитектуры доступно в [`docs/02-architecture/arc42-overview.md`](docs/02-architecture/arc42-overview.md) и [`docs/02-architecture/PCMEF.md`](docs/02-architecture/PCMEF.md).

### Ключевые ADR

- [ADR-001: Использовать PCMEF как архитектурную декомпозицию](docs/02-architecture/adr/adr-001.md)
- [ADR-002: Использовать нативный Android + Jetpack Compose](docs/02-architecture/adr/adr-002.md)
- [ADR-003: Использовать Spring Boot backend](docs/02-architecture/adr/adr-003.md)
- [ADR-004: Использовать PostgreSQL и Spring Data JPA](docs/02-architecture/adr/adr-004.md)
- [ADR-005: Использовать JWT Bearer auth](docs/02-architecture/adr/adr-005.md)
- [ADR-006: Использовать Open-Meteo как внешний погодный источник](docs/02-architecture/adr/adr-006.md)

## Модульная структура

### Android-приложение

- `auth`, `account` — регистрация, вход, сессия и профиль пользователя.
- `place`, `forecast`, `summary`, `graphs` — выбор локаций, загрузка прогноза, сводки и графики.
- `temperature`, `humidity`, `pressure`, `uvindex`, `precipitation`, `visibility`, `wind`, `gust`, `pop`, `sun`, `condition` — метеорологические доменные модули.
- `settings`, `support`, `radio`, `units` — настройки, поддержка, радиотесты и единицы измерения.
- `platform.remote`, `platform.local` — Retrofit API и Room Database.

### Backend

- `account` — пользователи, роли, регистрация, вход, профиль и удаление аккаунта.
- `security` — JWT, фильтр аутентификации, правила доступа и OpenAPI security scheme.
- `favorite` — избранные города пользователя.
- `support` — обращения пользователей и ответы администратора.
- `signal` — расчет и история тестов радиосигнала.
- `stats` — агрегированная статистика пользователя.
- `admin` — административная панель и admin endpoints.

## Тестирование

Основная проверка backend и покрытия:

```bash
./gradlew :server:test :server:jacocoTestCoverageVerification
```

Полная проверка Gradle-проекта:

```bash
./gradlew check
```

Для Android debug-сборки:

```bash
./gradlew :app:assembleDebug
```

## Полезные ссылки

- [Документация проекта](docs/)
- [Паспорт проекта](docs/00-project-charter/passport.md)
- [Архитектурный обзор arc42](docs/02-architecture/arc42-overview.md)
- [Архитектурные решения ADR](docs/02-architecture/adr/)
- [Описание интерфейсов](docs/02-architecture/interface.md)
- [Swagger UI локально](http://localhost:8080/swagger-ui/index.html)
