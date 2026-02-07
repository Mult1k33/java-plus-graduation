## ExploreWithMe

### Общая информация
Это микросервисная реализация приложения ExploreWithMe для поиска событий и участия в них.
Исходный монолит разделен на несколько независимых сервисов, взаимодействующих через API Gateway и Feign-клиенты.

**Технологический стек**: Java 21, Spring Boot, Spring Cloud(Eureka, Gateway, OpenFeign, Config), MapStruct, Lombok, Apache Maven, Docker, PostgreSQL, Postman.

## Основные сервисы
1. **event-service** - сервис управления событиями, категориями, подборками;
2. **user-service** - сервис управления пользователями.;
3. **request-service** - сервис управления запросами на участие в событии;
4. **comment-service** - сервис управления комментариями;
5. **stats-service** - сервис сбора и предоставления статистики;
6. **discovery-server** (Eureka) - регистрация и обнаружение сервисов;
7. **config-server** - централизованное хранение конфигураций для всех сервисов;
8. **gateway-server** - единая точка входа (API Gateway).

### Вспомогательный модуль
- **interaction-api** - общие DTO, Feign-клиенты, исключения, перечисления

### Конфигурация
Все настройки хранятся в **config-server**:
- `gateway-server.yml` - маршрутизация Gateway
- `user-service.yml` - настройки user-service
- `event-service.yml` - настройки event-service
- `request-service.yml` - настройки request-service
- `comment-service.yml` - настройки comment-service
- `stats-service.yml` - настройки stats-service

Приложение имеет общую базу данных. Каждый сервис имеет собственную schema.sql, обеспечивая изоляцию данных. 

## Внутренний API для взаимодействия сервисов
Для взаимодействия между микросервисами используются Feign Clients и Internal Endpoints:
- GET /internal/events/{eventId} - возвращает полное описание события;
- GET /internal/requests/event/{eventId}/count/{status} - возвращает количество заявок события с определенным статусом;
- POST /internal/requests/events/count - возвращает количество подтвержденных заявок для событий;
- GET /internal/users/{userId} - возвращает краткую информацию о пользователе.

## Внешний API
Взаимодействие с внешними запросами происходит через API Gateway, который перенаправляет внешние запросы к нужным сервисам.<br>
Примеры основных эндопинтов:<br>
### Публичные(доступны всем):
- GET /comments/events/{eventId} - поиск опубликованных комментариев к событию;
- GET /events - поиск событий;
- GET /categories - категории событий;
- GET //categories/{categoryId} - поиск категории по id;
- GET /compilations/{compId} - поиск подборки по id;
- 
### Приватные(доступны для авторизованных пользователей):
- POST /users/{userId}/comments - добавить комментарий к событию;
- PATCH /users/{userId}/comments/{commentId} - изменить комментарий;
- GET /users/{userId}/events - события пользователя;
- GET /users/{userId}/events/{eventId}/requests - все заявки на участие в событии;
- PATCH /users/{userId}/requests/{requestId}/cancel - отмена заявки на участие;

### Административные(доступны только администраторам):
- GET /admin/comments - поиск комментариев;
- DELETE /admin/comments/{commentId} - удалить комментарий;
- POST /admin/categories - создать категорию;
- PATCH /admin/compilations/{compId} - изменить подборку;
- GET /admin/events - расширенный поиск событий с фильтрами;
- POST /admin/users/ - создать пользователя;
- DELETE /admin/users/{userId} - удалить пользователя.

### Полная спецификация OpenAPI доступна в файлах:
1. [ewm-main-service-spec.json](./ewm-main-service-spec.json) - основная спецификация
2. [ewm-stat-service-spec.json](./ewm-stat-service-spec.json) - спецификация статистики