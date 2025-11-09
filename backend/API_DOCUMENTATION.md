# SocialSpace API Documentation

## 🚀 Базовый URL

```
http://localhost:8080/api
```

## 📋 Endpoints

### 🔐 Аутентификация

#### 1. Регистрация пользователя

**POST** `/api/auth/register`

**Request Body:**
```json
{
  "phone": "+79001234567",
  "email": "user@example.com",
  "name": "Иван Иванов",
  "password": "password123"
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "phone": "+79001234567",
    "email": "user@example.com",
    "name": "Иван Иванов",
    "avatar": null,
    "bio": null,
    "rating": null,
    "createdAt": "2025-11-09T12:00:00",
    "isVerified": false,
    "role": "USER"
  }
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "REGISTRATION_ERROR",
  "message": "Пользователь с таким номером телефона уже существует"
}
```

#### 2. Вход пользователя

**POST** `/api/auth/login`

**Request Body:**
```json
{
  "phone": "+79001234567",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "phone": "+79001234567",
    "email": "user@example.com",
    "name": "Иван Иванов",
    "avatar": null,
    "bio": null,
    "rating": null,
    "createdAt": "2025-11-09T12:00:00",
    "isVerified": false,
    "role": "USER"
  }
}
```

**Error Response (401 Unauthorized):**
```json
{
  "error": "LOGIN_ERROR",
  "message": "Неверный номер телефона или пароль"
}
```

#### 3. Получить текущего пользователя

**GET** `/api/auth/me`

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "phone": "+79001234567",
  "email": "user@example.com",
  "name": "Иван Иванов",
  "avatar": null,
  "bio": null,
  "rating": null,
  "createdAt": "2025-11-09T12:00:00",
  "isVerified": false,
  "role": "USER"
}
```

---

### 👤 Пользователи

#### 4. Получить публичный профиль пользователя

**GET** `/api/users/{id}`

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Иван Иванов",
  "avatar": null,
  "bio": "О себе",
  "rating": 4.5,
  "isVerified": false,
  "createdAt": "2025-11-09T12:00:00"
}
```

**Error Response (404 Not Found):**
```json
{
  "error": "USER_NOT_FOUND",
  "message": "Пользователь не найден"
}
```

#### 5. Обновить профиль пользователя

**PUT** `/api/users/{id}`

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "name": "Новое Имя",
  "email": "newemail@example.com",
  "bio": "Обновленная биография"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "phone": "+79001234567",
  "email": "newemail@example.com",
  "name": "Новое Имя",
  "avatar": null,
  "bio": "Обновленная биография",
  "rating": null,
  "createdAt": "2025-11-09T12:00:00",
  "isVerified": false,
  "role": "USER"
}
```

**Error Response (403 Forbidden):**
```json
{
  "error": "FORBIDDEN",
  "message": "Нет прав для редактирования этого профиля"
}
```

#### 6. Удалить пользователя

**DELETE** `/api/users/{id}`

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Аккаунт успешно удален"
}
```

---

### 📂 Категории

#### 7. Получить все категории

**GET** `/api/categories`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Мебель",
    "icon": "🪑",
    "type": "PRODUCT"
  },
  {
    "id": 2,
    "name": "Электроника",
    "icon": "📱",
    "type": "PRODUCT"
  },
  {
    "id": 16,
    "name": "Ремонт и строительство",
    "icon": "🔧",
    "type": "SERVICE"
  }
]
```

#### 8. Получить категории товаров

**GET** `/api/categories/products`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Мебель",
    "icon": "🪑",
    "type": "PRODUCT"
  },
  {
    "id": 2,
    "name": "Электроника",
    "icon": "📱",
    "type": "PRODUCT"
  }
]
```

#### 9. Получить категории услуг

**GET** `/api/categories/services`

**Response (200 OK):**
```json
[
  {
    "id": 16,
    "name": "Ремонт и строительство",
    "icon": "🔧",
    "type": "SERVICE"
  },
  {
    "id": 17,
    "name": "Уборка",
    "icon": "🧹",
    "type": "SERVICE"
  }
]
```

#### 10. Получить категорию по ID

**GET** `/api/categories/{id}`

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Мебель",
  "icon": "🪑",
  "type": "PRODUCT"
}
```

**Error Response (404 Not Found):**
```json
{
  "error": "CATEGORY_NOT_FOUND",
  "message": "Категория не найдена"
}
```

---

## 🧪 Примеры использования (cURL)

### Регистрация
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+79001234567",
    "email": "user@example.com",
    "name": "Иван Иванов",
    "password": "password123"
  }'
```

### Вход
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+79001234567",
    "password": "password123"
  }'
```

### Получить текущего пользователя
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Обновить профиль
```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Новое Имя",
    "bio": "Моя биография"
  }'
```

### Получить все категории
```bash
curl -X GET http://localhost:8080/api/categories
```

### Получить категории товаров
```bash
curl -X GET http://localhost:8080/api/categories/products
```

### Получить категории услуг
```bash
curl -X GET http://localhost:8080/api/categories/services
```

---

## 📝 Валидация

### Номер телефона
- Формат: `+7XXXXXXXXXX` (10 цифр после +7)
- Принимаются форматы: `+79001234567`, `89001234567`, `79001234567`
- Автоматическая нормализация к формату `+7XXXXXXXXXX`

### Email
- Стандартный формат email
- Опциональное поле

### Пароль
- Минимум 6 символов
- Максимум 100 символов

### Имя
- Минимум 2 символа
- Максимум 100 символов

### Биография
- Максимум 500 символов
- Опциональное поле

---

## 🔒 Аутентификация

API использует JWT (JSON Web Tokens) для аутентификации.

**Получение токена:**
1. Регистрация через `/api/auth/register`
2. Вход через `/api/auth/login`

**Использование токена:**
Добавьте заголовок `Authorization` во все защищенные запросы:
```
Authorization: Bearer YOUR_TOKEN_HERE
```

**Срок действия токена:**
- 7 дней с момента выдачи

---

## ❌ Коды ошибок

| Код | Описание |
|-----|----------|
| 400 | Bad Request - Неверный запрос |
| 401 | Unauthorized - Требуется аутентификация |
| 403 | Forbidden - Недостаточно прав |
| 404 | Not Found - Ресурс не найден |
| 500 | Internal Server Error - Внутренняя ошибка сервера |

---

## 🏃 Запуск сервера

```bash
# С использованием Gradle
./gradlew :backend:run

# Или с системным Gradle
gradle :backend:run
```

Сервер запустится на `http://localhost:8080`

---

**Версия API:** 1.0.0  
**Дата обновления:** 2025-11-09
