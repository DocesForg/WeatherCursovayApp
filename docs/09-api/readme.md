# Использование OpenAPI и Swagger в проекте Bura

Документ описывает, как в серверной части Bura публикуется OpenAPI-спецификация и как команда использует Swagger UI для просмотра и проверки REST API.

## 1. Назначение

В проекте OpenAPI и Swagger используются как единая точка входа для документации backend API:

- OpenAPI-спецификация описывает доступные HTTP-эндпоинты, параметры запросов, тела запросов и ответы сервера.
- Swagger UI предоставляет веб-интерфейс, в котором можно просматривать эту спецификацию и выполнять тестовые запросы к API.
- Документация генерируется автоматически на основе Spring MVC-контроллеров, DTO и Bean Validation-аннотаций.
- Для защищённых методов Swagger показывает общую JWT Bearer-схему авторизации, чтобы было понятно, какие запросы требуют токен.

## 2. Библиотека и подключение

Для генерации OpenAPI в backend-модуле используется SpringDoc OpenAPI:

```kotlin
implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
```

Зависимость подключена в `server/build.gradle.kts`. Стартер `springdoc-openapi-starter-webmvc-ui` автоматически:

1. сканирует Spring MVC-контроллеры;
2. строит OpenAPI JSON по маршрутам приложения;
3. подключает Swagger UI;
4. публикует служебные эндпоинты `/v3/api-docs/**` и `/swagger-ui/**`.

## 3. Основные адреса

При локальном запуске сервера на стандартном порту `8080` доступны следующие адреса:

| Назначение | URL |
| --- | --- |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

Путь к Swagger UI явно задан в профилях `dev` и `prod`:

```yaml
springdoc:
  swagger-ui:
    path: /swagger-ui.html
```

Если порт переопределён через переменную окружения `SERVER_PORT`, в URL нужно использовать фактический порт приложения.

## 4. Глобальная конфигурация OpenAPI

Глобальные настройки OpenAPI находятся в классе `OpenApiConfig`.

```java
@OpenAPIDefinition(
        info = @Info(title = "Bura API", version = "v1"),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
```

Эта конфигурация задаёт:

- название документации: `Bura API`;
- версию API: `v1`;
- схему безопасности `bearerAuth`;
- формат авторизации: HTTP Bearer Token с JWT.

За счёт глобального `SecurityRequirement` в OpenAPI-документации считается, что методы API по умолчанию требуют JWT. Исключения фактически задаются на уровне Spring Security.

## 5. Доступность Swagger и OpenAPI без авторизации

В `SecurityConfig` разрешён публичный доступ к Swagger UI, OpenAPI JSON и маршрутам аутентификации:

```java
.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api/auth/**").permitAll()
.anyRequest().authenticated()
```

Это означает:

- Swagger UI можно открыть без JWT;
- OpenAPI JSON можно получить без JWT;
- регистрация и вход доступны без JWT;
- остальные API-запросы требуют авторизованный доступ;
- права на пользовательские и административные операции дополнительно проверяются через `@PreAuthorize`.

## 6. Как формируется спецификация

Спецификация строится автоматически из кода серверного приложения:

- `@RestController` определяет класс как источник REST-эндпоинтов;
- `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PatchMapping`, `@PutMapping`, `@DeleteMapping` задают URL, HTTP-метод и структуру маршрута;
- `@PathVariable` и `@RequestParam` становятся параметрами операции;
- `@RequestBody` становится телом запроса;
- Java records и DTO-классы становятся схемами данных;
- `@Valid`, `@NotBlank`, `@Email` и другие validation-аннотации уточняют требования к входным данным;
- `@PreAuthorize` не заменяет OpenAPI-описание, но показывает в коде фактические правила доступа к операциям.

Отдельные аннотации `@Operation`, `@Tag` и `@ApiResponse` сейчас в контроллерах не используются. Поэтому описания операций в Swagger UI в основном выводятся из имён методов, маршрутов, типов параметров и DTO. При необходимости детализации документации можно добавить эти аннотации в контроллеры.

## 7. Группы API, которые отображаются в Swagger UI

SpringDoc собирает все контроллеры backend-модуля. Основные группы маршрутов:

### 7.1. Аутентификация и аккаунт

Контроллер: `AccountController`.

| Метод | Путь | Назначение | Авторизация |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | Регистрация пользователя | Не требуется |
| `POST` | `/api/auth/login` | Вход и получение JWT | Не требуется |
| `GET` | `/api/accounts/{accountId}` | Получение данных аккаунта | JWT, доступ к своему аккаунту или права администратора |
| `PATCH` | `/api/accounts/{accountId}/name` | Изменение имени | JWT, доступ к аккаунту |
| `PATCH` | `/api/accounts/{accountId}/password` | Изменение пароля | JWT, доступ к аккаунту |
| `DELETE` | `/api/accounts/{accountId}` | Удаление аккаунта | JWT, доступ к аккаунту |

### 7.2. Избранные города

Контроллер: `FavoriteCityController`.

| Метод | Путь | Назначение |
| --- | --- | --- |
| `GET` | `/api/accounts/{accountId}/favorites` | Список избранных городов |
| `GET` | `/api/accounts/{accountId}/favorites/search?city=...` | Поиск избранного города |
| `POST` | `/api/accounts/{accountId}/favorites` | Добавление города в избранное |
| `PUT` | `/api/accounts/{accountId}/favorites/{favoriteId}` | Обновление избранного города |
| `DELETE` | `/api/accounts/{accountId}/favorites/{favoriteId}` | Удаление избранного города |

Все операции требуют JWT и доступа к указанному аккаунту.

### 7.3. Тест радиосигнала

Контроллер: `RadioSignalController`.

| Метод | Путь | Назначение |
| --- | --- | --- |
| `GET` | `/api/accounts/{accountId}/radio-tests` | История тестов радиосигнала |
| `POST` | `/api/accounts/{accountId}/radio-tests` | Расчёт и сохранение нового теста |

Операции требуют JWT и доступа к указанному аккаунту.

### 7.4. Пользовательская статистика

Контроллер: `UserStatsController`.

| Метод | Путь | Назначение |
| --- | --- | --- |
| `GET` | `/api/accounts/{accountId}/stats` | Количество избранных городов, тестов радиосигнала и обращений в поддержку |

Операция требует JWT и доступа к указанному аккаунту.

### 7.5. Поддержка

Контроллер: `SupportController`.

| Метод | Путь | Назначение | Авторизация |
| --- | --- | --- | --- |
| `POST` | `/api/accounts/{accountId}/support/messages` | Отправка сообщения пользователя | JWT, доступ к аккаунту |
| `GET` | `/api/accounts/{accountId}/support/messages` | Просмотр своей переписки | JWT, доступ к аккаунту |
| `DELETE` | `/api/accounts/{accountId}/support/messages` | Удаление своей переписки | JWT, доступ к аккаунту |
| `GET` | `/api/admin/support/conversations` | Список обращений пользователей | JWT, роль `ADMIN` |
| `GET` | `/api/admin/support/accounts/{accountId}/messages` | Просмотр переписки пользователя администратором | JWT, роль `ADMIN` |
| `POST` | `/api/admin/support/accounts/{accountId}/messages` | Ответ администратора пользователю | JWT, роль `ADMIN` |

### 7.6. Администрирование

Контроллер: `AdminController`.

| Метод | Путь | Назначение |
| --- | --- | --- |
| `GET` | `/api/admin/dashboard` | Сводные показатели для админ-панели |
| `GET` | `/api/admin/accounts` | Список аккаунтов |
| `PATCH` | `/api/admin/accounts/{accountId}/role` | Изменение роли аккаунта |
| `GET` | `/admin/panel` | HTML-страница административной панели |

Методы контроллера защищены требованием роли `ADMIN`.

## 8. Работа с JWT в Swagger UI

Типовой сценарий проверки защищённых эндпоинтов:

1. Открыть `http://localhost:8080/swagger-ui.html`.
2. Выполнить `POST /api/auth/login` или `POST /api/auth/register`.
3. Скопировать JWT из ответа.
4. Нажать кнопку `Authorize` в Swagger UI.
5. Ввести токен в Bearer-схему авторизации. Если Swagger UI ожидает полный заголовок, нужно указать `Bearer <token>`; если интерфейс сам добавляет префикс, достаточно вставить только значение токена.
6. Выполнять защищённые запросы от имени выбранного пользователя.

Для административных маршрутов JWT должен принадлежать аккаунту с ролью `ADMIN`.

## 9. Рекомендации по поддержке документации

Чтобы Swagger UI оставался полезным для разработки и тестирования, при добавлении новых API нужно:

1. размещать маршруты в контроллерах с явными `@*Mapping`-аннотациями;
2. использовать DTO или Java records для тел запросов и ответов;
3. добавлять Bean Validation-аннотации к обязательным и проверяемым полям;
4. не забывать про `@PreAuthorize` для защищённых операций;
5. при сложной бизнес-логике добавлять `@Operation`, `@ApiResponse` и `@Tag`, чтобы Swagger показывал человекочитаемые описания;
6. после изменения API открывать `/v3/api-docs` или Swagger UI и проверять, что новый маршрут появился в документации;
7. синхронизировать пользовательские инструкции и примеры запросов с фактическими маршрутами контроллеров.

## 10. Проверка доступности

Быстрая проверка после запуска backend-приложения:

```bash
curl http://localhost:8080/v3/api-docs
```

Если приложение запущено корректно, команда вернёт JSON-документ OpenAPI. Swagger UI можно проверить в браузере по адресу:

```text
http://localhost:8080/swagger-ui.html
```

Если OpenAPI JSON доступен, но защищённые запросы возвращают `401 Unauthorized`, это ожидаемое поведение для маршрутов, которые не входят в публичный список Spring Security.
