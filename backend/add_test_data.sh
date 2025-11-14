#!/bin/bash

# Скрипт для добавления тестовых данных в SocialSpace API
# Использование: ./add_test_data.sh

API_URL="http://localhost:8080"

echo "🚀 Добавление тестовых данных в SocialSpace..."
echo ""

# Функция для извлечения токена из JSON
extract_token() {
    local response="$1"

    # Пробуем использовать jq, если доступен
    if command -v jq &> /dev/null; then
        echo "$response" | jq -r '.token // empty'
    else
        # Fallback: используем sed для парсинга
        echo "$response" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p'
    fi
}

# Функция для проверки успешности операции
check_response() {
    local response="$1"
    local operation="$2"

    if echo "$response" | grep -q '"error"'; then
        echo "❌ Ошибка при $operation:"
        echo "$response"
        return 1
    fi
    return 0
}

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

echo "   Ответ API: $USER1_RESPONSE"

if ! check_response "$USER1_RESPONSE" "регистрации пользователя 1"; then
    echo "⚠️  Возможно, пользователь уже существует. Пробуем войти..."
    USER1_RESPONSE=$(curl -s -X POST "$API_URL/api/auth/login" \
      -H "Content-Type: application/json" \
      -d '{
        "phone": "+79991234567",
        "password": "password123"
      }')
    echo "   Ответ API (login): $USER1_RESPONSE"
fi

USER1_TOKEN=$(extract_token "$USER1_RESPONSE")

if [ -z "$USER1_TOKEN" ]; then
    echo "❌ Не удалось получить токен для пользователя 1"
    exit 1
fi

echo "✅ Пользователь 1 зарегистрирован. Token: ${USER1_TOKEN:0:20}..."
echo "   Полный токен: $USER1_TOKEN"
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

echo "   Ответ API: $USER2_RESPONSE"

if ! check_response "$USER2_RESPONSE" "регистрации пользователя 2"; then
    echo "⚠️  Возможно, пользователь уже существует. Пробуем войти..."
    USER2_RESPONSE=$(curl -s -X POST "$API_URL/api/auth/login" \
      -H "Content-Type: application/json" \
      -d '{
        "phone": "+79997654321",
        "password": "password123"
      }')
    echo "   Ответ API (login): $USER2_RESPONSE"
fi

USER2_TOKEN=$(extract_token "$USER2_RESPONSE")

if [ -z "$USER2_TOKEN" ]; then
    echo "❌ Не удалось получить токен для пользователя 2"
    exit 1
fi

echo "✅ Пользователь 2 зарегистрирован. Token: ${USER2_TOKEN:0:20}..."
echo "   Полный токен: $USER2_TOKEN"
echo ""

# 3. Создание товаров для пользователя 1
echo "📦 Создание товаров для пользователя 1..."
echo ""

# Товар 1 - Диван
echo "   Создание товара 1: Диван угловой..."
PRODUCT1_RESPONSE=$(curl -s -X POST "$API_URL/api/products" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  -d '{
    "title": "Диван угловой",
    "description": "Удобный угловой диван в отличном состоянии. Раскладывается в двуспальную кровать.",
    "price": 15000,
    "categoryId": 1,
    "condition": "USED",
    "images": ["https://via.placeholder.com/400x300?text=Диван"]
  }')

if check_response "$PRODUCT1_RESPONSE" "создании товара 1"; then
    echo "✅ Товар 1 создан: Диван угловой"
else
    echo "   Ответ API: $PRODUCT1_RESPONSE"
fi

# Товар 2 - Ноутбук
echo "   Создание товара 2: Ноутбук Dell XPS 15..."
PRODUCT2_RESPONSE=$(curl -s -X POST "$API_URL/api/products" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  -d '{
    "title": "Ноутбук Dell XPS 15",
    "description": "Мощный ноутбук для работы и игр. i7, 16GB RAM, GTX 1650.",
    "price": 55000,
    "categoryId": 2,
    "condition": "USED",
    "images": ["https://via.placeholder.com/400x300?text=Ноутбук"]
  }')

if check_response "$PRODUCT2_RESPONSE" "создании товара 2"; then
    echo "✅ Товар 2 создан: Ноутбук Dell XPS 15"
else
    echo "   Ответ API: $PRODUCT2_RESPONSE"
fi

# Товар 3 - Велосипед
echo "   Создание товара 3: Горный велосипед..."
PRODUCT3_RESPONSE=$(curl -s -X POST "$API_URL/api/products" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  -d '{
    "title": "Горный велосипед",
    "description": "Велосипед для горных маршрутов. 21 скорость, амортизация.",
    "price": 20000,
    "categoryId": 7,
    "condition": "USED",
    "images": ["https://via.placeholder.com/400x300?text=Велосипед"]
  }')

if check_response "$PRODUCT3_RESPONSE" "создании товара 3"; then
    echo "✅ Товар 3 создан: Горный велосипед"
else
    echo "   Ответ API: $PRODUCT3_RESPONSE"
fi

# 4. Создание товаров для пользователя 2
echo ""
echo "📦 Создание товаров для пользователя 2..."
echo ""

# Товар 4 - Холодильник
echo "   Создание товара 4: Холодильник LG..."
PRODUCT4_RESPONSE=$(curl -s -X POST "$API_URL/api/products" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER2_TOKEN" \
  -d '{
    "title": "Холодильник LG",
    "description": "Двухкамерный холодильник в отличном состоянии. No Frost.",
    "price": 25000,
    "categoryId": 5,
    "condition": "USED",
    "images": ["https://via.placeholder.com/400x300?text=Холодильник"]
  }')

if check_response "$PRODUCT4_RESPONSE" "создании товара 4"; then
    echo "✅ Товар 4 создан: Холодильник LG"
else
    echo "   Ответ API: $PRODUCT4_RESPONSE"
fi

# Товар 5 - Детская коляска
echo "   Создание товара 5: Детская коляска 3 в 1..."
PRODUCT5_RESPONSE=$(curl -s -X POST "$API_URL/api/products" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER2_TOKEN" \
  -d '{
    "title": "Детская коляска 3 в 1",
    "description": "Универсальная коляска для новорожденных. Люлька, прогулочный блок, автокресло.",
    "price": 12000,
    "categoryId": 3,
    "condition": "USED",
    "images": ["https://via.placeholder.com/400x300?text=Коляска"]
  }')

if check_response "$PRODUCT5_RESPONSE" "создании товара 5"; then
    echo "✅ Товар 5 создан: Детская коляска 3 в 1"
else
    echo "   Ответ API: $PRODUCT5_RESPONSE"
fi

# Товар 6 - IPhone
echo "   Создание товара 6: iPhone 13 Pro 128GB..."
PRODUCT6_RESPONSE=$(curl -s -X POST "$API_URL/api/products" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER2_TOKEN" \
  -d '{
    "title": "iPhone 13 Pro 128GB",
    "description": "Отличное состояние, полный комплект, без царапин.",
    "price": 45000,
    "categoryId": 2,
    "condition": "USED",
    "images": ["https://via.placeholder.com/400x300?text=iPhone"]
  }')

if check_response "$PRODUCT6_RESPONSE" "создании товара 6"; then
    echo "✅ Товар 6 создан: iPhone 13 Pro"
else
    echo "   Ответ API: $PRODUCT6_RESPONSE"
fi

# 5. Создание услуг для пользователя 1
echo ""
echo "🔧 Создание услуг для пользователя 1..."
echo ""

# Услуга 1 - Ремонт квартир
echo "   Создание услуги 1: Ремонт квартир..."
SERVICE1_RESPONSE=$(curl -s -X POST "$API_URL/api/services" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  -d '{
    "title": "Ремонт квартир под ключ",
    "description": "Качественный ремонт квартир любой сложности. Все виды работ: штукатурка, шпаклевка, покраска, обои, полы, потолки. Опыт 10 лет. Гарантия на работы.",
    "price": "50000",
    "categoryId": 16,
    "images": ["https://via.placeholder.com/400x300?text=Ремонт"]
  }')

if check_response "$SERVICE1_RESPONSE" "создании услуги 1"; then
    echo "✅ Услуга 1 создана: Ремонт квартир"
else
    echo "   Ответ API: $SERVICE1_RESPONSE"
fi

# Услуга 2 - Репетиторство по математике
echo "   Создание услуги 2: Репетиторство по математике..."
SERVICE2_RESPONSE=$(curl -s -X POST "$API_URL/api/services" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  -d '{
    "title": "Репетиторство по математике",
    "description": "Помогу подготовиться к ОГЭ/ЕГЭ по математике. Опыт преподавания 5 лет. Индивидуальный подход к каждому ученику.",
    "price": "1000",
    "categoryId": 18,
    "images": ["https://via.placeholder.com/400x300?text=Репетитор"]
  }')

if check_response "$SERVICE2_RESPONSE" "создании услуги 2"; then
    echo "✅ Услуга 2 создана: Репетиторство по математике"
else
    echo "   Ответ API: $SERVICE2_RESPONSE"
fi

# Услуга 3 - Компьютерная помощь
echo "   Создание услуги 3: Компьютерная помощь..."
SERVICE3_RESPONSE=$(curl -s -X POST "$API_URL/api/services" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER1_TOKEN" \
  -d '{
    "title": "Компьютерная помощь на дому",
    "description": "Установка Windows, настройка компьютера, удаление вирусов, восстановление данных. Быстро и качественно.",
    "price": null,
    "categoryId": 22,
    "images": ["https://via.placeholder.com/400x300?text=Компьютеры"]
  }')

if check_response "$SERVICE3_RESPONSE" "создании услуги 3"; then
    echo "✅ Услуга 3 создана: Компьютерная помощь"
else
    echo "   Ответ API: $SERVICE3_RESPONSE"
fi

# 6. Создание услуг для пользователя 2
echo ""
echo "🔧 Создание услуг для пользователя 2..."
echo ""

# Услуга 4 - Уборка квартир
echo "   Создание услуги 4: Уборка квартир..."
SERVICE4_RESPONSE=$(curl -s -X POST "$API_URL/api/services" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER2_TOKEN" \
  -d '{
    "title": "Уборка квартир и домов",
    "description": "Профессиональная уборка квартир, домов, офисов. Генеральная уборка, поддерживающая уборка. Все средства свои. Качество гарантирую!",
    "price": null,
    "categoryId": 17,
    "images": ["https://via.placeholder.com/400x300?text=Уборка"]
  }')

if check_response "$SERVICE4_RESPONSE" "создании услуги 4"; then
    echo "✅ Услуга 4 создана: Уборка квартир"
else
    echo "   Ответ API: $SERVICE4_RESPONSE"
fi

# Услуга 5 - Стрижка и укладка
echo "   Создание услуги 5: Стрижка и укладка..."
SERVICE5_RESPONSE=$(curl -s -X POST "$API_URL/api/services" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER2_TOKEN" \
  -d '{
    "title": "Стрижка и укладка на дому",
    "description": "Женские и мужские стрижки любой сложности. Окрашивание, укладки. Выезд на дом. Все инструменты и материалы свои.",
    "price": "1500",
    "categoryId": 19,
    "images": ["https://via.placeholder.com/400x300?text=Парикмахер"]
  }')

if check_response "$SERVICE5_RESPONSE" "создании услуги 5"; then
    echo "✅ Услуга 5 создана: Стрижка и укладка"
else
    echo "   Ответ API: $SERVICE5_RESPONSE"
fi

# Услуга 6 - Фотосъемка
echo "   Создание услуги 6: Фотосъемка мероприятий..."
SERVICE6_RESPONSE=$(curl -s -X POST "$API_URL/api/services" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $USER2_TOKEN" \
  -d '{
    "title": "Фотосъемка мероприятий",
    "description": "Профессиональная фотосъемка свадеб, дней рождения, корпоративов. Обработка фото в подарок. Портфолио по запросу.",
    "price": "5000",
    "categoryId": 24,
    "images": ["https://via.placeholder.com/400x300?text=Фото"]
  }')

if check_response "$SERVICE6_RESPONSE" "создании услуги 6"; then
    echo "✅ Услуга 6 создана: Фотосъемка мероприятий"
else
    echo "   Ответ API: $SERVICE6_RESPONSE"
fi

echo ""
echo "🎉 Тестовые данные успешно добавлены!"
echo ""

# Проверяем, сколько товаров и услуг в базе
PRODUCTS_COUNT=$(curl -s "$API_URL/api/products?page=1&pageSize=100" | grep -o '"total":[0-9]*' | sed 's/"total"://')
SERVICES_COUNT=$(curl -s "$API_URL/api/services?page=1&pageSize=100" | grep -o '"total":[0-9]*' | sed 's/"total"://')

echo "📊 Итого:"
echo "   - 2 пользователя"
echo "   - $PRODUCTS_COUNT товаров в базе данных"
echo "   - $SERVICES_COUNT услуг в базе данных"
echo ""
echo "Логин пользователя 1 (Иван Петров):"
echo "   Телефон: +79991234567"
echo "   Пароль: password123"
echo "   - 3 товара (Диван, Ноутбук, Велосипед)"
echo "   - 3 услуги (Ремонт, Репетиторство, Компьютерная помощь)"
echo ""
echo "Логин пользователя 2 (Мария Сидорова):"
echo "   Телефон: +79997654321"
echo "   Пароль: password123"
echo "   - 3 товара (Холодильник, Коляска, iPhone)"
echo "   - 3 услуги (Уборка, Стрижка, Фотосъемка)"
