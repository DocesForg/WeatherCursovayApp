```PlantUML
@startuml BuraUseCaseDiagram
left to right direction
skinparam packageStyle rectangle
skinparam shadowing false
skinparam actorStyle awesome
skinparam usecase {
  BackgroundColor #F8FBFF
  BorderColor #3B6EA8
  ArrowColor #3B6EA8
}

actor "Гость" as Guest
actor "Пользователь" as User
actor "Администратор" as Admin
actor "Open-Meteo\nAPI" as OpenMeteo
actor "Bura Backend" as Backend

User --|> Guest
Admin --|> User

rectangle "Мобильное приложение Bura" as MobileApp {
  usecase "Зарегистрироваться" as UC_Register
  usecase "Войти" as UC_Login
  usecase "Выбрать город" as UC_SelectPlace
  usecase "Найти город" as UC_SearchPlace
  usecase "Просмотреть сводку\nпогоды" as UC_ViewSummary
  usecase "Просмотреть почасовые\nи дневные графики" as UC_ViewGraphs
  usecase "Получить прогноз" as UC_DownloadForecast
  usecase "Работать с кэшем\nи офлайн-данными" as UC_OfflineCache
  usecase "Управлять избранными\nгородами" as UC_Favorites
  usecase "Настроить тему\nи единицы измерения" as UC_Settings
  usecase "Управлять профилем" as UC_Profile
  usecase "Просмотреть статистику\nаккаунта" as UC_Stats
  usecase "Написать в поддержку" as UC_Support
  usecase "Удалить диалог\nподдержки" as UC_DeleteSupport
  usecase "Рассчитать качество\nрадиосигнала" as UC_RadioTest
  usecase "Просмотреть историю\nрадиотестов" as UC_RadioHistory
  usecase "Выйти из аккаунта" as UC_Logout
}

rectangle "Сервер Bura" as Server {
  usecase "Выдать JWT\nи сохранить аккаунт" as UC_AuthBackend
  usecase "Хранить избранное" as UC_FavoritesBackend
  usecase "Хранить обращения\nподдержки" as UC_SupportBackend
  usecase "Хранить радиотесты" as UC_RadioBackend
  usecase "Считать статистику\nпользователя" as UC_StatsBackend
  usecase "Просмотреть dashboard" as UC_AdminDashboard
  usecase "Управлять аккаунтами\nи ролями" as UC_AdminAccounts
  usecase "Обрабатывать обращения\nпользователей" as UC_AdminSupport
}

Guest --> UC_Register
Guest --> UC_Login
User --> UC_SelectPlace
User --> UC_ViewSummary
User --> UC_ViewGraphs
User --> UC_Favorites
User --> UC_Settings
User --> UC_Profile
User --> UC_Stats
User --> UC_Support
User --> UC_RadioTest
User --> UC_RadioHistory
User --> UC_Logout
Admin --> UC_AdminDashboard
Admin --> UC_AdminAccounts
Admin --> UC_AdminSupport

UC_SelectPlace .> UC_SearchPlace : <<include>>
UC_ViewSummary .> UC_DownloadForecast : <<include>>
UC_ViewGraphs .> UC_DownloadForecast : <<include>>
UC_ViewSummary .> UC_OfflineCache : <<extend>>
UC_ViewGraphs .> UC_OfflineCache : <<extend>>
UC_Favorites .> UC_SelectPlace : <<extend>>
UC_Profile .> UC_Stats : <<include>>
UC_Support .> UC_DeleteSupport : <<extend>>

UC_Register --> UC_AuthBackend
UC_Login --> UC_AuthBackend
UC_Favorites --> UC_FavoritesBackend
UC_Profile --> UC_AuthBackend
UC_Stats --> UC_StatsBackend
UC_Support --> UC_SupportBackend
UC_DeleteSupport --> UC_SupportBackend
UC_RadioTest --> UC_RadioBackend
UC_RadioHistory --> UC_RadioBackend
UC_AdminDashboard --> UC_StatsBackend
UC_AdminDashboard --> UC_FavoritesBackend
UC_AdminDashboard --> UC_RadioBackend
UC_AdminDashboard --> UC_SupportBackend
UC_AdminAccounts --> UC_AuthBackend
UC_AdminSupport --> UC_SupportBackend

UC_SearchPlace --> OpenMeteo
UC_DownloadForecast --> OpenMeteo
UC_Register --> Backend
UC_Login --> Backend
UC_Favorites --> Backend
UC_Profile --> Backend
UC_Stats --> Backend
UC_Support --> Backend
UC_RadioTest --> Backend
UC_RadioHistory --> Backend

note right of UC_DownloadForecast
  Прогноз включает температуру,
  ощущения, осадки, вероятность
  осадков, ветер, порывы,
  влажность, давление, видимость,
  UV-индекс, восход и закат.
end note

note bottom of Server
  Серверные операции защищены JWT;
  пользователь работает только со
  своим accountId, администратор
  имеет доступ к административным API.
end note
@enduml
```