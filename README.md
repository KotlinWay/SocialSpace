# SocialSpace

Мобильное приложение для жителей посёлка, объединяющее функции маркетплейса, доски объявлений услуг и новостной ленты местного сообщества.

## Технологический стек

### Backend
- **Ktor** - веб-фреймворк для Kotlin
- **PostgreSQL** - база данных
- **Exposed** - ORM для работы с БД
- **JWT** - аутентификация

### Client
- **Kotlin Multiplatform** - общий код для всех платформ
- **Compose Multiplatform** - современный UI фреймворк
- **Ktor Client** - HTTP клиент
- **SQLDelight** - локальная база данных
- **Koin** - Dependency Injection
- **Voyager** - навигация

## Структура проекта

```
SocialSpace/
├── shared/          # Общий код для Android и iOS
├── androidApp/      # Android приложение
├── server/          # Backend на Ktor
└── iosApp/          # iOS приложение (будет добавлено позже)
```

## Требования

- **JDK 11+** (рекомендуется JDK 17)
- **Android Studio** Hedgehog (2023.1.1) или новее
- **Xcode 15+** (для сборки iOS)
- **PostgreSQL 15+** (для backend)

## Установка и запуск

### 1. Клонирование репозитория

```bash
git clone https://github.com/your-username/SocialSpace.git
cd SocialSpace
```

### 2. Настройка базы данных

Создайте базу данных PostgreSQL:

```bash
psql -U postgres
CREATE DATABASE socialspace;
\q
```

### 3. Настройка переменных окружения

Создайте файл `.env` в корне проекта (или настройте переменные окружения):

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/socialspace
DATABASE_USER=postgres
DATABASE_PASSWORD=your_password
JWT_SECRET=your_secret_key_change_in_production
```

### 4. Запуск Backend сервера

```bash
# Из корня проекта
./gradlew :server:run
```

Сервер будет доступен по адресу: `http://localhost:8080`

Проверка работоспособности:
```bash
curl http://localhost:8080/health
```

### 5. Запуск Android приложения

#### Через Android Studio:
1. Откройте проект в Android Studio
2. Дождитесь синхронизации Gradle
3. Выберите конфигурацию `androidApp`
4. Нажмите Run (зелёная кнопка запуска)

#### Через командную строку:
```bash
./gradlew :androidApp:installDebug
```

### 6. Запуск iOS приложения (Coming Soon)

iOS приложение будет добавлено в следующих итерациях.

## Разработка

### Сборка проекта

```bash
# Сборка всех модулей
./gradlew build

# Сборка только shared модуля
./gradlew :shared:build

# Сборка только Android приложения
./gradlew :androidApp:assembleDebug

# Сборка только сервера
./gradlew :server:build
```

### Запуск тестов

```bash
# Все тесты
./gradlew test

# Тесты shared модуля
./gradlew :shared:test
```

### Проверка кода

```bash
# Kotlin code style check
./gradlew ktlintCheck

# Форматирование кода
./gradlew ktlintFormat
```

## API Endpoints

### Базовые endpoints

- `GET /` - Приветственное сообщение
- `GET /health` - Проверка статуса сервера

### Планируемые endpoints

- `POST /api/auth/register` - Регистрация пользователя
- `POST /api/auth/login` - Вход в систему
- `GET /api/users/me` - Профиль текущего пользователя
- `GET /api/products` - Список товаров
- `POST /api/products` - Создание объявления о товаре
- `GET /api/services` - Список услуг
- `POST /api/services` - Создание объявления об услуге

Полная документация API будет доступна после реализации.

## Архитектура

Проект использует **Clean Architecture** с разделением на слои:

### Shared модуль (Kotlin Multiplatform)
- **Presentation Layer** - UI и ViewModels (MVI pattern)
- **Domain Layer** - Use Cases и бизнес-логика
- **Data Layer** - Repositories и источники данных

### Server модуль (Ktor)
- **Routes** - HTTP endpoints
- **Services** - Бизнес-логика
- **Repositories** - Работа с БД
- **Models** - Модели данных
- **Plugins** - Конфигурация Ktor

## Прогресс разработки

См. файл [CLAUDE.md](CLAUDE.md) для детального плана разработки и текущего статуса.

### Текущий статус: Фаза 0 - Инициализация проекта ✅

- [x] Создание репозитория
- [x] Настройка Kotlin Multiplatform проекта
- [x] Настройка серверного проекта (Ktor)
- [x] Создание базовой структуры модулей

### Следующие шаги:
- [ ] Настройка базы данных и миграций
- [ ] Реализация JWT аутентификации
- [ ] Создание базовых UI компонентов

## Участие в разработке

Этот проект находится в активной разработке. Мы работаем небольшими итерациями, добавляя функциональность шаг за шагом.

## Лицензия

MIT License

## Контакты

Для вопросов и предложений создавайте Issues в репозитории.

---

**Версия**: 1.0.0
**Последнее обновление**: 2025-11-08
