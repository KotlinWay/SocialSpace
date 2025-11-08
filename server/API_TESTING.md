# API Testing Guide - User Endpoints

## Предварительные требования

1. Запустите PostgreSQL:
```bash
# Убедитесь что PostgreSQL запущен и настроен в application.conf
```

2. Запустите сервер:
```bash
./gradlew :server:run
# Или
gradle :server:run
```

Сервер запустится на `http://localhost:8080`

## API Endpoints

### 1. Health Check
```bash
curl http://localhost:8080/health
# Ответ: OK
```

### 2. Получить всех пользователей
```bash
curl -X GET http://localhost:8080/api/users \
  -H "Content-Type: application/json"
```

**Успешный ответ (200):**
```json
[
  {
    "id": "uuid",
    "phone": "+79001234567",
    "email": "user@example.com",
    "name": "Иван Иванов",
    "avatar": null,
    "bio": null,
    "rating": 0.0,
    "createdAt": "2025-11-08T12:00:00Z",
    "lastActive": "2025-11-08T12:00:00Z",
    "role": "USER"
  }
]
```

### 3. Создать пользователя
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+79001234567",
    "email": "user@example.com",
    "name": "Иван Иванов",
    "password": "password123"
  }'
```

**Успешный ответ (201):**
```json
{
  "id": "uuid",
  "phone": "+79001234567",
  "email": "user@example.com",
  "name": "Иван Иванов",
  "avatar": null,
  "bio": null,
  "rating": 0.0,
  "createdAt": "2025-11-08T12:00:00Z",
  "lastActive": "2025-11-08T12:00:00Z",
  "role": "USER"
}
```

**Ошибки валидации (400):**
```json
{
  "errors": [
    "Phone is required",
    "Name must be at least 2 characters",
    "Email format is invalid",
    "Password must be at least 6 characters"
  ]
}
```

**Дубликат телефона (409):**
```json
{
  "error": "User with this phone already exists"
}
```

### 4. Получить пользователя по ID
```bash
curl -X GET http://localhost:8080/api/users/{user-id} \
  -H "Content-Type: application/json"
```

**Успешный ответ (200):**
```json
{
  "id": "uuid",
  "phone": "+79001234567",
  "email": "user@example.com",
  "name": "Иван Иванов",
  "avatar": null,
  "bio": null,
  "rating": 0.0,
  "createdAt": "2025-11-08T12:00:00Z",
  "lastActive": "2025-11-08T12:00:00Z",
  "role": "USER"
}
```

**Пользователь не найден (404):**
```json
{
  "error": "User not found"
}
```

**Неверный формат ID (400):**
```json
{
  "error": "Invalid user ID format"
}
```

### 5. Обновить пользователя
```bash
curl -X PUT http://localhost:8080/api/users/{user-id} \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Иван Петрович",
    "email": "newemail@example.com",
    "bio": "Житель посёлка"
  }'
```

**Успешный ответ (200):**
```json
{
  "id": "uuid",
  "phone": "+79001234567",
  "email": "newemail@example.com",
  "name": "Иван Петрович",
  "avatar": null,
  "bio": "Житель посёлка",
  "rating": 0.0,
  "createdAt": "2025-11-08T12:00:00Z",
  "lastActive": "2025-11-08T12:00:00Z",
  "role": "USER"
}
```

**Ошибки:**
- 400 - Invalid request body / Validation errors
- 404 - User not found
- 409 - Email already in use

### 6. Удалить пользователя
```bash
curl -X DELETE http://localhost:8080/api/users/{user-id}
```

**Успешный ответ (200):**
```json
{
  "message": "User deleted successfully"
}
```

**Пользователь не найден (404):**
```json
{
  "error": "User not found"
}
```

## Правила валидации

### Создание пользователя (POST)
- **phone**: обязательное, формат `+7XXXXXXXXXX` или `8XXXXXXXXXX`
- **name**: обязательное, минимум 2 символа, максимум 255
- **email**: опциональное, валидный email формат
- **password**: обязательное, минимум 6 символов

### Обновление пользователя (PUT)
- **name**: опциональное, если указано - минимум 2 символа, максимум 255
- **email**: опциональное, если указано - валидный email формат
- **bio**: опциональное, максимум 1000 символов
- **avatar**: опциональное, URL изображения

## Тестовые сценарии

### Сценарий 1: Создание и получение пользователя
```bash
# 1. Создать пользователя
USER_RESPONSE=$(curl -s -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+79001234567",
    "email": "test@example.com",
    "name": "Тестовый Пользователь",
    "password": "test123"
  }')

# Извлечь ID
USER_ID=$(echo $USER_RESPONSE | grep -o '"id":"[^"]*"' | cut -d'"' -f4)

# 2. Получить пользователя по ID
curl -X GET http://localhost:8080/api/users/$USER_ID
```

### Сценарий 2: Обновление пользователя
```bash
# Обновить данные
curl -X PUT http://localhost:8080/api/users/$USER_ID \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Обновленное Имя",
    "bio": "Новая биография"
  }'

# Проверить изменения
curl -X GET http://localhost:8080/api/users/$USER_ID
```

### Сценарий 3: Проверка дубликатов
```bash
# Попытка создать пользователя с существующим телефоном
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+79001234567",
    "email": "another@example.com",
    "name": "Другой Пользователь",
    "password": "test123"
  }'
# Ожидается 409 Conflict
```

### Сценарий 4: Валидация
```bash
# Невалидные данные
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "123",
    "email": "invalid-email",
    "name": "T",
    "password": "123"
  }'
# Ожидается 400 с массивом ошибок
```

### Сценарий 5: Удаление пользователя
```bash
# Удалить пользователя
curl -X DELETE http://localhost:8080/api/users/$USER_ID

# Попытка получить удаленного пользователя
curl -X GET http://localhost:8080/api/users/$USER_ID
# Ожидается 404
```

## Коды состояний HTTP

| Код | Описание |
|-----|----------|
| 200 | OK - Успешная операция |
| 201 | Created - Пользователь создан |
| 400 | Bad Request - Ошибка валидации или неверный формат данных |
| 404 | Not Found - Пользователь не найден |
| 409 | Conflict - Пользователь с такими данными уже существует |
| 500 | Internal Server Error - Ошибка сервера |

## Примечания

1. Все даты возвращаются в формате ISO 8601
2. UUID генерируются автоматически при создании
3. Телефон должен быть уникальным
4. Email должен быть уникальным (если указан)
5. Пароли в будущем будут хешироваться (TODO)
