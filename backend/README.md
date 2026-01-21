# АМИНА - Backend

Backend часть защищенного мессенджера "АМИНА", реализованная на Spring Boot 4.0.1 с использованием современных Java
технологий.

## Технологический стек

- **Фреймворк:** Spring Boot 4.0.1
- **Язык:** Java 25
- **База данных:** PostgreSQL 18.1
- **Сборка:** Maven 3.9.12
- **Контейнеризация:** Docker
- **Аутентификация:** JWT (JSON Web Tokens)
- **Real-time:** STOMP over WebSocket
- **ORM:** Spring Data JPA (Hibernate)

## Основные функции

### Аутентификация и авторизация

- Регистрация и вход пользователей с JWT токенами
- Веб-токены для WebSocket подключений
- Защита эндпоинтов Spring Security
- Кастомная аутентификация для WebSocket соединений

### Управление чатами

- Создание приватных и групповых чатов
- Управление участниками чатов
- Распределение зашифрованных симметричных ключей
- Валидация доступа к чатам

### Сообщения

- Отправка сообщений через REST API и WebSocket
- Подтверждения прочтения сообщений
- История сообщений с пагинацией
- Редактирование сообщений

### WebSocket интеграция

- STOMP протокол поверх WebSocket
- Авторизация подписок и отправки сообщений
- Обработка ошибок через WebSocket
- Персистентные соединения с SockJS fallback

## Структура проекта

```
src/main/java/minchakov/arkadii/amina/
├── AminaBackendApplication.java          # Точка входа приложения
├── config/                               # Конфигурационные классы
│   ├── ApplicationConfig.java            # Общая конфигурация
│   ├── SecurityConfig.java              # Конфигурация Spring Security
│   └── StompConfig.java                 # Конфигурация WebSocket
├── controller/                           # REST контроллеры
│   ├── AuthController.java              # Аутентификация
│   ├── ChatController.java              # Управление чатами
│   ├── MessageController.java           # Управление сообщениями
│   ├── UserController.java              # Пользователи
│   ├── StompMessageController.java      # WebSocket контроллер
│   └── advice/                          # Обработка исключений
├── dto/                                  # Data Transfer Objects
├── exception/                            # Кастомные исключения
├── filter/                               # Фильтры (JWT)
├── interceptor/                          # Интерцепторы (WebSocket)
├── model/                                # JPA сущности
├── repository/                           # Spring Data репозитории
├── service/                              # Бизнес-логика
├── util/                                 # Утилиты
└── validator/                            # Валидаторы
```

## Модели данных

### Пользователь (User)

- ID, уникальное имя пользователя
- Хэш пароля (BCrypt)
- Публичный ключ и зашифрованный приватный ключ
- Список чатов пользователя

### Чат (Chat)

- ID, название чата
- Дата создания и обновления
- Участники чата (UserChat)
- Сообщения в чате

### Связь пользователь-чат (UserChat)

- Составной ключ (пользователь + чат)
- Зашифрованный симметричный ключ для этого пользователя
- Дата добавления в чат

### Сообщение (Message)

- ID, отправитель, чат
- Содержимое сообщения
- Дата создания и обновления
- Список получателей (подтверждения прочтения)

### Веб-токен (WebSocketToken)

- UUID токен для WebSocket подключения
- Связь с пользователем (один к одному)

## Запуск проекта

### Предварительные требования

- Java 25 JDK
- Maven 3.9+
- PostgreSQL 18.1+
- Docker и Docker Compose (опционально)

### Локальная разработка

1. **Настройка базы данных:**

```bash
# Создание базы данных PostgreSQL
createdb amina_db
# или используйте предоставленный db-init.sql
```

2. **Настройка переменных окружения:**
   Создайте файл `.env` в корне проекта:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/amina_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password
JWT_SECRET=your-super-secret-jwt-key
KEYSTORE_PASSWORD=changeit
```

3. **Генерация SSL сертификата:**

```bash
keytool -genkeypair -alias amina -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore src/main/resources/keystore.p12 \
  -validity 3650 -storepass changeit
```

4. **Запуск приложения:**

```bash
mvn spring-boot:run
```

### Docker сборка

1. **Сборка Docker образа:**

```bash
docker build -t amina-backend .
```

2. **Запуск через Docker Compose:**

```bash
docker-compose up backend
```

## Конфигурация

### Файлы конфигурации

1. **application.properties** - основные настройки Spring Boot
2. **custom.properties** - кастомные свойства (JWT секрет)
3. **db-init.sql** - SQL скрипт инициализации базы данных

### Настройки Spring Boot

```properties
# Основные настройки
spring.application.name=backend
server.ssl.enabled=true
server.ssl.key-store-type=PKCS12
server.ssl.key-store=classpath:keystore.p12
# Настройки базы данных
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.jpa.show-sql=${HIBERNATE_SHOW_SQL}
# WebSocket
spring.websocket.enabled=true
```

## API документация

### Аутентификация

#### Регистрация пользователя

```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "username",
  "passwordHash": "sha256-hash-of-password",
  "publicKey": "user-public-key",
  "encryptedPrivateKey": "encrypted-private-key"
}
```

#### Вход пользователя

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "username",
  "passwordHash": "sha256-hash-of-password"
}
```

#### Получение WebSocket токена

```http
POST /api/v1/auth/ws-token
Authorization: Bearer <jwt-token>
```

### Чаты

#### Создание чата

```http
POST /api/v1/chat
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "chatName": "Название чата",
  "usersDetails": [
    {
      "username": "user1",
      "encryptedSymmetricKey": "encrypted-key-1"
    },
    {
      "username": "user2",
      "encryptedSymmetricKey": "encrypted-key-2"
    }
  ]
}
```

#### Получение информации о чате

```http
GET /api/v1/chat/{id}
Authorization: Bearer <jwt-token>
```

#### Получение списка пользователей чата

```http
GET /api/v1/chat/{id}/users
Authorization: Bearer <jwt-token>
```

#### Получение симметричного ключа

```http
GET /api/v1/chat/{id}/key
Authorization: Bearer <jwt-token>
```

### Сообщения

#### Получение списка сообщений

```http
GET /api/v1/chat/{chatId}/message
Authorization: Bearer <jwt-token>
```

#### Обновление сообщения

```http
PATCH /api/v1/chat/{chatId}/message/{id}
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "content": "Новое содержание"
}
```

### Пользователи

#### Получение списка чатов пользователя

```http
GET /api/v1/user/chats
Authorization: Bearer <jwt-token>
```

#### Получение публичных ключей пользователей

```http
POST /api/v1/user/keys
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "usernames": [
    {"username": "user1"},
    {"username": "user2"}
  ]
}
```

### WebSocket эндпоинты

#### Подключение к WebSocket

```
/ws?token=<websocket-token>
```

#### Отправка сообщения

```
/app/chat/{id}/message/post
```

#### Подтверждение прочтения

```
/app/chat/{chatId}/message/{messageId}/receive
```

#### Подписка на сообщения чата

```
/topic/chat/{id}
```

#### Подписка на подтверждения прочтения

```
/topic/chat/{id}/received
```

#### Обработка ошибок

```
/user/queue/error
```

## Безопасность

### JWT аутентификация

- Токены подписываются с использованием HMAC256
- Срок жизни токена: 31 день
- Токен передается в заголовке `Authorization: Bearer <token>`

### WebSocket безопасность

- Одноразовые UUID токены для подключения
- Валидация токенов перед установкой соединения
- Авторизация подписок и отправки сообщений

### CORS настройки

- Разрешенные методы: GET, POST, PUT, PATCH, DELETE
- Разрешенные заголовки: все
- Поддержка credentials
- Разрешенные origins: все (для разработки)

### Защита данных

- BCrypt для хэширования паролей
- Все соединения используют HTTPS
- Защита от CSRF атак (отключена для WebSocket)
- Stateless аутентификация

## WebSocket архитектура

### Конфигурация STOMP

- Брокер сообщений: простой in-memory брокер
- Префикс приложения: `/app`
- Префикс пользователя: `/user`
- Префиксы тем: `/topic`, `/queue`

### Обработка сообщений

1. **preSend интерцептор** - авторизация STOMP команд
2. **ChannelInterceptor** - проверка доступа к чатам
3. **MessageMapping** - обработка бизнес-логики
4. **SendTo/SendToUser** - маршрутизация ответов

### Авторизация WebSocket

1. Подписка на темы проверяет доступ к чату
2. Отправка сообщений проверяет права отправителя
3. Подтверждение прочтения валидирует получателя

## База данных

### Схема базы данных

```sql
-- Пользователи
CREATE TABLE "user"
(
    id                    SERIAL PRIMARY KEY,
    username              VARCHAR UNIQUE NOT NULL,
    password_hash         VARCHAR        NOT NULL,
    public_key            VARCHAR        NOT NULL,
    encrypted_private_key VARCHAR        NOT NULL,
    created_at            TIMESTAMP      NOT NULL,
    updated_at            TIMESTAMP      NOT NULL
);

-- Чаты
CREATE TABLE chat
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR   NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Связь пользователь-чат
CREATE TABLE user_chat
(
    user_id                 INTEGER REFERENCES "user" (id) ON DELETE CASCADE,
    chat_id                 INTEGER REFERENCES chat (id) ON DELETE CASCADE,
    encrypted_symmetric_key VARCHAR   NOT NULL,
    created_at              TIMESTAMP NOT NULL,
    PRIMARY KEY (user_id, chat_id)
);

-- Сообщения
CREATE TABLE message
(
    id         SERIAL PRIMARY KEY,
    sender_id  INTEGER REFERENCES "user" (id) ON DELETE CASCADE NOT NULL,
    chat_id    INTEGER REFERENCES chat (id) ON DELETE CASCADE   NOT NULL,
    content    VARCHAR                                          NOT NULL,
    created_at TIMESTAMP                                        NOT NULL,
    updated_at TIMESTAMP                                        NOT NULL
);

-- Вебсокет-токены
CREATE TABLE web_socket_token
(
    id      UUID PRIMARY KEY,
    user_id INTEGER UNIQUE REFERENCES "user" (id) ON DELETE CASCADE NOT NULL
);

-- Получатели сообщений
CREATE TABLE message_receiver
(
    message_id  INTEGER REFERENCES message (id) ON DELETE CASCADE NOT NULL,
    receiver_id INTEGER REFERENCES "user" (id) ON DELETE CASCADE  NOT NULL,
    created_at  TIMESTAMP                                         NOT NULL,
    PRIMARY KEY (message_id, receiver_id)
);
```

### Индексы

```sql
-- Для быстрого поиска сообщений в чате
CREATE INDEX ON message (chat_id, created_at ASC);
CREATE INDEX ON message (chat_id, created_at DESC);
```

## Обработка ошибок

### Глобальная обработка исключений

- `@RestControllerAdvice` - централизованная обработка ошибок
- `@ExceptionHandler` - обработка специфичных исключений
- `@MessageExceptionHandler` - обработка ошибок WebSocket

### Типы исключений

- `AppException` - базовое исключение приложения
- `BadRequestException` - ошибка валидации (400)
- `NotFoundException` - ресурс не найден (404)
- `InternalServerErrorException` - внутренняя ошибка (500)
- `JwtAuthenticationException` - ошибка аутентификации JWT

### Формат ответа об ошибке

```json
{
  "code": 400,
  "message": "Описание ошибки",
  "data": null,
  "timestamp": "2024-01-21T10:30:00.000"
}
```

## Валидация

### Аннотации валидации

- `@NotBlank`, `@NotEmpty` - проверка пустых значений
- `@Length` - проверка длины строки
- `@UsernameConstraint` - уникальность имени пользователя
- `@ChatCreateUsernameConstraint` - существование пользователя

### Кастомные валидаторы

- `RegisterDTOUsernameValidator` - проверка уникальности имени
- `ChatCreateUsernameValidator` - проверка существования пользователя
- `ChatCreateOwnUsernameValidator` - проверка участия в чате
- `GetUsersKeysUsernameValidator` - проверка существования пользователя

## Тестирование API

### Использование HTTP-клиента

Файл `requests.http` содержит примеры запросов для тестирования:

```http
### Регистрация
POST https://localhost:8080/api/v1/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "passwordHash": "hash",
  "publicKey": "key",
  "encryptedPrivateKey": "encrypted"
}
```

### Переменные окружения

Файл `http-client.env.json` содержит переменные для тестирования:

```json
{
  "dev": {
    "jwt": "your-jwt-token",
    "chatId": "1",
    "messageId": "1",
    "host": "localhost",
    "port": "8080"
  }
}
```

## Мониторинг и логирование

### Уровни логирования

Настраиваются через переменную `LOGGING_LEVEL`:

- `DEBUG` - подробные логи для разработки
- `INFO` - основная информация о работе приложения
- `WARN` - предупреждения
- `ERROR` - ошибки

### Логи SQL запросов

- `HIBERNATE_SHOW_SQL` - показывать SQL запросы
- `HIBERNATE_FORMAT_SQL` - форматировать SQL запросы

## Производительность

### Оптимизации базы данных

- Индексы на часто используемых полях
- Эффективные запросы через Spring Data JPA
- Пагинация для списков сообщений

### WebSocket

- In-memory брокер сообщений
- Эффективная маршрутизация сообщений
- Минимальная задержка при доставке

### Кэширование

- Spring Cache для часто запрашиваемых данных
- Кэширование результатов валидации
- Оптимизация повторяющихся запросов

## Развертывание

### Docker образ

```dockerfile
FROM maven:3.9.12-eclipse-temurin-25-noble AS builder
# Многостадийная сборка
FROM eclipse-temurin:25.0.1_8-jre-alpine-3.23
# Минимальный JRE образ
```

### Переменные окружения Docker

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/amina_db
SPRING_DATASOURCE_USERNAME=amina_user
SPRING_DATASOURCE_PASSWORD=secure_password
JWT_SECRET=your-jwt-secret
KEYSTORE_PASSWORD=changeit
```

## Устранение неполадок

### Распространенные проблемы

1. **Ошибка подключения к базе данных:**
    - Проверьте параметры подключения
    - Убедитесь, что PostgreSQL запущен
    - Проверьте права доступа пользователя

2. **Ошибки SSL:**
    - Убедитесь, что файл keystore.p12 существует
    - Проверьте пароль keystore
    - Проверьте формат сертификата

3. **Ошибки WebSocket:**
    - Проверьте токен WebSocket
    - Убедитесь, что WebSocket включен
    - Проверьте настройки прокси

4. **Ошибки JWT:**
    - Проверьте JWT секрет
    - Убедитесь, что токен не истек
    - Проверьте формат токена

### Логи для отладки

```bash
# Просмотр логов Docker контейнера
docker-compose logs backend

# Логи Spring Boot с подробной информацией
export LOGGING_LEVEL=DEBUG
```

## Безопасность в production

### Рекомендации

1. Используйте сильный JWT секрет
2. Настройте HTTPS с доверенным сертификатом
3. Ограничьте CORS политики
4. Используйте брандмауэр для защиты портов
5. Регулярно обновляйте зависимости
6. Настройте мониторинг и алерты

### Конфигурация безопасности

- Настройте политики CORS для production
- Используйте отдельные сертификаты для каждого окружения
- Настройте rate limiting для API
- Включите защиту от DDoS атак

---

**Важно:** Этот бэкенд является частью системы end-to-end шифрования. Сервер никогда не имеет доступа к незашифрованным
сообщениям или приватным ключам пользователей. Все криптографические операции выполняются на клиенте.