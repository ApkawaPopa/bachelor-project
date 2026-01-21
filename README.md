# АМИНА

**АМИНА** - это защищенный мессенджер с end-to-end шифрованием, построенный на современном стеке технологий. Приложение
обеспечивает безопасную передачу сообщений между пользователями с использованием криптографических алгоритмов.

## Архитектура проекта

Проект состоит из двух основных компонентов:

### Бэкенд (Backend)

- **Фреймворк:** Spring Boot 4.0.1
- **Язык:** Java 25
- **База данных:** PostgreSQL 18.1
- **Аутентификация:** JWT (JSON Web Tokens)
- **WebSocket:** STOMP over WebSocket для real-time сообщений
- **Шифрование:** Асимметричное (RSA-OAEP) и симметричное (AES-GCM) шифрование
- **Контейнеризация:** Docker

### Фронтенд (Frontend)

- **Фреймворк:** Vue.js 3
- **Сборщик:** Vite
- **WebSocket:** SockJS + STOMP.js
- **Криптография:** Web Crypto API
- **Стилизация:** CSS с Grid/Flexbox

## Основные возможности

1. **Безопасная регистрация и аутентификация**
    - Генерация RSA ключевых пар на клиенте
    - Шифрование приватного ключа с использованием пароля
    - JWT для HTTP аутентификации

2. **End-to-End Шифрование**
    - Асимметричное шифрование для передачи симметричных ключей
    - Симметричное шифрование (AES-GCM) для сообщений
    - Каждый чат имеет уникальный симметричный ключ

3. **Управление чатами**
    - Создание приватных и групповых чатов
    - Приглашение пользователей в чаты
    - Распределение зашифрованных симметричных ключей

4. **Real-time обмен сообщениями**
    - Мгновенная доставка сообщений через WebSocket
    - Подтверждения прочтения
    - История сообщений

5. **Безопасное хранение**
    - Пароли хэшируются с использованием BCrypt
    - Приватные ключи шифруются на клиенте
    - Сервер хранит только зашифрованные данные

## Требования

- **Docker** и **Docker Compose** для развертывания бэкенда и БД
- **Node.js** (версия ^20.19.0 или ≥22.12.0) для фронтенда
- Современный браузер с поддержкой Web Crypto API

## Быстрый старт

### 1. Клонирование репозитория

```bash
git clone https://github.com/ApkawaPopa/amina.git
cd amina
```

### 2. Настройка переменных окружения

Создайте файл `.env` в корневой папке проекта на основе `.env.example`:

```env
# PostgreSQL
POSTGRES_USER=amina_user
POSTGRES_PASSWORD=secure_password
POSTGRES_DB=amina_db
POSTGRES_PORT=5432

# Backend
BACKEND_ADDRESS=0.0.0.0
BACKEND_PORT=8080
BACKEND_SHOW_SQL=false
BACKEND_FORMAT_SQL=false
BACKEND_LOGGING_LEVEL=INFO
BACKEND_CERTIFICATE_PASSWORD=changeit
BACKEND_JWT_SECRET=your_jwt_secret_here
BACKEND_PASSWORD_SALT=your_password_salt_here
```

### 3. Запуск сервисов

```bash
docker-compose up -d
```

Эта команда запустит:

- PostgreSQL базу данных на порту 5432
- Spring Boot бэкенд на порту 8080 (HTTPS)

### 4. Запуск фронтенда

```bash
cd frontend
npm install
npm run dev
```

Фронтенд будет доступен по адресу: `https://localhost:3125`

### 5. Генерация SSL сертификатов (опционально)

Если нужны собственные SSL сертификаты:

```bash
# Генерация сертификата для backend
keytool -genkeypair -alias amina -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore backend/src/main/resources/keystore.p12 \
  -validity 3650 -storepass changeit

# Генерация сертификата для frontend
openssl req -x509 -newkey rsa:2048 -keyout frontend/localhost.key \
  -out frontend/localhost.crt -days 365 -nodes -subj "/CN=localhost"
openssl pkcs12 -export -out frontend/localhost.pfx \
  -inkey frontend/localhost.key -in frontend/localhost.crt \
  -passout pass:changeit
```

## Структура проекта

```
amina/
├── backend/              # Spring Boot приложение
│   ├── src/main/java/   # Исходный код Java
│   ├── src/main/resources/ # Конфигурационные файлы
│   └── Dockerfile       # Docker конфигурация для бэкенда
├── frontend/            # Vue.js приложение
│   ├── src/            # Исходный код Vue компонентов
│   ├── public/         # Статические файлы
│   └── vite.config.js  # Конфигурация Vite
├── docker-compose.yml   # Docker Compose конфигурация
└── README.md           # Этот файл
```

## Безопасность

### Криптографический стек

1. **Генерация ключей:**
    - RSA-2048 для асимметричного шифрования
    - AES-256-GCM для симметричного шифрования

2. **Шифрование сообщений:**
   ```
   Каждое сообщение шифруется уникальным вектором инициализации
   версия (1 байт) + IV (12 байт) + зашифрованное содержимое
   ```

3. **Хранение ключей:**
    - Публичные ключи хранятся на сервере
    - Приватные ключи шифруются паролем пользователя и хранятся на клиенте
    - Симметричные ключи чатов шифруются публичными ключами участников

### Защита передачи данных

- Все HTTP запросы используют HTTPS
- WebSocket соединения защищены SSL/TLS
- JWT токены для аутентификации API запросов

## API документация

Бэкенд предоставляет следующие основные эндпоинты:

### Аутентификация

- `POST /api/v1/auth/register` - регистрация пользователя
- `POST /api/v1/auth/login` - вход в систему
- `POST /api/v1/auth/ws-token` - получение токена для WebSocket

### Чаты

- `POST /api/v1/chat` - создание чата
- `GET /api/v1/chat/{id}` - получение информации о чате
- `GET /api/v1/chat/{id}/users` - список пользователей чата
- `GET /api/v1/chat/{id}/key` - получение зашифрованного симметричного ключа

### Сообщения

- `GET /api/v1/chat/{chatId}/message` - список сообщений чата
- `PATCH /api/v1/chat/{chatId}/message/{id}` - обновление сообщения

### Пользователи

- `GET /api/v1/user/chats` - список чатов пользователя
- `POST /api/v1/user/keys` - получение публичных ключей пользователей

### WebSocket эндпоинты

- `/ws` - основной WebSocket эндпоинт
- `/app/chat/{id}/message/post` - отправка сообщения
- `/app/chat/{chatId}/message/{messageId}/receive` - подтверждение прочтения
- `/topic/chat/{id}` - подписка на сообщения чата
- `/topic/chat/{id}/received` - подписка на подтверждения прочтения

## Разработка

### Backend разработка

```bash
cd backend
# Установите Maven и JDK 25
mvn spring-boot:run
```

### Frontend разработка

```bash
cd frontend
npm install
npm run dev
```

### Тестирование

Для тестирования API можно использовать файл `backend/src/main/resources/requests.http` с HTTP клиентом IntelliJ IDEA
или аналогичным инструментом.

## Мониторинг и логи

- Бэкенд логи доступны через `docker-compose logs backend`
- База данных логи доступны через `docker-compose logs db`
- Уровень логирования настраивается через переменную `BACKEND_LOGGING_LEVEL`

## Устранение неполадок

### Проблемы с SSL сертификатами

1. Убедитесь, что пароль в `KEYSTORE_PASSWORD` совпадает с паролем при генерации сертификата
2. Для разработки можно отключить SSL, изменив `server.ssl.enabled=false` в `application.properties`

### Проблемы с подключением к БД

1. Проверьте, что PostgreSQL запущен: `docker-compose ps`
2. Убедитесь, что переменные окружения корректно установлены

### Проблемы с WebSocket

1. Проверьте, что получен действительный WebSocket токен через `/api/v1/auth/ws-token`
2. Убедитесь, что фронтенд использует тот же адрес, что и бэкенд

---

**Важно:** Этот проект предназначен для образовательных целей.