# PCMEF-слои в проекте Bura

## 1. Назначение документа

Документ описывает, как архитектурная модель **PCMEF** применяется в Bura. Проект состоит из Android-клиента и Spring Boot backend, поэтому слои представлены в обеих частях системы.

**PCMEF**:

- **P — Presentation**: отображение и пользовательский ввод.
- **C — Control**: маршрутизация действий, прием запросов, управление состоянием сценария.
- **M — Mediator**: бизнес-логика и координация нескольких источников данных.
- **E — Entity**: доменные сущности, DTO, value objects.
- **F — Foundation**: инфраструктура, БД, API-клиенты, файловое хранилище, конфигурация.

---

## 2. Общий поток данных

```text
Android UI
  → ViewModel / Destination handlers
  → Repository / Use-case
  → Retrofit API или локальный DAO / Open-Meteo downloader
  → Backend Controller
  → Service / Security / Calculation logic
  → JPA Repository
  → PostgreSQL
```

Для погодных сценариев Android может обращаться напрямую к Open-Meteo:

```text
SummaryScreen / GraphScreen
  → ViewModel
  → ForecastRepository
  → ForecastDataCacher
  → ForecastDataDownloader
  → Open-Meteo Forecast API
```

---

## 3. Presentation Layer

### 3.1. Ответственность

Presentation отвечает только за визуальное представление, ввод пользователя и вызов переданных callback-обработчиков. Слой не должен напрямую выполнять SQL-запросы, HTTP-запросы или сложные расчеты.

### 3.2. Android

К Presentation относятся:

- `MainActivity.kt`, `App.kt` — запуск приложения и установка Compose-контента.
- `AppNavHost.kt` — нижняя навигация и подключение экранов.
- `*Destination.kt` — связывает экран с зависимостями из `AppContainer`.
- `*Screen.kt` и мелкие Compose-компоненты — отрисовка состояния.
- `common/AppTheme.kt`, `common/AppColors.kt`, `common/AppIcons.kt` — UI-тема и визуальные ресурсы.

Примеры экранов:

| Экран | Файлы/пакеты |
|-------|--------------|
| Главная погодная сводка | `summary/SummaryScreen.kt`, `summary/SummaryDestination.kt` |
| Графики | `graphs/EssentialGraphsScreen.kt`, `graphs/EssentialGraphsDestination.kt` |
| Избранное | `place/saved/FavoritesDestination.kt` и компоненты пакета `place/saved` |
| Аккаунт | `account/AccountDestination.kt` |
| Поддержка | `support/SupportDestination.kt` |
| Радиосигнал | `radio/RadioSignalDestination.kt` |
| Настройки | `settings/SettingsScreen.kt`, `settings/SettingsDestination.kt` |

### 3.3. Backend

Backend почти не имеет отдельного UI, кроме HTML admin panel:

- `AdminController.panel()` возвращает HTML-страницу администратора.
- Основной backend Presentation в REST-системе фактически представлен JSON-контрактом, но обработка HTTP относится к Control.

---

## 4. Control Layer

### 4.1. Ответственность

Control принимает действия пользователя или HTTP-запросы, выбирает нужный сценарий, проверяет доступ и передает управление Mediator-слою. В Android этот слой также хранит состояние экрана.

### 4.2. Android

К Control относятся:

- `AppNavHost.kt` — маршрутизация между экранами.
- `SummaryViewModel` — формирует `SummaryState` для главного экрана.
- `EssentialGraphsViewModel` — формирует состояние графиков.
- `PlacePickerViewModel` — управляет поиском/выбором мест.
- `SelectedUnitsViewModel` — управляет настройками единиц измерения.
- ViewModel внутри `SupportDestination.kt` — управляет диалогом поддержки.
- Destination-функции, которые создают ViewModel и передают callbacks в UI.

Control не должен знать детали HTTP endpoints или SQL-запросов. Он вызывает repository/use-case методы и получает готовую модель состояния.

### 4.3. Backend

К Control относятся REST controllers:

| Controller | Ответственность |
|------------|-----------------|
| `AccountController` | `/api/auth/**`, `/api/accounts/**`: регистрация, вход, профиль, пароль, удаление аккаунта. |
| `FavoriteCityController` | `/api/accounts/{accountId}/favorites`: список, поиск, создание, обновление, удаление избранных городов. |
| `SupportController` | `/api/accounts/{accountId}/support/messages` и `/api/admin/support/**`: сообщения поддержки. |
| `RadioSignalController` | `/api/accounts/{accountId}/radio-tests`: запуск расчета и история. |
| `UserStatsController` | `/api/accounts/{accountId}/stats`: агрегированная статистика аккаунта. |
| `AdminController` | `/api/admin/**`, `/admin/panel`: dashboard, аккаунты, роли, admin UI. |

Security-control:

- `SecurityConfig` задает правила доступа.
- `JwtAuthFilter` извлекает и проверяет Bearer token.
- `@PreAuthorize` на endpoints проверяет account-level доступ.

---

## 5. Mediator Layer

### 5.1. Ответственность

Mediator содержит бизнес-операции, преобразование данных, координацию Foundation-компонентов и принятие решений. Он не занимается отрисовкой UI и не должен быть привязан к конкретной визуальной реализации.

### 5.2. Android

К Mediator относятся:

| Компонент | Роль |
|-----------|------|
| `ForecastRepository` | Выбирает между кэшем и загрузкой прогноза, возвращает `ForecastResult`. |
| `ForecastConverter` | Преобразует сырые погодные данные в доменный `Forecast`. |
| `AccountRepository` | Координирует backend auth/profile API, Room-кэш аккаунта и auth-session. |
| `SavedPlacesRepository` | Управляет локально сохраненными местами и синхронизацией избранного. |
| `FavoritesSyncRepository` | Синхронизирует избранные города backend → Room. |
| `SupportRepository` | Отправляет/получает сообщения поддержки и кэширует их локально. |
| `RadioSignalRepository` | Запускает радиотест на backend и сохраняет результат в Room. |
| `SelectedPlaceRepository`, `SelectedUnitsRepository`, `AuthSessionRepository` | Управляют выбранным местом, единицами измерения и auth-сессией. |
| `Get*`, `Add*`, `Delete*` use-case классы | Подготавливают данные для UI: сводки, графики, избранное, выбор места. |

### 5.3. Backend

К Mediator относятся:

| Компонент | Роль |
|-----------|------|
| `AccountService` | Регистрация, вход, смена имени/пароля, удаление аккаунта, каскадная очистка связанных данных. |
| `JwtService` | Создание и проверка JWT. |
| `AccountAccessEvaluator` | Решение, может ли текущий пользователь получить доступ к accountId. |
| `FavoriteCityService` | CRUD и поиск избранных городов, преобразование JPA-сущностей в DTO. |
| `RadioSignalService` | Расчет расстояния, FSPL/path loss, влияния погоды, качества, latency и speed; сохранение истории радиотестов. |
| `SupportService` | Сбор admin conversation summary, отметка сообщений прочитанными, формирование conversation DTO и удаление переписки. |
| `UserStatsService` | Агрегация количества избранных городов, радиотестов и обращений поддержки. |
| `AdminService` | Подсчет dashboard-метрик, список аккаунтов и смена роли аккаунта. |

REST-контроллеры backend теперь остаются в Control-слое: они принимают HTTP-параметры, применяют `@PreAuthorize`/validation-аннотации и делегируют бизнес-операции соответствующим сервисам.

---

## 6. Entity Layer

### 6.1. Ответственность

Entity содержит данные предметной области и структуры обмена. Этот слой не должен самостоятельно ходить в сеть или БД.

### 6.2. Android entities и domain models

| Группа | Примеры |
|--------|---------|
| Погода | `Forecast`, `ForecastData`, `HourMoment`, `HourPeriod`, `Condition`, `Temperature`, `Humidity`, `Pressure`, `UvIndex`, `Visibility`, `Wind`, `Precipitation`, `Pop`, `SunEvent`. |
| Места | `Place`, `Location`, `Coordinates`, `SavedPlace`. |
| Состояния UI | `SummaryState`, `EssentialGraphsState`, `PlacePickerResults`, `ForecastResult`. |
| Room entities | `AccountEntity`, `FavoriteCityEntity`, `SupportTicketEntity`, `RadioSignalTestEntity`. |
| Remote DTO | `LoginRequest`, `RegisterRequest`, `AuthResponse`, `AccountDto`, `FavoriteCityDto`, `SupportMessageDto`, `RadioSignalResponseDto`, `StatsDto`. |

### 6.3. Backend entities и DTO

| Группа | Примеры |
|--------|---------|
| JPA entities | `UserAccountEntity`, `FavoriteCityEntity`, `SupportMessageEntity`, `RadioSignalTestEntity`. |
| Enums/value types | `AccountRole`. |
| DTO records | `AccountDtos.*`, `FavoriteCityRequest/Response`, `CreateMessageRequest`, `SupportMessageResponse`, `RadioSignalRequest/Response`, `StatsResponse`, `DashboardResponse`. |

---

## 7. Foundation Layer

### 7.1. Ответственность

Foundation инкапсулирует инфраструктуру: базы данных, HTTP-клиенты, конфигурацию, файловые операции и внешние сервисы. Верхние слои не должны знать технические детали подключения.

### 7.2. Android

| Компонент | Назначение |
|-----------|------------|
| `BuraBackendApi` | Retrofit-интерфейс REST API backend. |
| `ApiProvider` | Создает Retrofit/OkHttp, добавляет JWT interceptor и обработку 401. |
| `BuraDao` | Room DAO для локальных таблиц аккаунта, избранного, поддержки и радиотестов. |
| `BuraDatabase` | Конфигурация Room database. |
| `ForecastDataDownloader` | HTTPS-загрузка прогноза Open-Meteo. |
| `SearchPlaces` | Запросы к Open-Meteo Geocoding API. |
| `ForecastDataCacher` | Файловое кэширование прогноза. |
| `SharedPreferences` | Хранение сессии, настроек, выбранного места. |

### 7.3. Backend

| Компонент | Назначение |
|-----------|------------|
| `UserAccountRepository` | Spring Data JPA доступ к аккаунтам. |
| `FavoriteCityRepository` | Доступ к избранным городам. |
| `SupportMessageRepository` | Доступ к сообщениям поддержки. |
| `RadioSignalTestRepository` | Доступ к истории радиотестов. |
| PostgreSQL | Основное серверное хранилище. |
| `RestClient` в `RadioSignalService` | Запрос погодных данных Open-Meteo для расчета радиосигнала. |
| `application.yml`, `application-dev.yml`, `application-prod.yml` | Конфигурация профилей, datasource, JWT и support mailbox. |
| `docker-compose.yml` | Инфраструктурный запуск PostgreSQL и backend. |

---

## 8. Маппинг пакетов на PCMEF

| Пакет/модуль | Основной слой | Комментарий |
|--------------|---------------|-------------|
| `app/src/main/java/com/docesforg/bura/common` | Presentation/Entity | UI-тема, helpers, часть value utilities. |
| `app/.../summary`, `app/.../graphs` | Presentation/Control/Mediator/Entity | Экраны, ViewModel, use-cases, summary/graph models. |
| `app/.../forecast` | Mediator/Entity/Foundation | Repository, converter, forecast data, downloader/cache. |
| `app/.../place` | Presentation/Control/Mediator/Entity/Foundation | Поиск, выбор, сохранение и синхронизация мест. |
| `app/.../platform/remote` | Foundation/Entity | Retrofit API и DTO. |
| `app/.../platform/local` | Foundation/Entity | Room DB, DAO и локальные entities. |
| `server/.../account` | Control/Mediator/Entity/Foundation | Controller, service, DTO, JPA entity/repository. |
| `server/.../security` | Control/Mediator/Foundation | Security config, JWT filter/service, access evaluator. |
| `server/.../favorite` | Control/Entity/Foundation | CRUD controller, entity, repository. |
| `server/.../support` | Control/Mediator/Entity/Foundation | REST/admin support workflows, entity, repository. |
| `server/.../signal` | Control/Mediator/Entity/Foundation | REST радиотестов, расчет, entity, repository, Open-Meteo client. |
| `server/.../stats`, `server/.../admin` | Control/Mediator | Тонкие controllers и service-классы для агрегаций, ролей и admin UI/API. |

---

## 9. Правила развития проекта

1. Новый экран добавлять в Presentation как `*Destination` + `*Screen`, а состояние держать во ViewModel.
2. Новую бизнес-операцию оформлять repository/use-case/service методом, а не писать в Compose UI.
3. Новый REST endpoint описывать одновременно на backend controller и в `BuraBackendApi`/DTO, если он нужен Android-клиенту.
4. Новую таблицу backend оформлять как JPA entity + Spring Data repository.
5. Новую локальную таблицу Android оформлять как Room entity + DAO методы + повышение версии `BuraDatabase`.
6. Для новых protected endpoints добавлять проверку доступа через Spring Security или `@PreAuthorize`.
7. DTO должны оставаться простыми структурами данных без бизнес-логики.
