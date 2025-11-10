#!/bin/bash

# Скрипт для добавления тестовых данных в SocialSpace API
# Использование: ./add_test_data.sh

API_URL="http://localhost:8080"

echo "🚀 Добавление тестовых данных в SocialSpace..."
echo ""

# 1. Регистрация тестового пользователя 1
echo "📝 Регистрация пользователя 1..."
USER1_RESPONSE=$(curl -s -X POST "$API_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+79991234567",
    "name": "Иван Петров",
    "email": "ivan@example.com",
    "password": "password123"
  }')

USER1_TOKEN=$(echo $USER1_RESPONSE | grep -o '"token":"[^"]*' | sed 's/"token":"//')
echo "✅ Пользователь 1 зарегистрирован. Token: ${USER1_TOKEN:0:20}..."
echo ""

# 2. Регистрация тестового пользователя 2
echo "📝 Регистрация пользователя 2..."
USER2_RESPONSE=$(curl -s -X POST "$API_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+79997654321",
    "name": "Мария Сидорова",
    "email": "maria@example.com",
    "password": "password123"
  }')

USER2_TOKEN=$(echo $USER2_RESPONSE | grep -o '"token":"[^"]*' | sed 's/"token":"//')
echo "✅ Пользователь 2 зарегистрирован. Token: ${USER2_TOKEN:0:20}..."
echo ""

# 3. Создание товаров для пользователя 1
echo "📦 Создание товаров для пользователя 1..."

# Товар 1 - Диван
curl -s -X POST "$API_URL/api/products" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  -d '{
    "title": "Диван угловой",
    "description": "Удобный угловой диван в отличном состоянии. Раскладывается в двуспальную кровать.",
    "price": 15000,
    "categoryId": 1,
    "condition": "USED",
    "images": ["https://via.placeholder.com/400x300?text=Диван"]
  }' > /dev/null
echo "✅ Товар 1 создан: Диван угловой"

# Товар 2 - Ноутбук
curl -s -X POST "$API_URL/api/products" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  -d '{
    "title": "Ноутбук Dell XPS 15",
    "description": "Мощный ноутбук для работы и игр. i7, 16GB RAM, GTX 1650.",
    "price": 55000,
    "categoryId": 2,
    "condition": "USED",
    "images": ["https://via.placeholder.com/400x300?text=Ноутбук"]
  }' > /dev/null
echo "✅ Товар 2 создан: Ноутбук Dell XPS 15"

# Товар 3 - Велосипед
curl -s -X POST "$API_URL/api/products" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  -d '{
    "title": "Горный велосипед",
    "description": "Велосипед для горных маршрутов. 21 скорость, амортизация.",
    "price": 20000,
    "categoryId": 7,
    "condition": "USED",
    "images": ["https://via.placeholder.com/400x300?text=Велосипед"]
  }' > /dev/null
echo "✅ Товар 3 создан: Горный велосипед"

# 4. Создание товаров для пользователя 2
echo ""
echo "📦 Создание товаров для пользователя 2..."

# Товар 4 - Холодильник
curl -s -X POST "$API_URL/api/products" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER2_TOKEN" \
  -d '{
    "title": "Холодильник LG",
    "description": "Двухкамерный холодильник в отличном состоянии. No Frost.",
    "price": 25000,
    "categoryId": 5,
    "condition": "USED",
    "images": ["https://via.placeholder.com/400x300?text=Холодильник"]
  }' > /dev/null
echo "✅ Товар 4 создан: Холодильник LG"

# Товар 5 - Детская коляска
curl -s -X POST "$API_URL/api/products" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER2_TOKEN" \
  -d '{
    "title": "Детская коляска 3 в 1",
    "description": "Универсальная коляска для новорожденных. Люлька, прогулочный блок, автокресло.",
    "price": 12000,
    "categoryId": 3,
    "condition": "USED",
    "images": ["https://via.placeholder.com/400x300?text=Коляска"]
  }' > /dev/null
echo "✅ Товар 5 создан: Детская коляска 3 в 1"

# Товар 6 - IPhone
curl -s -X POST "$API_URL/api/products" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER2_TOKEN" \
  -d '{
    "title": "iPhone 13 Pro 128GB",
    "description": "Отличное состояние, полный комплект, без царапин.",
    "price": 45000,
    "categoryId": 2,
    "condition": "USED",
    "images": ["https://via.placeholder.com/400x300?text=iPhone"]
  }' > /dev/null
echo "✅ Товар 6 создан: iPhone 13 Pro"

echo ""
echo "🎉 Тестовые данные успешно добавлены!"
echo ""
echo "📊 Итого:"
echo "   - 2 пользователя"
echo "   - 6 товаров"
echo ""
echo "Логин пользователя 1:"
echo "   Телефон: +79991234567"
echo "   Пароль: password123"
echo ""
echo "Логин пользователя 2:"
echo "   Телефон: +79997654321"
echo "   Пароль: password123"
