# SocialSpace Backend

Ktor-based REST API server для SocialSpace marketplace приложения.

## Технологии

- Kotlin
- Ktor Server 3.3.2
- Exposed ORM 0.58.0
- PostgreSQL 42.7.4
- JWT Authentication
- BCrypt для хеширования паролей

## Структура проекта

```
backend/
├── src/main/kotlin/info/javaway/sc/backend/
│   ├── Application.kt           # Точка входа
│   ├── plugins/                 # Ktor плагины
│   │   ├── Serialization.kt     # JSON сериализация
│   │   ├── Security.kt          # JWT аутентификация
│   │   ├── HTTP.kt              # CORS настройки
│   │   ├── Monitoring.kt        # Логирование
│   │   ├── Routing.kt           # Маршруты API
│   │   └── Database.kt          # Подключение к БД
│   ├── models/                  # Модели данных
│   │   ├── User.kt
│   │   ├── Product.kt
│   │   ├── Service.kt
│   │   └── Category.kt
│   ├── data/tables/             # Схемы таблиц Exposed
│   │   ├── Users.kt
│   │   ├── Products.kt
│   │   ├── Services.kt
│   │   ├── Categories.kt
│   │   └── Favorites.kt
│   └── utils/                   # Утилиты
│       ├── JwtConfig.kt         # JWT токены
│       └── PasswordHasher.kt    # Хеширование паролей
├── src/main/resources/
│   ├── application.conf         # Конфигурация
│   └── logback.xml              # Настройки логирования
└── build.gradle.kts
```

## Запуск

### 1. Подготовка базы данных

Убедитесь, что PostgreSQL запущен и создайте базу данных:

```bash
createdb socialspace
```

Или через psql:

```sql
CREATE DATABASE socialspace;
```

### 2. Настройка конфигурации

Отредактируйте `src/main/resources/application.conf` или установите переменные окружения:

```bash
export DATABASE_URL="jdbc:postgresql://localhost:5432/socialspace"
export DATABASE_USER="postgres"
export DATABASE_PASSWORD="your_password"
export JWT_SECRET="your-secret-key"
```

### 3. Запуск сервера

```bash
# Из корня проекта
./gradlew :backend:run

# Или из папки backend
cd backend
../gradlew run
```

Сервер запустится на `http://localhost:8080`

### 4. Проверка работы

```bash
curl http://localhost:8080
# Ответ: SocialSpace API - Server is running!

curl http://localhost:8080/health
# Ответ: OK
```

## API Endpoints (в разработке)

### Аутентификация
- `POST /api/auth/register` - Регистрация
- `POST /api/auth/login` - Вход
- `GET /api/auth/me` - Текущий пользователь

### Пользователи
- `GET /api/users/{id}` - Профиль
- `PUT /api/users/{id}` - Обновление профиля

### Товары
- `GET /api/products` - Список товаров
- `POST /api/products` - Создание товара
- `GET /api/products/{id}` - Детали товара
- `PUT /api/products/{id}` - Обновление
- `DELETE /api/products/{id}` - Удаление

### Услуги
- `GET /api/services` - Список услуг
- `POST /api/services` - Создание услуги

### Категории
- `GET /api/categories` - Все категории

## Разработка

### Тестирование

```bash
./gradlew :backend:test
```

### Сборка JAR

```bash
./gradlew :backend:shadowJar
```

## Переменные окружения

| Переменная | Описание | По умолчанию |
|-----------|----------|--------------|
| PORT | Порт сервера | 8080 |
| DATABASE_URL | JDBC URL базы данных | jdbc:postgresql://localhost:5432/socialspace |
| DATABASE_USER | Пользователь БД | postgres |
| DATABASE_PASSWORD | Пароль БД | postgres |
| JWT_SECRET | Секретный ключ JWT | (требуется изменить в production) |
| JWT_ISSUER | JWT Issuer | http://0.0.0.0:8080 |
| JWT_AUDIENCE | JWT Audience | http://0.0.0.0:8080 |

## Следующие шаги

- [ ] Реализация API эндпоинтов для аутентификации
- [ ] Реализация CRUD операций для товаров
- [ ] Реализация CRUD операций для услуг
- [ ] Загрузка файлов/изображений
- [ ] Пагинация и фильтрация
- [ ] Тесты
