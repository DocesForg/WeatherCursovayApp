# Руководство администратора Bura

## 1. Назначение

Документ описывает запуск и администрирование серверной части Bura, управление пользователями, ролями, обращениями поддержки и данными приложения. Сервер предоставляет REST API для Android-приложения и веб-панель администратора.

## 2. Архитектура

Проект состоит из двух модулей Gradle:

- `app` — Android-приложение Bura.
- `server` — Spring Boot API-сервер.

Сервер использует:

- Java 21;
- Spring Boot;
- Spring Security с JWT-аутентификацией;
- Spring Data JPA;
- PostgreSQL;
- OpenAPI/Swagger UI;
- Open-Meteo для погодных данных в расчёте радиосигнала.

Основные серверные функции:

- регистрация и вход пользователей;
- JWT-сессии;
- хранение избранных городов;
- хранение истории радиотестов;
- переписка пользователей с поддержкой;
- административная статистика;
- просмотр и изменение ролей пользователей;
- веб-панель поддержки `/admin/panel`.

## 3. Требования к окружению

- JDK 21.
- Docker и Docker Compose для локального запуска PostgreSQL и сервера.
- Доступ к сети для загрузки Gradle-зависимостей и обращения к Open-Meteo.
- Свободные порты:
  - `5432` для PostgreSQL;
  - `8080` для сервера.

## 4. Переменные окружения

Создайте файл `.env` в корне проекта на основе `.env.example`.

Минимальный набор переменных:

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
```

Рекомендуется дополнительно задать список администраторов:

```dotenv
APP_ADMIN_EMAILS=admin@example.com,owner@example.com
```

В приложении свойство читается как `app.admin.emails`. Для Spring Boot переменная окружения `APP_ADMIN_EMAILS` соответствует этому свойству.

### 4.1 Рекомендации по секретам

- Не используйте значение `change-me-to-at-least-32-characters` в продуктивной среде.
- Используйте случайный секрет JWT длиной не менее 32 символов.
- Храните `.env` вне системы контроля версий.
- Регулярно меняйте пароль PostgreSQL и JWT-секрет по регламенту безопасности.

## 5. Локальный запуск

### 5.1 Запуск через Docker Compose

1. Скопируйте пример окружения:

   ```bash
   cp .env.example .env
   ```

2. Отредактируйте `.env` и задайте безопасные значения.
3. Запустите инфраструктуру:

   ```bash
   docker compose up --build
   ```

4. Дождитесь, пока PostgreSQL пройдёт healthcheck, а сервер запустится на порту `8080`.

### 5.2 Остановка

```bash
docker compose down
```

Чтобы удалить том с данными PostgreSQL:

```bash
docker compose down -v
```

Используйте удаление тома только для тестового окружения, потому что это уничтожит базу данных.

## 6. Проверка работоспособности

После запуска доступны:

- API: `http://localhost:8080/api/...`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- веб-панель администратора: `http://localhost:8080/admin/panel`

Публичны только эндпоинты `/api/auth/**`, Swagger UI и OpenAPI. Остальные запросы требуют JWT-токен.

## 7. Создание администратора

Роль администратора назначается по email.

1. Добавьте email администратора в переменную `APP_ADMIN_EMAILS`.
2. Перезапустите сервер.
3. Зарегистрируйте пользователя с этим email или выполните вход существующим пользователем.
4. При регистрации или следующем входе сервер назначит роль `ADMIN`, если email есть в списке администраторов.

Проверить роль можно через ответ авторизации или через административный список аккаунтов.

## 8. Аутентификация и авторизация

### 8.1 Регистрация

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "displayName": "User",
  "password": "secret"
}
```

Ответ содержит JWT-токен и данные аккаунта.

### 8.2 Вход

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "secret"
}
```

### 8.3 Использование токена

Для защищённых запросов передавайте заголовок:

```http
Authorization: Bearer <token>
```

Пользователь может обращаться только к данным своего аккаунта. Администратор имеет доступ к административным эндпоинтам и может обращаться к данным пользователей согласно правилам доступа.

## 9. Управление пользователями

### 9.1 Данные аккаунта пользователя

- `GET /api/accounts/{accountId}` — получить профиль.
- `PATCH /api/accounts/{accountId}/name` — изменить отображаемое имя.
- `PATCH /api/accounts/{accountId}/password` — изменить пароль.
- `DELETE /api/accounts/{accountId}` — удалить аккаунт.

Удаление аккаунта также удаляет связанные избранные города, радиотесты и сообщения поддержки.

Для удаления администратора требуется параметр:

```http
DELETE /api/accounts/{accountId}?allowAdminDelete=true
```

### 9.2 Административный список аккаунтов

```http
GET /api/admin/accounts
Authorization: Bearer <admin-token>
```

Ответ содержит список аккаунтов с `id`, `email`, `displayName` и `role`.

### 9.3 Изменение роли

```http
PATCH /api/admin/accounts/{accountId}/role
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "role": "ADMIN"
}
```

Поддерживаемые роли: `USER`, `ADMIN`.

> Важно: если email пользователя не входит в `APP_ADMIN_EMAILS`, при следующем входе роль может быть пересчитана на основании конфигурации. Для постоянного администратора добавьте email в переменную окружения.

## 10. Панель администратора

Веб-панель доступна по адресу:

```text
http://localhost:8080/admin/panel
```

Для доступа необходим JWT администратора. Если панель запрашивает токен, используйте токен, полученный через `/api/auth/login`.

В панели можно:

- просматривать диалоги поддержки;
- видеть пользователей с непрочитанными сообщениями;
- открывать переписку конкретного пользователя;
- отправлять ответ от имени администратора.

## 11. Поддержка пользователей

### 11.1 Пользовательские эндпоинты

- `POST /api/accounts/{accountId}/support/messages` — отправить сообщение пользователя.
- `GET /api/accounts/{accountId}/support/messages` — получить переписку пользователя.
- `DELETE /api/accounts/{accountId}/support/messages` — удалить переписку пользователя.

Сообщение пользователя содержит email, имя и текст. Сервер сохраняет адрес пересылки из `APP_SUPPORT_MAILBOX`.

### 11.2 Административные эндпоинты поддержки

- `GET /api/admin/support/conversations` — список диалогов.
- `GET /api/admin/support/accounts/{accountId}/messages` — сообщения конкретного аккаунта; при открытии пользовательские непрочитанные сообщения помечаются прочитанными администратором.
- `POST /api/admin/support/accounts/{accountId}/messages` — ответ администратора.

Пример ответа администратора:

```http
POST /api/admin/support/accounts/1/messages
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "message": "Здравствуйте! Мы проверим ваш вопрос."
}
```

## 12. Избранные города

Эндпоинты избранного:

- `GET /api/accounts/{accountId}/favorites` — список избранных городов.
- `GET /api/accounts/{accountId}/favorites/search?city=...` — поиск по избранным.
- `POST /api/accounts/{accountId}/favorites` — добавить город.
- `PUT /api/accounts/{accountId}/favorites/{favoriteId}` — изменить город.
- `DELETE /api/accounts/{accountId}/favorites/{favoriteId}` — удалить город.

Пример добавления:

```http
POST /api/accounts/1/favorites
Authorization: Bearer <token>
Content-Type: application/json

{
  "cityName": "Moscow",
  "latitude": 55.7558,
  "longitude": 37.6173
}
```

## 13. Радиотесты

Эндпоинты радиотестов:

- `POST /api/accounts/{accountId}/radio-tests` — запустить расчёт.
- `GET /api/accounts/{accountId}/radio-tests` — получить историю.

Для расчёта сервер:

1. вычисляет расстояние между двумя точками;
2. получает текущую погоду через Open-Meteo;
3. рассчитывает потери, задержку, примерную скорость и качество;
4. сохраняет результат в историю пользователя.

Пример запроса:

```http
POST /api/accounts/1/radio-tests
Authorization: Bearer <token>
Content-Type: application/json

{
  "cityA": "Moscow",
  "cityB": "Saint Petersburg",
  "latitudeA": 55.7558,
  "longitudeA": 37.6173,
  "latitudeB": 59.9311,
  "longitudeB": 30.3609,
  "frequencyMhz": 2400
}
```

## 14. Статистика

### 14.1 Пользовательская статистика

```http
GET /api/accounts/{accountId}/stats
Authorization: Bearer <token>
```

Ответ содержит:

- количество избранных городов;
- количество радиотестов;
- наличие обращений в поддержку.

### 14.2 Административная статистика

```http
GET /api/admin/dashboard
Authorization: Bearer <admin-token>
```

Ответ содержит:

- общее количество пользователей;
- количество администраторов;
- количество избранных записей;
- количество радиотестов;
- количество аккаунтов, обращавшихся в поддержку.

## 15. Swagger и диагностика API

Swagger UI доступен без JWT по адресу:

```text
http://localhost:8080/swagger-ui.html
```

Используйте Swagger для проверки структуры запросов и ответов. Для защищённых эндпоинтов добавьте JWT-токен в авторизацию Swagger UI, если интерфейс предоставляет такую возможность, или выполняйте запросы через `curl`, Postman либо аналогичный инструмент.

## 16. Резервное копирование и восстановление

Данные хранятся в PostgreSQL. Для Docker Compose используется том `postgres_data`.

### 16.1 Резервная копия

Пример команды для локального окружения:

```bash
docker exec bura-postgres pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB" > bura-backup.sql
```

### 16.2 Восстановление

```bash
cat bura-backup.sql | docker exec -i bura-postgres psql -U "$POSTGRES_USER" "$POSTGRES_DB"
```

Перед восстановлением убедитесь, что база находится в ожидаемом состоянии, а резервная копия соответствует версии приложения.

## 17. Обновление сервера

1. Сделайте резервную копию базы данных.
2. Получите новую версию кода.
3. Проверьте `.env` и новые переменные окружения.
4. Пересоберите и запустите сервисы:

   ```bash
   docker compose up --build -d
   ```

5. Проверьте логи сервера и доступность API.
6. Проверьте вход пользователя и администратора.

## 18. Логи и устранение неполадок

### 18.1 Просмотр логов

```bash
docker compose logs -f server
```

```bash
docker compose logs -f postgres
```

### 18.2 Сервер не запускается

Проверьте:

- заполнен ли `.env`;
- доступен ли порт `8080`;
- прошёл ли PostgreSQL healthcheck;
- совпадают ли `SPRING_DATASOURCE_*` с настройками PostgreSQL;
- достаточно ли длинный `APP_JWT_SECRET`.

### 18.3 Пользователь не получает роль администратора

Проверьте:

- есть ли email в `APP_ADMIN_EMAILS`;
- нет ли лишних пробелов или ошибок регистра;
- был ли сервер перезапущен после изменения `.env`;
- выполнил ли пользователь вход после изменения конфигурации.

### 18.4 Ошибки 401 или 403

- `401 Unauthorized` обычно означает отсутствующий, истёкший или неверный JWT.
- `403 Forbidden` обычно означает недостаточную роль или попытку доступа к чужому аккаунту.

### 18.5 Радиотесты возвращают неточные погодные поправки

Если Open-Meteo недоступен, сервер использует значения погоды по умолчанию. Проверьте интернет-доступ контейнера `server` и DNS.

## 19. Безопасность

- Используйте HTTPS перед сервером в продуктивной среде.
- Не публикуйте `.env` и секреты.
- Ограничьте доступ к PostgreSQL извне.
- Регулярно обновляйте базовые Docker-образы и зависимости.
- Ограничьте список `APP_ADMIN_EMAILS` минимально необходимыми адресами.
- Удаляйте неактуальные администраторские аккаунты.
- Делайте резервные копии перед обновлениями и миграциями.
