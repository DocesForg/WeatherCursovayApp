# Спецификация методов проекта Bura

## 1. Назначение документа

Документ описывает методы серверной и мобильной частей Bura на уровне детального проектирования: назначение, входные и выходные данные, правила доступа, основные алгоритмы и ошибки. Спецификация предназначена для реализации, ревью, тестирования и сопровождения проекта.

## 2. Общие соглашения

### 2.1. Архитектурные слои

| Слой | Компоненты | Ответственность |
| --- | --- | --- |
| Android UI | `*Destination`, `*Screen`, `ViewModel` | Отображение экранов, сбор пользовательского ввода, запуск сценариев через репозитории. |
| Android data | `AccountRepository`, `SavedPlacesRepository`, `ForecastRepository`, `BuraBackendApi` | Работа с backend API, локальной БД Room, файлами и кэшем. |
| Backend API | Spring `@RestController` | REST-методы аккаунтов, избранного, поддержки, радиотестов, статистики и администрирования. |
| Backend domain/service | `AccountService`, `JwtService`, репозитории JPA | Бизнес-правила, авторизация, хэширование паролей, JWT, каскадное удаление данных. |
| External API | Open-Meteo | Геокодирование, прогноз погоды, текущие погодные параметры для радиотеста. |

### 2.2. Авторизация

- Публичные методы backend: `POST /api/auth/register`, `POST /api/auth/login`, Swagger/OpenAPI endpoints.
- Остальные backend-методы требуют JWT Bearer token в заголовке `Authorization: Bearer <token>`.
- Доступ к пользовательским ресурсам проверяется выражением `@accountAccess.canAccess(accountId, authentication)`:
  - владелец аккаунта может работать только со своим `accountId`;
  - администратор с ролью `ADMIN` может работать с любым `accountId`.
- Административные методы требуют `hasRole('ADMIN')`.

### 2.3. Формат ошибок backend

| Условие | HTTP-статус | Описание |
| --- | --- | --- |
| Некорректный или отсутствующий JWT | `401 Unauthorized` | Запрос к защищенному endpoint без действующей сессии. |
| Недостаточно прав | `403 Forbidden` | Пользователь обращается к чужому ресурсу или не является администратором. |
| Ресурс не найден | `404 Not Found` | Аккаунт, избранный город или диалог поддержки отсутствует. |
| Конфликт бизнес-правила | `409 Conflict` | Например, повторная регистрация email или удаление администратора без явного подтверждения. |
| Ошибка валидации | `400 Bad Request` | Нарушены ограничения `@Email`, `@NotBlank` или передана неподдерживаемая роль. |

## 3. Backend REST API

### 3.1. Методы аккаунта и аутентификации

#### `POST /api/auth/register` — регистрация

| Параметр | Тип | Обязательность | Описание |
| --- | --- | --- | --- |
| `email` | `String`, email | да | Уникальная почта пользователя. |
| `displayName` | `String`, not blank | да | Отображаемое имя. |
| `password` | `String`, not blank | да | Пароль в открытом виде, на сервере сохраняется только BCrypt-хэш. |

**Результат:** `AuthResponse { token, account }`, где `account = { id, email, displayName, role }`.

**Алгоритм:**
1. Проверить уникальность `email`.
2. Создать `UserAccountEntity`.
3. Захэшировать пароль через `PasswordEncoder`.
4. Назначить роль: `ADMIN`, если email находится в конфигурации `app.admin.emails`, иначе `USER`.
5. Сохранить аккаунт и выпустить JWT.

**Ошибки:** `409 Conflict`, если email уже существует; `400 Bad Request`, если поля невалидны.

#### `POST /api/auth/login` — вход

| Параметр | Тип | Обязательность | Описание |
| --- | --- | --- | --- |
| `email` | `String`, email | да | Почта аккаунта. |
| `password` | `String`, not blank | да | Пароль пользователя. |

**Результат:** `AuthResponse { token, account }`.

**Алгоритм:**
1. Найти аккаунт по email.
2. Сверить пароль с BCrypt-хэшем.
3. Синхронизировать роль с `app.admin.emails`, если конфигурация изменилась.
4. Вернуть новый JWT и DTO аккаунта.

**Ошибки:** `401 Unauthorized`, если аккаунт не найден или пароль неверный.

#### `GET /api/accounts/{accountId}` — получение профиля

| Параметр | Тип | Источник | Описание |
| --- | --- | --- | --- |
| `accountId` | `long` | path | Идентификатор аккаунта. |

**Доступ:** владелец аккаунта или администратор.

**Результат:** `AccountResponse { id, email, displayName, role }`.

**Ошибки:** `404 Not Found`/исключение поиска, если аккаунт отсутствует; `403 Forbidden`, если нет доступа.

#### `PATCH /api/accounts/{accountId}/name` — изменение имени

| Параметр | Тип | Обязательность | Описание |
| --- | --- | --- | --- |
| `accountId` | `long` | да | Идентификатор аккаунта. |
| `displayName` | `String`, not blank | да | Новое отображаемое имя. |

**Доступ:** владелец аккаунта или администратор.

**Результат:** обновленный `AccountResponse`.

**Алгоритм:** найти аккаунт, записать `displayName`, сохранить запись.

#### `PATCH /api/accounts/{accountId}/password` — изменение пароля

| Параметр | Тип | Обязательность | Описание |
| --- | --- | --- | --- |
| `accountId` | `long` | да | Идентификатор аккаунта. |
| `password` | `String`, not blank | да | Новый пароль. |

**Доступ:** владелец аккаунта или администратор.

**Результат:** пустой ответ при успешном выполнении.

**Алгоритм:** найти аккаунт, вычислить новый BCrypt-хэш, сохранить запись.

#### `DELETE /api/accounts/{accountId}` — удаление аккаунта

| Параметр | Тип | Источник | Описание |
| --- | --- | --- | --- |
| `accountId` | `long` | path | Удаляемый аккаунт. |
| `allowAdminDelete` | `boolean` | query, default `false` | Явное подтверждение удаления администратора. |

**Доступ:** владелец аккаунта или администратор.

**Результат:** пустой ответ.

**Алгоритм:**
1. Найти аккаунт.
2. Если аккаунт имеет роль `ADMIN` и `allowAdminDelete=false`, остановить удаление.
3. В транзакции удалить избранные города, радиотесты, сообщения поддержки и сам аккаунт.

**Ошибки:** `404 Not Found`, если аккаунт отсутствует; `409 Conflict`, если удаляется администратор без подтверждения.

### 3.2. Методы избранных городов

Базовый путь: `/api/accounts/{accountId}/favorites`.

| Метод | Endpoint | Назначение | Входные данные | Результат |
| --- | --- | --- | --- | --- |
| `GET` | `/api/accounts/{accountId}/favorites` | Получить все избранные города аккаунта. | `accountId` | `List<FavoriteCityResponse>` |
| `GET` | `/api/accounts/{accountId}/favorites/search?city={city}` | Найти избранные города по части названия без учета регистра. | `accountId`, `city` | `List<FavoriteCityResponse>` |
| `POST` | `/api/accounts/{accountId}/favorites` | Добавить город в избранное. | `FavoriteCityRequest` | `201 Created`, `FavoriteCityResponse` |
| `PUT` | `/api/accounts/{accountId}/favorites/{favoriteId}` | Полностью обновить избранный город. | `favoriteId`, `FavoriteCityRequest` | `FavoriteCityResponse` |
| `DELETE` | `/api/accounts/{accountId}/favorites/{favoriteId}` | Удалить избранный город. | `favoriteId` | `204 No Content` |

`FavoriteCityRequest`:

| Поле | Тип | Описание |
| --- | --- | --- |
| `cityName` | `String`, not blank | Название города. |
| `latitude` | `double` | Широта. |
| `longitude` | `double` | Долгота. |

`FavoriteCityResponse`: `id`, `cityName`, `latitude`, `longitude`.

**Доступ:** владелец аккаунта или администратор.

**Ошибки:** `404 Not Found`, если `favoriteId` не принадлежит аккаунту или отсутствует.

### 3.3. Методы радиотестов

Базовый путь: `/api/accounts/{accountId}/radio-tests`.

#### `GET /api/accounts/{accountId}/radio-tests` — история радиотестов

**Доступ:** владелец аккаунта или администратор.

**Результат:** список `RadioSignalResponse`, отсортированный по `createdAt` по убыванию.

#### `POST /api/accounts/{accountId}/radio-tests` — расчет качества радиосигнала

`RadioSignalRequest`:

| Поле | Тип | Описание |
| --- | --- | --- |
| `cityA`, `cityB` | `String`, not blank | Названия точек маршрута. |
| `latitudeA`, `longitudeA` | `double` | Координаты первой точки. |
| `latitudeB`, `longitudeB` | `double` | Координаты второй точки. |
| `frequencyMhz` | `double` | Частота в МГц; если значение `<= 0`, используется `900.0`. |

`RadioSignalResponse`:

| Поле | Тип | Описание |
| --- | --- | --- |
| `id` | `Long` | Идентификатор сохраненного теста. |
| `cityA`, `cityB` | `String` | Названия точек. |
| `distanceKm` | `double` | Расстояние по формуле гаверсинуса. |
| `pathLossDb` | `double` | Потери на трассе с учетом погоды. |
| `quality` | `String` | `Excellent`, `Good`, `Fair` или `Poor`. |
| `latencyMs` | `double` | Расчетная задержка распространения сигнала. |
| `speedMbps` | `double` | Оценочная скорость передачи данных. |
| `createdAt` | `Instant` | Время выполнения теста. |

**Алгоритм:**
1. Рассчитать расстояние между координатами по формуле гаверсинуса.
2. Получить текущую температуру, влажность, дождь и облачность для обеих точек через Open-Meteo.
3. Если Open-Meteo недоступен, использовать погодные значения по умолчанию: `15 °C`, `60%`, `0 мм`, `50%`.
4. Усреднить погодные показатели между точками.
5. Рассчитать FSPL: `20*log10(distanceMeters) + 20*log10(frequencyHz) - 147.55`.
6. Добавить погодное затухание: влажность, дождь, облачность, отклонение температуры от `15 °C`.
7. Классифицировать качество:
   - `< 110 dB` — `Excellent`;
   - `< 125 dB` — `Good`;
   - `< 140 dB` — `Fair`;
   - иначе `Poor`.
8. Рассчитать задержку с учетом показателя преломления воздуха `1.0003`.
9. Рассчитать скорость по фактору качества и штрафу задержки.
10. Сохранить тест и вернуть DTO.

### 3.4. Методы поддержки

#### Пользовательские методы

| Метод | Endpoint | Назначение | Входные данные | Результат |
| --- | --- | --- | --- | --- |
| `POST` | `/api/accounts/{accountId}/support/messages` | Отправить сообщение пользователя в поддержку. | `CreateMessageRequest` | `SupportMessageResponse` |
| `GET` | `/api/accounts/{accountId}/support/messages` | Получить диалог пользователя с поддержкой. | `accountId` | `SupportConversationResponse` |
| `DELETE` | `/api/accounts/{accountId}/support/messages` | Удалить диалог пользователя. | `accountId` | пустой ответ |

`CreateMessageRequest`: `email`, `name`, `message`. `email` валидируется как email, `name` и `message` не должны быть пустыми.

**Особенности:**
- пользовательские сообщения сохраняются с `sender = USER` и `seenByAdmin = false`;
- `forwardTo` заполняется из конфигурации `app.support.mailbox`;
- при запросе несуществующего диалога возвращается `404 Not Found`.

#### Административные методы поддержки

| Метод | Endpoint | Назначение | Результат |
| --- | --- | --- | --- |
| `GET` | `/api/admin/support/conversations` | Список диалогов, сгруппированных по аккаунтам, с последним сообщением и флагом непрочитанности. | `List<AdminConversationSummaryResponse>` |
| `GET` | `/api/admin/support/accounts/{accountId}/messages` | Получить диалог аккаунта и отметить пользовательские непрочитанные сообщения как прочитанные администратором. | `SupportConversationResponse` |
| `POST` | `/api/admin/support/accounts/{accountId}/messages` | Отправить ответ администратора в существующий диалог. | `SupportMessageResponse` |

**Доступ:** только `ADMIN`.

**Ошибки:** `404 Not Found`, если администратор отвечает в несуществующий диалог.

### 3.5. Методы пользовательской статистики

#### `GET /api/accounts/{accountId}/stats`

**Доступ:** владелец аккаунта или администратор.

**Результат:** `StatsResponse`.

| Поле | Тип | Описание |
| --- | --- | --- |
| `favorites` | `int` | Количество избранных городов аккаунта. |
| `radioTests` | `int` | Количество сохраненных радиотестов аккаунта. |
| `supportRequests` | `long` | `1`, если у аккаунта есть диалог поддержки, иначе `0`. |

### 3.6. Административные методы

| Метод | Endpoint | Назначение | Результат |
| --- | --- | --- | --- |
| `GET` | `/api/admin/dashboard` | Получить агрегированные счетчики системы. | `DashboardResponse` |
| `GET` | `/api/admin/accounts` | Получить список аккаунтов. | `List<AccountAdminView>` |
| `PATCH` | `/api/admin/accounts/{accountId}/role` | Изменить роль аккаунта. | `AccountAdminView` |
| `GET` | `/admin/panel` | Вернуть HTML-страницу панели поддержки. | `text/html` |

`DashboardResponse`: `users`, `admins`, `favorites`, `radioTests`, `supportRequests`.

`AccountRoleUpdateRequest`:

| Поле | Тип | Описание |
| --- | --- | --- |
| `role` | `String`, not blank | Новая роль. Допустимые значения после нормализации: `USER`, `ADMIN`. |

**Доступ:** только `ADMIN`.

**Ошибки:** `400 Bad Request`, если роль не поддерживается; `404 Not Found`, если аккаунт отсутствует.

## 4. Backend service methods

### 4.1. `AccountService`

| Метод | Назначение | Вход | Выход | Побочные эффекты |
| --- | --- | --- | --- | --- |
| `register(request)` | Создание аккаунта. | `RegisterRequest` | `AuthResponse` | Создает запись аккаунта, хэширует пароль, выдает JWT. |
| `login(request)` | Аутентификация. | `LoginRequest` | `AuthResponse` | Может обновить роль аккаунта согласно конфигурации admin email. |
| `get(accountId)` | Получение DTO аккаунта. | `long` | `AccountResponse` | Нет. |
| `updateName(accountId, displayName)` | Изменение имени. | `long`, `String` | `AccountResponse` | Обновляет запись аккаунта. |
| `updatePassword(accountId, password)` | Изменение пароля. | `long`, `String` | `void` | Обновляет BCrypt-хэш. |
| `delete(accountId, allowAdminDelete)` | Каскадное удаление аккаунта. | `long`, `boolean` | `void` | Удаляет связанные избранные города, радиотесты и сообщения поддержки. |
| `toAuth(account)` | Формирование ответа аутентификации. | `UserAccountEntity` | `AuthResponse` | Создает JWT. |
| `toDto(account)` | Маппинг entity в DTO. | `UserAccountEntity` | `AccountResponse` | Нет. |
| `resolveRole(email)` | Определение роли по конфигурации. | `String` | `AccountRole` | Нет. |

### 4.2. `JwtService`

| Метод | Назначение | Вход | Выход | Правила |
| --- | --- | --- | --- | --- |
| `createToken(accountId, email, role)` | Создать JWT. | id аккаунта, email, роль | `String` | Subject = email, claims: `uid`, `role`, срок действия из `app.jwt.expiration-seconds`, подпись HS256. |
| `parseToken(token)` | Проверить и разобрать JWT. | `String` | `Claims` | При ошибке подписи, формата или срока действия выбрасывает `IllegalArgumentException`. |

### 4.3. `AccountAccessEvaluator`

| Метод | Назначение | Вход | Выход | Правила |
| --- | --- | --- | --- | --- |
| `canAccess(accountId, authentication)` | Проверить доступ к ресурсу аккаунта. | `long`, `Authentication` | `boolean` | `true` для администратора; для пользователя — только если principal равен `accountId`. |

### 4.4. `JwtAuthFilter`

| Метод | Назначение | Правила |
| --- | --- | --- |
| `doFilterInternal(request, response, filterChain)` | Извлечь JWT, распарсить claims и заполнить `SecurityContext`. | При невалидном токене очищает контекст и продолжает цепочку фильтров; итоговый запрет выполняет Spring Security. |
| `extractToken(request)` | Получить токен из запроса. | Основной источник — заголовок `Authorization`; для `/admin/panel` допускается query-параметр `token`. |

## 5. Android backend API interface

Интерфейс `BuraBackendApi` описывает Retrofit-клиент backend. Все методы, кроме `login` и `register`, выполняются с JWT, который добавляет `ApiProvider` через OkHttp interceptor.

| Метод Kotlin | HTTP | Endpoint | DTO запроса | DTO ответа |
| --- | --- | --- | --- | --- |
| `login(body)` | `POST` | `api/auth/login` | `LoginRequest` | `AuthResponse` |
| `register(body)` | `POST` | `api/auth/register` | `RegisterRequest` | `AuthResponse` |
| `deleteAccount(accountId)` | `DELETE` | `api/accounts/{accountId}` | — | `Unit` |
| `updateName(accountId, body)` | `PATCH` | `api/accounts/{accountId}/name` | `UpdateNameRequestDto` | `AccountDto` |
| `updatePassword(accountId, body)` | `PATCH` | `api/accounts/{accountId}/password` | `UpdatePasswordRequestDto` | `Unit` |
| `favorites(accountId)` | `GET` | `api/accounts/{accountId}/favorites` | — | `List<FavoriteCityDto>` |
| `addFavorite(accountId, body)` | `POST` | `api/accounts/{accountId}/favorites` | `FavoriteCityRequestDto` | `FavoriteCityDto` |
| `deleteFavorite(accountId, favoriteId)` | `DELETE` | `api/accounts/{accountId}/favorites/{favoriteId}` | — | `Unit` |
| `sendSupportMessage(accountId, body)` | `POST` | `api/accounts/{accountId}/support/messages` | `SendSupportMessageRequestDto` | `SupportMessageDto` |
| `supportConversation(accountId)` | `GET` | `api/accounts/{accountId}/support/messages` | — | `SupportConversationDto` |
| `deleteSupportConversation(accountId)` | `DELETE` | `api/accounts/{accountId}/support/messages` | — | `Unit` |
| `runSignalTest(accountId, body)` | `POST` | `api/accounts/{accountId}/radio-tests` | `RadioSignalRequestDto` | `RadioSignalResponseDto` |
| `radioHistory(accountId)` | `GET` | `api/accounts/{accountId}/radio-tests` | — | `List<RadioSignalResponseDto>` |
| `stats(accountId)` | `GET` | `api/accounts/{accountId}/stats` | — | `StatsDto` |

## 6. Android repository methods

### 6.1. `AuthSessionRepository`

| Метод | Назначение | Вход | Выход | Хранилище |
| --- | --- | --- | --- | --- |
| `isLoggedIn()` | Проверить состояние сессии. | — | `Boolean` | `SharedPreferences` + `StateFlow`. |
| `authToken()` | Получить JWT. | — | `String?` | Возвращает только непустой токен. |
| `accountPassword()` | Получить сохраненный пароль для экрана профиля. | — | `String` | `SharedPreferences`. |
| `accountId()` | Получить id аккаунта. | — | `Long?` | `-1` считается отсутствием id. |
| `saveSession(token, accountId, password)` | Сохранить сессию после входа/регистрации. | токен, id, пароль | `Unit` | Обновляет `loggedIn=true`. |
| `savePassword(password)` | Обновить локально сохраненный пароль. | `String` | `Unit` | Не меняет JWT. |
| `clearSession()` | Выйти из аккаунта. | — | `Unit` | Удаляет токен, id и пароль, обновляет `loggedIn=false`. |

### 6.2. `AccountRepository`

| Метод | Назначение | Вход | Выход | Побочные эффекты |
| --- | --- | --- | --- | --- |
| `login(email, password)` | Выполнить вход. | email, password | `AuthResponse` | Вызывает backend, сохраняет аккаунт в Room. |
| `register(email, name, password)` | Зарегистрировать аккаунт. | email, имя, password | `AuthResponse` | Вызывает backend, сохраняет аккаунт в Room. |
| `getLocalAccount()` | Получить локальную запись аккаунта текущей сессии. | — | `AccountEntity?` | Читает Room. |
| `updateLocalName(name)` | Обновить имя аккаунта. | `String` | `Unit` | Вызывает backend и обновляет Room. |
| `updatePassword(password)` | Обновить пароль аккаунта. | `String` | `Unit` | Вызывает backend; сохранение пароля в настройках выполняет вызывающий код. |
| `stats()` | Получить статистику аккаунта. | — | `AccountStats` | Требует `accountId` в сессии. |
| `deleteCurrentAccount()` | Удалить текущий аккаунт и локальные данные. | — | `Unit` | Пытается удалить аккаунт на backend, затем удаляет локальные избранные, обращения, радиотесты, аккаунт и файлы мест. |

### 6.3. `SavedPlacesRepository`

| Метод | Назначение | Вход | Выход | Правила |
| --- | --- | --- | --- | --- |
| `savePlace(place)` | Сохранить место в избранное. | `Place` | `Unit` | Если есть аккаунт, пробует отправить город на backend; затем записывает место в файл, если оно еще не сохранено. |
| `getSavedPlaces()` | Получить сохраненные места. | — | `List<Place>` | При наличии аккаунта сначала синхронизирует backend в локальные файлы; затем использует memory cache или файлы. |
| `getSavedPlace(coords)` | Найти сохраненное место по координатам. | `Coordinates` | `Place?` | Сравнивает координаты. |
| `deletePlace(place)` | Удалить место. | `Place` | `Unit` | При наличии аккаунта ищет соответствующий remote favorite и удаляет его; затем удаляет локальный файл и кэш. |
| `deletePlacesForAccount(accountId)` | Удалить локальные файлы мест аккаунта. | `Long` | `Unit` | Очищает директорию `places/{accountId}` и сбрасывает memory cache текущего аккаунта. |
| `syncFromBackendIfPossible()` | Синхронизировать remote favorites в файлы. | — | `Unit` | Приватный метод; ошибки backend подавляются через `runCatching`. |
| `replaceLocalCache(places)` | Перезаписать локальный файловый кэш. | `List<Place>` | `Unit` | Удаляет старые файлы и записывает актуальные. |

### 6.4. `FavoritesSyncRepository`

| Метод | Назначение | Вход | Выход | Правила |
| --- | --- | --- | --- | --- |
| `sync()` | Синхронизировать избранные города backend → Room. | — | `List<FavoriteCityEntity>` | Требует `accountId`; получает список через API, сохраняет в Room и возвращает локальные записи. |

### 6.5. `ForecastRepository`

| Метод | Назначение | Вход | Выход | Правила |
| --- | --- | --- | --- | --- |
| `forecast(coords, units, updatePolicy)` | Получить прогноз по координатам. | координаты, единицы измерения, политика обновления | `Forecast?` | Для одних координат использует `Mutex`, чтобы не выполнять параллельные загрузки; при необходимости скачивает новые данные, сохраняет кэш и конвертирует в доменную модель. |
| `shouldUpdate(data, updatePolicy)` | Определить необходимость обновления. | кэш, политика | `Boolean` | `Static` не обновляет; `Eager` обновляет кэш старше 1 часа; `Frugal` — старше 6 часов; отсутствие кэша всегда требует загрузки. |

### 6.6. `ForecastDataDownloader`

| Метод | Назначение | Вход | Выход | Правила |
| --- | --- | --- | --- | --- |
| `downloadForecast(coords)` | Скачать и преобразовать прогноз Open-Meteo. | `Coordinates` | `ForecastData?` | Возвращает `null`, если сеть недоступна или HTTP-код не `200`. |
| `downloadForecastJson(coords)` | Выполнить HTTPS-запрос. | `Coordinates` | `String?` | Устанавливает `User-Agent`, `connectTimeout=10000`, `readTimeout=10000`. |
| `convertJsonToData(jsonString)` | Преобразовать JSON в `ForecastData`. | `String` | `ForecastData` | Парсит hourly/daily массивы, отбрасывает неполные последние дни и sunrise/sunset-заглушки 1970 года. |
| `openMeteoUrl(coords)` | Сформировать URL прогноза. | `Coordinates` | `String` | Запрашивает hourly weather, daily sunrise/sunset, `timezone=auto`, `past_days=1`, скорость ветра в м/с. |
| `formatCoordinate(value)` | Округлить координату для URL. | `Double` | `String` | Формат `%.2f` с `Locale.ROOT`. |

### 6.7. `SearchPlaces`

| Метод | Назначение | Вход | Выход | Правила |
| --- | --- | --- | --- | --- |
| `invoke(query, languageCode)` | Найти места через Open-Meteo Geocoding API. | поисковая строка, код языка | `List<Place>?` | Возвращает `null` при сетевой ошибке; преобразует результаты в доменную модель `Place`. |
| `downloadPlacesJson(query, languageCode)` | Скачать JSON геокодинга. | query, язык | `String?` | Выполняет сетевой GET с таймаутами и `User-Agent`. |
| `openMeteoUrl(query, languageCode)` | Сформировать URL геокодинга. | query, язык | `String` | Использует endpoint `/v1/search`, параметр `count=10`. |

### 6.8. `SelectedPlaceRepository`

| Метод | Назначение | Вход | Выход | Правила |
| --- | --- | --- | --- | --- |
| `selectPlace(place)` | Сохранить выбранное место текущего пользователя/гостя. | `Place` | `Unit` | Записывает JSON в `SharedPreferences`. |
| `getSelectedPlace()` | Получить выбранное место. | — | `Place?` | Возвращает `null`, если запись отсутствует. |
| `selectedPlaceKey()` | Получить ключ хранения. | — | `String` | Для аккаунта ключ содержит `accountId`, для гостя — `guest`. |
| `placeToJson(place)` / `jsonToPlace(value)` | Сериализация и десериализация места. | `Place` / `String` | `String` / `Place` | Сохраняет название, административные поля, страну, timezone и координаты. |

## 7. Требования к тестированию методов

| Группа методов | Обязательные проверки |
| --- | --- |
| Аутентификация | Успешная регистрация, повторный email, вход с неверным паролем, назначение роли admin по конфигурации. |
| Аккаунт | Получение профиля владельцем и администратором, запрет чужого доступа, изменение имени, изменение пароля, каскадное удаление. |
| Избранное | CRUD по owner account, поиск без учета регистра, запрет доступа к чужому `accountId`, удаление несуществующего `favoriteId`. |
| Радиотест | Расчет расстояния, классификация качества на границах, fallback погоды при ошибке Open-Meteo, сохранение истории. |
| Поддержка | Создание диалога, получение диалога, группировка admin summaries, отметка непрочитанных сообщений, удаление диалога. |
| Статистика | Корректные счетчики избранного, радиотестов и наличия обращения поддержки. |
| Android repositories | Сохранение сессии, синхронизация Room/файлов с backend, fallback при сетевых ошибках, политики обновления прогноза. |
