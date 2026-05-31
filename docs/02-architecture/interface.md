# Интерфейсы проекта Bura

## 1. Назначение документа

Документ описывает основные интерфейсы проекта Bura: программные интерфейсы Kotlin/Java, REST API между Android и backend, локальные DAO-интерфейсы и внешние HTTP-интерфейсы.

---

## 2. Android → Backend REST API

Android-клиент обращается к backend через Retrofit-интерфейс `BuraBackendApi`. Базовый URL для emulator-конфигурации: `http://10.0.2.2:8080/`. Для защищенных endpoints `ApiProvider` добавляет заголовок:

```http
Authorization: Bearer <jwt>
```

### 2.1. Аутентификация и аккаунт

| Метод | Endpoint | Тело запроса | Ответ | Назначение |
|-------|----------|--------------|-------|------------|
| `POST` | `/api/auth/login` | `LoginRequest(email, password)` | `AuthResponse(token, account)` | Вход пользователя. |
| `POST` | `/api/auth/register` | `RegisterRequest(email, displayName, password)` | `AuthResponse(token, account)` | Регистрация пользователя. |
| `GET` | `/api/accounts/{accountId}` | — | `AccountResponse` | Получение профиля аккаунта. |
| `PATCH` | `/api/accounts/{accountId}/name` | `UpdateNameRequest(displayName)` | `AccountResponse` | Изменение отображаемого имени. |
| `PATCH` | `/api/accounts/{accountId}/password` | `UpdatePasswordRequest(password)` | `204/empty` | Изменение пароля. |
| `DELETE` | `/api/accounts/{accountId}` | query `allowAdminDelete` | `204/empty` | Удаление аккаунта и связанных данных. |

Android DTO:

- `LoginRequest`
- `RegisterRequest`
- `AuthResponse`
- `AccountDto`
- `UpdateNameRequestDto`
- `UpdatePasswordRequestDto`

Backend DTO:

- `AccountDtos.LoginRequest`
- `AccountDtos.RegisterRequest`
- `AccountDtos.AuthResponse`
- `AccountDtos.AccountResponse`
- `AccountDtos.UpdateNameRequest`
- `AccountDtos.UpdatePasswordRequest`

### 2.2. Избранные города

| Метод | Endpoint | Тело/параметры | Ответ | Назначение |
|-------|----------|----------------|-------|------------|
| `GET` | `/api/accounts/{accountId}/favorites` | — | `List<FavoriteCityResponse>` | Список избранных городов. |
| `GET` | `/api/accounts/{accountId}/favorites/search?city=...` | query `city` | `List<FavoriteCityResponse>` | Поиск по избранным городам пользователя. |
| `POST` | `/api/accounts/{accountId}/favorites` | `FavoriteCityRequest(cityName, latitude, longitude)` | `FavoriteCityResponse` | Добавление города в избранное. |
| `PUT` | `/api/accounts/{accountId}/favorites/{favoriteId}` | `FavoriteCityRequest` | `FavoriteCityResponse` | Обновление избранного города. |
| `DELETE` | `/api/accounts/{accountId}/favorites/{favoriteId}` | — | `204/empty` | Удаление избранного города. |

Android DTO:

- `FavoriteCityDto`
- `FavoriteCityRequestDto`

Backend DTO:

- `FavoriteCityController.FavoriteCityRequest`
- `FavoriteCityController.FavoriteCityResponse`

### 2.3. Поддержка

| Метод | Endpoint | Тело запроса | Ответ | Назначение |
|-------|----------|--------------|-------|------------|
| `POST` | `/api/accounts/{accountId}/support/messages` | `CreateMessageRequest(email, name, message)` | `SupportMessageResponse` | Отправка сообщения пользователя в поддержку. |
| `GET` | `/api/accounts/{accountId}/support/messages` | — | `SupportConversationResponse` | Получение диалога пользователя с поддержкой. |
| `DELETE` | `/api/accounts/{accountId}/support/messages` | — | `204/empty` | Удаление диалога пользователя. |
| `GET` | `/api/admin/support/conversations` | — | `List<AdminConversationSummaryResponse>` | Список диалогов для администратора. |
| `GET` | `/api/admin/support/accounts/{accountId}/messages` | — | `SupportConversationResponse` | Просмотр диалога пользователя администратором. |
| `POST` | `/api/admin/support/accounts/{accountId}/messages` | `SendMessageRequest(message)` | `SupportMessageResponse` | Ответ администратора пользователю. |

Android DTO:

- `SendSupportMessageRequestDto`
- `SupportMessageDto`
- `SupportConversationDto`

Backend DTO:

- `SupportController.CreateMessageRequest`
- `SupportController.SendMessageRequest`
- `SupportController.SupportMessageResponse`
- `SupportController.SupportConversationResponse`
- `SupportController.AdminConversationSummaryResponse`

### 2.4. Радиосигнал

| Метод | Endpoint | Тело запроса | Ответ | Назначение |
|-------|----------|--------------|-------|------------|
| `GET` | `/api/accounts/{accountId}/radio-tests` | — | `List<RadioSignalResponse>` | История тестов радиосигнала. |
| `POST` | `/api/accounts/{accountId}/radio-tests` | `RadioSignalRequest` | `RadioSignalResponse` | Расчет радиосигнала между двумя городами. |

`RadioSignalRequest` содержит:

- `cityA`, `cityB`
- `latitudeA`, `longitudeA`
- `latitudeB`, `longitudeB`
- `frequencyMhz`

`RadioSignalResponse` содержит:

- `id`
- `cityA`, `cityB`
- `distanceKm`
- `pathLossDb`
- `quality`
- `latencyMs`
- `speedMbps`
- `createdAt`

### 2.5. Статистика аккаунта

| Метод | Endpoint | Ответ | Назначение |
|-------|----------|-------|------------|
| `GET` | `/api/accounts/{accountId}/stats` | `StatsResponse(favorites, radioTests, supportRequests)` | Сводная статистика пользователя. |

### 2.6. Администрирование

| Метод | Endpoint | Тело/ответ | Назначение |
|-------|----------|------------|------------|
| `GET` | `/api/admin/dashboard` | `DashboardResponse` | Количество пользователей, админов, избранного, радиотестов и обращений. |
| `GET` | `/api/admin/accounts` | `List<AccountAdminView>` | Список аккаунтов. |
| `PATCH` | `/api/admin/accounts/{accountId}/role` | `AccountRoleUpdateRequest(role)` → `AccountAdminView` | Изменение роли пользователя. |
| `GET` | `/admin/panel` | `text/html` | HTML-панель администратора. |

---

## 3. Android Kotlin-интерфейсы

### 3.1. `BuraBackendApi`

`BuraBackendApi` — основной typed REST contract Android-приложения. Он отделяет repository layer от деталей URL, HTTP-методов и сериализации.

Ключевые группы методов:

- auth/account: `login`, `register`, `deleteAccount`, `updateName`, `updatePassword`;
- favorites: `favorites`, `addFavorite`, `deleteFavorite`;
- support: `sendSupportMessage`, `supportConversation`, `deleteSupportConversation`;
- radio: `runSignalTest`, `radioHistory`;
- stats: `stats`.

Используется слоями Mediator:

- `AccountRepository`
- `FavoritesSyncRepository`
- `SavedPlacesRepository`
- `SupportRepository`
- `RadioSignalRepository`

### 3.2. `BuraDao`

`BuraDao` — Room DAO для локального кэша пользовательских данных.

Группы методов:

| Группа | Методы |
|--------|--------|
| Account | `upsertAccount`, `getAccount`, `deleteAccount` |
| Favorites | `upsertFavorites`, `getFavorites`, `deleteFavorites` |
| Support | `upsertSupport`, `getSupportTickets`, `deleteSupportTickets` |
| Radio tests | `upsertRadioTest`, `getRadioTests`, `deleteRadioTests` |

DAO используется repository-классами Android и не должен вызываться напрямую из Compose UI.

### 3.3. Sealed-интерфейсы UI/domain состояний

В Android используются Kotlin sealed interfaces для безопасного моделирования вариантов состояния:

| Интерфейс | Назначение |
|-----------|------------|
| `AppRoutes` | Типизированные маршруты навигации: Home, Favorites, Account, Support, RadioSignal, EssentialGraphs, Settings. |
| `ForecastResult<T>` | Результат получения прогноза: `Success`, `FailedToDownload`, `Outdated`. |
| `SummaryState` | Состояние главного экрана: success/loading/errors/no selected place. |
| `EssentialGraphsState` | Состояние экрана графиков. |
| `PlacePickerResults` | Результат выбора/поиска места. |
| `HourSummary` | Элемент почасовой ленты: погода или солнечное событие. |
| `FuturePrecipitation` | Будущие осадки: через N часов, в конкретный день или отсутствие. |
| `PrecipitationTotal` | Суммарные осадки за сегодня или другой день. |
| `SunSummary`, `Sunrise`, `Sunset` | Варианты состояния восхода/заката. |
| `UseProtection` | Рекомендация защиты от UV: интервал, до конца дня или не требуется. |

---

## 4. Backend Java-интерфейсы

### 4.1. Spring Data JPA repositories

Spring Data repositories являются Foundation-интерфейсами backend. Они описывают доступ к PostgreSQL, а реализацию генерирует Spring Data JPA.

#### `UserAccountRepository`

Наследует `JpaRepository<UserAccountEntity, Long>`.

Методы:

- `findByEmail(String email)`
- `existsByEmail(String email)`
- `countByRole(String role)`

Используется `AccountService`, `AdminController`, security-компонентами.

#### `FavoriteCityRepository`

Наследует `JpaRepository<FavoriteCityEntity, Long>`.

Методы:

- `findAllByAccountId(long accountId)`
- `findAllByAccountIdAndCityNameContainingIgnoreCase(long accountId, String cityName)`
- `findByIdAndAccountId(long id, long accountId)`
- `deleteAllByAccountId(long accountId)`

Используется для CRUD избранных городов и статистики.

#### `SupportMessageRepository`

Наследует `JpaRepository<SupportMessageEntity, Long>`.

Методы:

- `findAllByAccountIdOrderByCreatedAtAsc(long accountId)`
- `findFirstByAccountIdOrderByCreatedAtAsc(long accountId)`
- `findFirstByAccountIdOrderByCreatedAtDesc(long accountId)`
- `findAllByOrderByCreatedAtDesc()`
- `findAllByAccountIdAndSenderAndSeenByAdminFalse(long accountId, String sender)`
- `existsByAccountId(long accountId)`
- `existsByAccountIdAndSenderAndSeenByAdminFalse(long accountId, String sender)`
- `countByAccountId(long accountId)`
- `deleteAllByAccountId(long accountId)`
- `countDistinctAccountId()`

Используется поддержкой, админ-панелью, статистикой и удалением аккаунта.

#### `RadioSignalTestRepository`

Наследует `JpaRepository<RadioSignalTestEntity, Long>`.

Методы:

- `findAllByAccountIdOrderByCreatedAtDesc(long accountId)`
- `deleteAllByAccountId(long accountId)`

Используется историей радиотестов, статистикой и удалением аккаунта.

### 4.2. REST controller interfaces как HTTP-контракты

Контроллеры backend образуют публичный HTTP-интерфейс системы:

- `AccountController`
- `FavoriteCityController`
- `SupportController`
- `RadioSignalController`
- `UserStatsController`
- `AdminController`

Внутри Java они реализованы как классы, но для внешних клиентов их интерфейсом являются HTTP method + path + JSON DTO + status code.

### 4.3. Security interfaces

| Компонент | Интерфейсная роль |
|-----------|-------------------|
| `JwtAuthFilter` | Принимает HTTP request, извлекает JWT, устанавливает `Authentication` в SecurityContext. |
| `JwtService` | Методы создания и проверки токена. |
| `AccountAccessEvaluator` | Метод доступа для SpEL выражения `@accountAccess.canAccess(...)`. |
| `SecurityConfig` | Декларирует правила доступа и подключает JWT filter. |

---

## 5. Внешние интерфейсы

### 5.1. Open-Meteo Forecast API

Android `ForecastDataDownloader` вызывает:

```text
GET https://api.open-meteo.com/v1/forecast
```

Параметры включают:

- `latitude`, `longitude`
- hourly-поля: temperature, humidity, dew point, apparent temperature, precipitation probability, rain, showers, snowfall, weather code, pressure, visibility, wind speed/direction/gusts, uv index, is_day
- daily-поля: sunrise, sunset
- `wind_speed_unit=ms`
- `timezone=auto`
- `past_days=1`

Backend `RadioSignalController` также использует Open-Meteo для получения погодного snapshot при расчете радиосигнала.

### 5.2. Open-Meteo Geocoding API

Android поиск мест использует:

```text
GET https://geocoding-api.open-meteo.com/v1/search
```

Результаты преобразуются в доменные `Place`/`Location` и используются экраном выбора города.

---

## 6. Локальные интерфейсы хранения

### 6.1. Room tables

| Таблица | Entity | Назначение |
|---------|--------|------------|
| `account` | `AccountEntity` | Локальный кэш текущего аккаунта. |
| `favorite_city` | `FavoriteCityEntity` | Локальный кэш избранных городов пользователя. |
| `support_ticket` | `SupportTicketEntity` | Локальный кэш сообщений поддержки. |
| `radio_signal_test` | `RadioSignalTestEntity` | Локальный кэш истории радиотестов. |

### 6.2. PostgreSQL tables

| Таблица | Entity | Назначение |
|---------|--------|------------|
| `user_account` | `UserAccountEntity` | Аккаунты, email, displayName, passwordHash, role. |
| `favorite_city` | `FavoriteCityEntity` | Избранные города пользователей. |
| `support_message` | `SupportMessageEntity` | Сообщения пользователей и администраторов. |
| `radio_signal_test` | `RadioSignalTestEntity` | История расчетов радиосигнала. |

---

## 7. Версионирование и совместимость интерфейсов

1. При изменении backend DTO нужно обновить Android DTO в `platform.remote`.
2. При добавлении endpoint нужно добавить метод в `BuraBackendApi`, если endpoint нужен мобильному клиенту.
3. При изменении Room-схемы нужно увеличить `BuraDatabase.version`; сейчас используется destructive migration, поэтому production-миграции требуют отдельной реализации.
4. При добавлении защищенного endpoint нужно явно описать правило доступа в Spring Security или `@PreAuthorize`.
5. При изменении Open-Meteo полей нужно обновить парсинг в downloader/converter и UI-модели, которые зависят от этих данных.
