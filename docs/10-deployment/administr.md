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
