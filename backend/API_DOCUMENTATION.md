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

### 🛍 Товары

#### 11. Получить список товаров

**GET** `/api/products`

**Query Parameters:**
- `categoryId` (optional): Long - фильтр по категории
- `status` (optional): String - фильтр по статусу (ACTIVE, SOLD, ARCHIVED)
- `condition` (optional): String - фильтр по состоянию (NEW, USED)
- `minPrice` (optional): Double - минимальная цена
- `maxPrice` (optional): Double - максимальная цена
- `search` (optional): String - поиск по названию и описанию
- `page` (optional): Int - номер страницы (по умолчанию 1)
- `pageSize` (optional): Int - размер страницы (по умолчанию 20, максимум 100)

**Response (200 OK):**
```json
{
  "products": [
    {
      "id": 1,
      "userId": 1,
      "title": "Диван угловой",
      "description": "Продаю диван в отличном состоянии",
      "price": 15000.0,
      "categoryId": 1,
      "condition": "USED",
      "images": ["http://example.com/image1.jpg"],
      "status": "ACTIVE",
      "views": 42,
      "createdAt": "2025-11-09T12:00:00",
      "updatedAt": "2025-11-09T12:00:00"
    }
  ],
  "total": 50,
  "page": 1,
  "pageSize": 20,
  "totalPages": 3
}
```

#### 12. Получить детали товара

**GET** `/api/products/{id}`

**Response (200 OK):**
```json
{
  "product": {
    "id": 1,
    "userId": 1,
    "title": "Диван угловой",
    "description": "Продаю диван в отличном состоянии",
    "price": 15000.0,
    "categoryId": 1,
    "condition": "USED",
    "images": ["http://example.com/image1.jpg"],
    "status": "ACTIVE",
    "views": 43,
    "createdAt": "2025-11-09T12:00:00",
    "updatedAt": "2025-11-09T12:00:00"
  },
  "user": {
    "id": 1,
    "name": "Иван Иванов",
    "avatar": null,
    "rating": 4.5,
    "isVerified": false
  },
  "category": {
    "id": 1,
    "name": "Мебель",
    "icon": "🪑"
  },
  "isFavorite": false
}
```

**Error Response (404 Not Found):**
```json
{
  "error": "PRODUCT_NOT_FOUND",
  "message": "Товар не найден"
}
```

#### 13. Создать товар

**POST** `/api/products`

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "title": "Диван угловой",
  "description": "Продаю диван в отличном состоянии. Размеры: 250x180 см",
  "price": 15000.0,
  "categoryId": 1,
  "condition": "USED",
  "images": ["http://example.com/image1.jpg", "http://example.com/image2.jpg"]
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "userId": 1,
  "title": "Диван угловой",
  "description": "Продаю диван в отличном состоянии. Размеры: 250x180 см",
  "price": 15000.0,
  "categoryId": 1,
  "condition": "USED",
  "images": ["http://example.com/image1.jpg", "http://example.com/image2.jpg"],
  "status": "ACTIVE",
  "views": 0,
  "createdAt": "2025-11-09T12:00:00",
  "updatedAt": "2025-11-09T12:00:00"
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "INVALID_TITLE",
  "message": "Название не может быть пустым"
}
```

#### 14. Обновить товар

**PUT** `/api/products/{id}`

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "title": "Диван угловой (новое название)",
  "price": 14000.0,
  "status": "ACTIVE"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "userId": 1,
  "title": "Диван угловой (новое название)",
  "description": "Продаю диван в отличном состоянии. Размеры: 250x180 см",
  "price": 14000.0,
  "categoryId": 1,
  "condition": "USED",
  "images": ["http://example.com/image1.jpg", "http://example.com/image2.jpg"],
  "status": "ACTIVE",
  "views": 43,
  "createdAt": "2025-11-09T12:00:00",
  "updatedAt": "2025-11-09T13:30:00"
}
```

**Error Response (403 Forbidden):**
```json
{
  "error": "FORBIDDEN",
  "message": "Нет прав для редактирования этого товара"
}
```

#### 15. Удалить товар

**DELETE** `/api/products/{id}`

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Товар успешно удален"
}
```

**Error Response (403 Forbidden):**
```json
{
  "error": "FORBIDDEN",
  "message": "Нет прав для удаления этого товара"
}
```

#### 16. Получить свои товары

**GET** `/api/products/my`

**Headers:**
```
Authorization: Bearer {token}
```

**Query Parameters:**
- `page` (optional): Int - номер страницы (по умолчанию 1)
- `pageSize` (optional): Int - размер страницы (по умолчанию 20, максимум 100)

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "userId": 1,
    "title": "Диван угловой",
    "description": "Продаю диван в отличном состоянии",
    "price": 15000.0,
    "categoryId": 1,
    "condition": "USED",
    "images": ["http://example.com/image1.jpg"],
    "status": "ACTIVE",
    "views": 42,
    "createdAt": "2025-11-09T12:00:00",
    "updatedAt": "2025-11-09T12:00:00"
  }
]
```

#### 17. Добавить товар в избранное

**POST** `/api/products/{id}/favorite`

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Товар добавлен в избранное"
}
```

**Error Response (409 Conflict):**
```json
{
  "error": "ALREADY_IN_FAVORITES",
  "message": "Товар уже в избранном"
}
```

#### 18. Удалить товар из избранного

**DELETE** `/api/products/{id}/favorite`

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Товар удален из избранного"
}
```

#### 19. Получить список избранных товаров

**GET** `/api/products/favorites`

**Headers:**
```
Authorization: Bearer {token}
```

**Query Parameters:**
- `page` (optional): Int - номер страницы (по умолчанию 1)
- `pageSize` (optional): Int - размер страницы (по умолчанию 20, максимум 100)

**Response (200 OK):**
```json
{
  "products": [
    {
      "id": 2,
      "userId": 3,
      "title": "iPhone 13 Pro",
      "description": "Состояние идеальное, полный комплект",
      "price": 65000.0,
      "categoryId": 2,
      "condition": "USED",
      "images": ["http://example.com/iphone.jpg"],
      "status": "ACTIVE",
      "views": 120,
      "createdAt": "2025-11-08T10:00:00",
      "updatedAt": "2025-11-08T10:00:00"
    }
  ],
  "total": 5,
  "page": 1,
  "pageSize": 20,
  "totalPages": 1
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

### Получить список товаров
```bash
curl -X GET "http://localhost:8080/api/products?page=1&pageSize=20&categoryId=1"
```

### Получить детали товара
```bash
curl -X GET http://localhost:8080/api/products/1
```

### Создать товар
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Диван угловой",
    "description": "Продаю диван в отличном состоянии",
    "price": 15000.0,
    "categoryId": 1,
    "condition": "USED",
    "images": ["http://example.com/image1.jpg"]
  }'
```

### Обновить товар
```bash
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Диван угловой (обновлено)",
    "price": 14000.0
  }'
```

### Удалить товар
```bash
curl -X DELETE http://localhost:8080/api/products/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Получить свои товары
```bash
curl -X GET http://localhost:8080/api/products/my \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Добавить товар в избранное
```bash
curl -X POST http://localhost:8080/api/products/1/favorite \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Удалить товар из избранного
```bash
curl -X DELETE http://localhost:8080/api/products/1/favorite \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Получить избранные товары
```bash
curl -X GET http://localhost:8080/api/products/favorites \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

### 🔧 Услуги

#### 20. Получить список услуг

**GET** `/api/services`

**Query Parameters:**
- `categoryId` (optional): Long - фильтр по категории
- `status` (optional): String - фильтр по статусу (ACTIVE, INACTIVE)
- `search` (optional): String - поиск по названию и описанию
- `page` (optional): Int - номер страницы (по умолчанию 1)
- `pageSize` (optional): Int - размер страницы (по умолчанию 20, максимум 100)

**Response (200 OK):**
```json
{
  "services": [
    {
      "id": 1,
      "userId": 1,
      "title": "Ремонт компьютеров",
      "description": "Профессиональный ремонт ПК и ноутбуков",
      "categoryId": 16,
      "price": "1000",
      "images": ["http://example.com/service1.jpg"],
      "status": "ACTIVE",
      "views": 25,
      "createdAt": "2025-11-09T12:00:00",
      "updatedAt": "2025-11-09T12:00:00"
    }
  ],
  "total": 30,
  "page": 1,
  "pageSize": 20,
  "totalPages": 2
}
```

#### 21. Получить детали услуги

**GET** `/api/services/{id}`

**Response (200 OK):**
```json
{
  "service": {
    "id": 1,
    "userId": 1,
    "title": "Ремонт компьютеров",
    "description": "Профессиональный ремонт ПК и ноутбуков. Опыт работы более 5 лет.",
    "categoryId": 16,
    "price": "1000",
    "images": ["http://example.com/service1.jpg"],
    "status": "ACTIVE",
    "views": 26,
    "createdAt": "2025-11-09T12:00:00",
    "updatedAt": "2025-11-09T12:00:00"
  },
  "user": {
    "id": 1,
    "name": "Иван Иванов",
    "avatar": null,
    "rating": 4.8,
    "isVerified": true
  },
  "category": {
    "id": 16,
    "name": "Ремонт и обслуживание",
    "icon": "🔧"
  }
}
```

**Error Response (404 Not Found):**
```json
{
  "error": "SERVICE_NOT_FOUND",
  "message": "Услуга не найдена"
}
```

#### 22. Создать услугу

**POST** `/api/services`

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "title": "Ремонт компьютеров",
  "description": "Профессиональный ремонт ПК и ноутбуков. Опыт работы более 5 лет.",
  "categoryId": 16,
  "price": "1000",
  "images": ["http://example.com/service1.jpg"]
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "userId": 1,
  "title": "Ремонт компьютеров",
  "description": "Профессиональный ремонт ПК и ноутбуков. Опыт работы более 5 лет.",
  "categoryId": 16,
  "price": "1000",
  "images": ["http://example.com/service1.jpg"],
  "status": "ACTIVE",
  "views": 0,
  "createdAt": "2025-11-09T12:00:00",
  "updatedAt": "2025-11-09T12:00:00"
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "INVALID_CATEGORY",
  "message": "Указанная категория не предназначена для услуг"
}
```

**Валидация:**
- `title`: обязательное, 1-200 символов
- `description`: обязательное, минимум 1 символ
- `categoryId`: обязательное, должна быть категория типа SERVICE
- `price`: опциональное, может быть null или строка (например "1000" или "Договорная")
- `images`: обязательное, 1-5 изображений

#### 23. Обновить услугу

**PUT** `/api/services/{id}`

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body (все поля опциональные):**
```json
{
  "title": "Ремонт компьютеров (обновлено)",
  "description": "Новое описание",
  "categoryId": 16,
  "price": "1500",
  "status": "ACTIVE",
  "images": ["http://example.com/new_image.jpg"]
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "userId": 1,
  "title": "Ремонт компьютеров (обновлено)",
  "description": "Новое описание",
  "categoryId": 16,
  "price": "1500",
  "images": ["http://example.com/new_image.jpg"],
  "status": "ACTIVE",
  "views": 26,
  "createdAt": "2025-11-09T12:00:00",
  "updatedAt": "2025-11-09T13:00:00"
}
```

**Error Response (403 Forbidden):**
```json
{
  "error": "FORBIDDEN",
  "message": "Нет прав для редактирования этой услуги"
}
```

#### 24. Удалить услугу

**DELETE** `/api/services/{id}`

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Услуга успешно удалена"
}
```

**Error Response (403 Forbidden):**
```json
{
  "error": "FORBIDDEN",
  "message": "Нет прав для удаления этой услуги"
}
```

#### 25. Получить свои услуги

**GET** `/api/services/my`

**Headers:**
```
Authorization: Bearer {token}
```

**Query Parameters:**
- `page` (optional): Int - номер страницы (по умолчанию 1)
- `pageSize` (optional): Int - размер страницы (по умолчанию 20, максимум 100)

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "userId": 1,
    "title": "Ремонт компьютеров",
    "description": "Профессиональный ремонт ПК и ноутбуков",
    "categoryId": 16,
    "price": "1000",
    "images": ["http://example.com/service1.jpg"],
    "status": "ACTIVE",
    "views": 26,
    "createdAt": "2025-11-09T12:00:00",
    "updatedAt": "2025-11-09T12:00:00"
  }
]
```

---

## 🧪 Примеры использования (cURL)

### Services

### Получить список услуг
```bash
curl -X GET "http://localhost:8080/api/services?categoryId=16&page=1&pageSize=10"
```

### Получить детали услуги
```bash
curl -X GET http://localhost:8080/api/services/1
```

### Создать услугу
```bash
curl -X POST http://localhost:8080/api/services \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Ремонт компьютеров",
    "description": "Профессиональный ремонт ПК и ноутбуков",
    "categoryId": 16,
    "price": "1000",
    "images": ["http://example.com/service1.jpg"]
  }'
```

### Обновить услугу
```bash
curl -X PUT http://localhost:8080/api/services/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Ремонт компьютеров (обновлено)",
    "price": "1500"
  }'
```

### Удалить услугу
```bash
curl -X DELETE http://localhost:8080/api/services/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Получить свои услуги
```bash
curl -X GET http://localhost:8080/api/services/my \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## 📁 Загрузка файлов

### 1. Загрузка изображения

**POST** `/api/upload?type={type}`

Универсальный endpoint для загрузки изображений (аватары, фото товаров, фото услуг)

**Headers:**
```
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Query параметры:**
- `type` (обязательный) - тип файла: `avatar`, `product`, `service`

**Request (multipart/form-data):**
- `file` - файл изображения (JPG, PNG, WEBP, максимум 5 MB)

**Response (200 OK):**
```json
{
  "success": true,
  "url": "/uploads/products/550e8400-e29b-41d4-a716-446655440000.jpg",
  "fileName": "550e8400-e29b-41d4-a716-446655440000.jpg",
  "message": "Файл успешно загружен"
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Размер файла (6.50 MB) превышает максимально допустимый (5 MB)"
}
```

**Примеры использования:**

```bash
# Загрузка изображения товара
curl -X POST "http://localhost:8080/api/upload?type=product" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -F "file=@/path/to/image.jpg"

# Загрузка изображения услуги
curl -X POST "http://localhost:8080/api/upload?type=service" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -F "file=@/path/to/image.png"

# Загрузка аватара
curl -X POST "http://localhost:8080/api/upload?type=avatar" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -F "file=@/path/to/avatar.jpg"
```

### 2. Загрузка аватара пользователя

**POST** `/api/users/{id}/avatar`

Специальный endpoint для загрузки и обновления аватара пользователя. Автоматически обновляет поле `avatar` в профиле пользователя.

**Headers:**
```
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Path параметры:**
- `id` - ID пользователя (можно обновить только свой аватар)

**Request (multipart/form-data):**
- `file` - файл изображения аватара (JPG, PNG, WEBP, максимум 5 MB)

**Response (200 OK):**
```json
{
  "success": true,
  "url": "/uploads/avatars/550e8400-e29b-41d4-a716-446655440000.jpg",
  "fileName": "550e8400-e29b-41d4-a716-446655440000.jpg",
  "message": "Аватар успешно обновлен"
}
```

**Error Response (403 Forbidden):**
```json
{
  "error": "FORBIDDEN",
  "message": "Вы можете обновить только свой аватар"
}
```

**Пример использования:**

```bash
curl -X POST http://localhost:8080/api/users/1/avatar \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -F "file=@/path/to/avatar.jpg"
```

### 3. Доступ к загруженным файлам

Все загруженные файлы доступны по URL:
```
http://localhost:8080/uploads/{type}/{filename}
```

**Примеры:**
- Аватар: `http://localhost:8080/uploads/avatars/550e8400-e29b-41d4-a716-446655440000.jpg`
- Фото товара: `http://localhost:8080/uploads/products/550e8400-e29b-41d4-a716-446655440000.jpg`
- Фото услуги: `http://localhost:8080/uploads/services/550e8400-e29b-41d4-a716-446655440000.jpg`

### Требования к изображениям

- **Форматы:** JPG, JPEG, PNG, WEBP
- **Максимальный размер:** 5 MB
- **Валидация:** проверка MIME type и расширения файла
- **Имена файлов:** автоматическая генерация UUID для предотвращения конфликтов

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

### Товары

#### Название товара
- Минимум 1 символ
- Максимум 200 символов
- Обязательное поле

#### Описание товара
- Минимум 1 символ
- Обязательное поле

#### Цена
- Не может быть отрицательной
- Обязательное поле

#### Изображения
- Минимум 1 изображение
- Максимум 5 изображений
- Обязательное поле

#### Состояние (condition)
- Допустимые значения: `NEW`, `USED`
- Обязательное поле

#### Статус (status)
- Допустимые значения: `ACTIVE`, `SOLD`, `ARCHIVED`
- По умолчанию: `ACTIVE`

### Услуги

#### Название услуги
- Минимум 1 символ
- Максимум 200 символов
- Обязательное поле

#### Описание услуги
- Минимум 1 символ
- Обязательное поле

#### Цена услуги
- Опциональное поле
- Может быть null или строка (например "1000" или "Договорная")

#### Изображения услуги
- Минимум 1 изображение
- Максимум 5 изображений
- Обязательное поле

#### Статус (status)
- Допустимые значения: `ACTIVE`, `INACTIVE`
- По умолчанию: `ACTIVE`

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
| 200 | OK - Успешный запрос |
| 201 | Created - Ресурс успешно создан |
| 400 | Bad Request - Неверный запрос |
| 401 | Unauthorized - Требуется аутентификация |
| 403 | Forbidden - Недостаточно прав |
| 404 | Not Found - Ресурс не найден |
| 409 | Conflict - Конфликт данных |
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
