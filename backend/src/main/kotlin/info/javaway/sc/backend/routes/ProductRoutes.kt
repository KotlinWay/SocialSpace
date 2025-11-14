package info.javaway.sc.backend.routes

import info.javaway.sc.api.models.*
import info.javaway.sc.backend.repository.CategoryRepository
import info.javaway.sc.backend.repository.ProductRepository
import info.javaway.sc.backend.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.math.ceil

/**
 * Маршруты для работы с товарами
 */
fun Route.productRoutes(
    productRepository: ProductRepository = ProductRepository(),
    userRepository: UserRepository = UserRepository(),
    categoryRepository: CategoryRepository = CategoryRepository()
) {

    route("/products") {

        /**
         * GET /api/products
         * Получить список товаров с фильтрацией и пагинацией
         *
         * Query параметры:
         * - categoryId: Long? - фильтр по категории
         * - status: String? - фильтр по статусу (ACTIVE, SOLD, ARCHIVED)
         * - condition: String? - фильтр по состоянию (NEW, USED)
         * - minPrice: Double? - минимальная цена
         * - maxPrice: Double? - максимальная цена
         * - search: String? - поиск по названию и описанию
         * - page: Int? - номер страницы (по умолчанию 1)
         * - pageSize: Int? - размер страницы (по умолчанию 20, максимум 100)
         */
        get {
            try {
                println("🌐 GET /api/products - Получен запрос")
                println("   Query parameters: ${call.request.queryParameters}")

                val categoryId = call.request.queryParameters["categoryId"]?.toLongOrNull()
                val statusStr = call.request.queryParameters["status"]
                val conditionStr = call.request.queryParameters["condition"]
                val minPrice = call.request.queryParameters["minPrice"]?.toDoubleOrNull()
                val maxPrice = call.request.queryParameters["maxPrice"]?.toDoubleOrNull()
                val searchQuery = call.request.queryParameters["search"]
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull()?.coerceIn(1, 100) ?: 20

                println("   Распарсены параметры:")
                println("   - categoryId: $categoryId")
                println("   - status: $statusStr")
                println("   - condition: $conditionStr")
                println("   - minPrice: $minPrice")
                println("   - maxPrice: $maxPrice")
                println("   - search: $searchQuery")
                println("   - page: $page")
                println("   - pageSize: $pageSize")

                // Валидация page
                if (page < 1) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("INVALID_PAGE", "Номер страницы должен быть больше 0")
                    )
                    return@get
                }

                // Парсинг enum значений
                val status = statusStr?.let {
                    try {
                        ProductStatus.valueOf(it)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }

                val condition = conditionStr?.let {
                    try {
                        ProductCondition.valueOf(it)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }

                val offset = ((page - 1) * pageSize).toLong()

                println("   📞 Вызов productRepository.getAllProducts()...")
                // Получаем товары и их количество
                val products = productRepository.getAllProducts(
                    categoryId = categoryId,
                    status = status,
                    condition = condition,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    searchQuery = searchQuery,
                    limit = pageSize,
                    offset = offset
                )

                println("   📞 Вызов productRepository.countProducts()...")
                val total = productRepository.countProducts(
                    categoryId = categoryId,
                    status = status,
                    condition = condition,
                    minPrice = minPrice,
                    maxPrice = maxPrice,
                    searchQuery = searchQuery
                )

                val totalPages = ceil(total.toDouble() / pageSize).toInt()

                // Получаем ID текущего пользователя (если авторизован)
                val principal = call.principal<JWTPrincipal>()
                val currentUserId = principal?.payload?.getClaim("userId")?.asLong()

                println("   🔄 Формирование расширенных данных для ${products.size} товаров...")

                // Формируем расширенные данные для каждого товара
                val productListItems = products.mapNotNull { product ->
                    // Загружаем пользователя
                    val user = userRepository.findById(product.userId)
                    if (user == null) {
                        println("   ⚠️  Пользователь ${product.userId} не найден для товара ${product.id}")
                        return@mapNotNull null
                    }

                    // Загружаем категорию
                    val category = categoryRepository.findById(product.categoryId)
                    if (category == null) {
                        println("   ⚠️  Категория ${product.categoryId} не найдена для товара ${product.id}")
                        return@mapNotNull null
                    }

                    // Проверяем, в избранном ли товар
                    val isFavorite = currentUserId?.let {
                        productRepository.isFavorite(it, product.id)
                    } ?: false

                    ProductListItem(
                        id = product.id,
                        title = product.title,
                        description = product.description,
                        price = product.price,
                        condition = product.condition,
                        images = product.images,
                        status = product.status,
                        views = product.views,
                        createdAt = product.createdAt,
                        updatedAt = product.updatedAt,
                        user = UserPublicInfo(
                            id = user.id,
                            name = user.name,
                            avatar = user.avatar,
                            phone = user.phone,
                            rating = user.rating,
                            isVerified = user.isVerified
                        ),
                        category = CategoryInfo(
                            id = category.id,
                            name = category.name,
                            icon = category.icon
                        ),
                        isFavorite = isFavorite
                    )
                }

                val response = ProductListResponse(
                    products = productListItems,
                    total = total,
                    page = page,
                    pageSize = pageSize,
                    totalPages = totalPages
                )

                println("   ✅ Ответ сформирован: products.size=${productListItems.size}, total=$total, totalPages=$totalPages")
                call.respond(HttpStatusCode.OK, response)
            } catch (e: Exception) {
                call.application.log.error("Get products error", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", "Ошибка при получении списка товаров")
                )
            }
        }

        /**
         * GET /api/products/{id}
         * Получить детальную информацию о товаре
         */
        get("/{id}") {
            try {
                val productId = call.parameters["id"]?.toLongOrNull()

                if (productId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("INVALID_ID", "Неверный ID товара")
                    )
                    return@get
                }

                val product = productRepository.findById(productId)
                if (product == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("PRODUCT_NOT_FOUND", "Товар не найден")
                    )
                    return@get
                }

                // Увеличиваем счетчик просмотров
                productRepository.incrementViews(productId)

                // Получаем информацию о пользователе
                val user = userRepository.findById(product.userId)
                if (user == null) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("USER_NOT_FOUND", "Пользователь товара не найден")
                    )
                    return@get
                }

                // Получаем информацию о категории
                val category = categoryRepository.findById(product.categoryId)
                if (category == null) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("CATEGORY_NOT_FOUND", "Категория товара не найдена")
                    )
                    return@get
                }

                // Проверяем, авторизован ли пользователь и в избранном ли товар
                val principal = call.principal<JWTPrincipal>()
                val currentUserId = principal?.payload?.getClaim("userId")?.asLong()
                val isFavorite = currentUserId?.let { productRepository.isFavorite(it, productId) } ?: false

                val response = ProductResponse(
                    product = product,
                    user = UserPublicInfo(
                        id = user.id,
                        name = user.name,
                        avatar = user.avatar,
                        phone = user.phone,
                        rating = user.rating,
                        isVerified = user.isVerified
                    ),
                    category = CategoryInfo(
                        id = category.id,
                        name = category.name,
                        icon = category.icon
                    ),
                    isFavorite = isFavorite
                )

                call.respond(HttpStatusCode.OK, response)
            } catch (e: Exception) {
                call.application.log.error("Get product details error", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", "Ошибка при получении товара")
                )
            }
        }

        /**
         * Защищенные маршруты (требуют аутентификации)
         */
        authenticate("auth-jwt") {

            /**
             * GET /api/products/my
             * Получить список товаров текущего пользователя
             */
            get("/my") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()

                    if (userId == null) {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse("UNAUTHORIZED", "Требуется авторизация")
                        )
                        return@get
                    }

                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull()?.coerceIn(1, 100) ?: 20
                    val offset = ((page - 1) * pageSize).toLong()

                    val products = productRepository.findByUserId(userId, limit = pageSize, offset = offset)

                    call.respond(HttpStatusCode.OK, products)
                } catch (e: Exception) {
                    call.application.log.error("Get my products error", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("SERVER_ERROR", "Ошибка при получении товаров пользователя")
                    )
                }
            }

            /**
             * POST /api/products
             * Создать новый товар
             */
            post {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()

                    if (userId == null) {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse("UNAUTHORIZED", "Требуется авторизация")
                        )
                        return@post
                    }

                    val request = call.receive<CreateProductRequest>()

                    // Валидация
                    if (request.title.isBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_TITLE", "Название не может быть пустым")
                        )
                        return@post
                    }

                    if (request.title.length > 200) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_TITLE", "Название не может быть длиннее 200 символов")
                        )
                        return@post
                    }

                    if (request.description.isBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_DESCRIPTION", "Описание не может быть пустым")
                        )
                        return@post
                    }

                    if (request.price < 0) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_PRICE", "Цена не может быть отрицательной")
                        )
                        return@post
                    }

                    if (request.images.isEmpty()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_IMAGES", "Необходимо добавить хотя бы одно изображение")
                        )
                        return@post
                    }

                    if (request.images.size > 5) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_IMAGES", "Максимум 5 изображений")
                        )
                        return@post
                    }

                    // Проверяем существование категории
                    val category = categoryRepository.findById(request.categoryId)
                    if (category == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_CATEGORY", "Категория не найдена")
                        )
                        return@post
                    }

                    if (category.type != CategoryType.PRODUCT) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_CATEGORY", "Указанная категория не предназначена для товаров")
                        )
                        return@post
                    }

                    val product = productRepository.createProduct(
                        userId = userId,
                        title = request.title,
                        description = request.description,
                        price = request.price,
                        categoryId = request.categoryId,
                        condition = request.condition,
                        images = request.images
                    )

                    if (product != null) {
                        // Получаем информацию о пользователе
                        val user = userRepository.findById(userId)
                        if (user == null) {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                ErrorResponse("USER_NOT_FOUND", "Пользователь не найден")
                            )
                            return@post
                        }

                        // Получаем информацию о категории (уже проверяли выше, но на всякий случай)
                        val categoryInfo = categoryRepository.findById(product.categoryId)
                        if (categoryInfo == null) {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                ErrorResponse("CATEGORY_NOT_FOUND", "Категория не найдена")
                            )
                            return@post
                        }

                        val response = ProductResponse(
                            product = product,
                            user = UserPublicInfo(
                                id = user.id,
                                name = user.name,
                                avatar = user.avatar,
                                phone = user.phone,
                                rating = user.rating,
                                isVerified = user.isVerified
                            ),
                            category = CategoryInfo(
                                id = categoryInfo.id,
                                name = categoryInfo.name,
                                icon = categoryInfo.icon
                            ),
                            isFavorite = false
                        )

                        call.respond(HttpStatusCode.Created, response)
                    } else {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse("CREATE_ERROR", "Ошибка при создании товара")
                        )
                    }
                } catch (e: Exception) {
                    call.application.log.error("Create product error", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("SERVER_ERROR", "Ошибка при создании товара")
                    )
                }
            }

            /**
             * PUT /api/products/{id}
             * Обновить товар (только свой)
             */
            put("/{id}") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()
                    val productId = call.parameters["id"]?.toLongOrNull()

                    if (userId == null || productId == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_REQUEST", "Неверный запрос")
                        )
                        return@put
                    }

                    // Проверяем, что товар принадлежит пользователю
                    if (!productRepository.isOwner(userId, productId)) {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse("FORBIDDEN", "Нет прав для редактирования этого товара")
                        )
                        return@put
                    }

                    val request = call.receive<UpdateProductRequest>()

                    // Валидация
                    request.title?.let {
                        if (it.isBlank() || it.length > 200) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("INVALID_TITLE", "Название должно быть от 1 до 200 символов")
                            )
                            return@put
                        }
                    }

                    request.description?.let {
                        if (it.isBlank()) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("INVALID_DESCRIPTION", "Описание не может быть пустым")
                            )
                            return@put
                        }
                    }

                    request.price?.let {
                        if (it < 0) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("INVALID_PRICE", "Цена не может быть отрицательной")
                            )
                            return@put
                        }
                    }

                    request.images?.let {
                        if (it.isEmpty() || it.size > 5) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("INVALID_IMAGES", "Количество изображений должно быть от 1 до 5")
                            )
                            return@put
                        }
                    }

                    request.categoryId?.let {
                        val category = categoryRepository.findById(it)
                        if (category == null || category.type != CategoryType.PRODUCT) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("INVALID_CATEGORY", "Неверная категория")
                            )
                            return@put
                        }
                    }

                    val updatedProduct = productRepository.updateProduct(
                        productId = productId,
                        title = request.title,
                        description = request.description,
                        price = request.price,
                        categoryId = request.categoryId,
                        condition = request.condition,
                        status = request.status,
                        images = request.images
                    )

                    if (updatedProduct != null) {
                        // Получаем информацию о пользователе
                        val user = userRepository.findById(updatedProduct.userId)
                        if (user == null) {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                ErrorResponse("USER_NOT_FOUND", "Пользователь не найден")
                            )
                            return@put
                        }

                        // Получаем информацию о категории
                        val categoryInfo = categoryRepository.findById(updatedProduct.categoryId)
                        if (categoryInfo == null) {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                ErrorResponse("CATEGORY_NOT_FOUND", "Категория не найдена")
                            )
                            return@put
                        }

                        // Проверяем, в избранном ли товар
                        val isFavorite = productRepository.isFavorite(userId, productId)

                        val response = ProductResponse(
                            product = updatedProduct,
                            user = UserPublicInfo(
                                id = user.id,
                                name = user.name,
                                avatar = user.avatar,
                                phone = user.phone,
                                rating = user.rating,
                                isVerified = user.isVerified
                            ),
                            category = CategoryInfo(
                                id = categoryInfo.id,
                                name = categoryInfo.name,
                                icon = categoryInfo.icon
                            ),
                            isFavorite = isFavorite
                        )

                        call.respond(HttpStatusCode.OK, response)
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("PRODUCT_NOT_FOUND", "Товар не найден")
                        )
                    }
                } catch (e: Exception) {
                    call.application.log.error("Update product error", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("SERVER_ERROR", "Ошибка при обновлении товара")
                    )
                }
            }

            /**
             * DELETE /api/products/{id}
             * Удалить товар (только свой)
             */
            delete("/{id}") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()
                    val productId = call.parameters["id"]?.toLongOrNull()

                    if (userId == null || productId == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_REQUEST", "Неверный запрос")
                        )
                        return@delete
                    }

                    // Проверяем, что товар принадлежит пользователю
                    if (!productRepository.isOwner(userId, productId)) {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse("FORBIDDEN", "Нет прав для удаления этого товара")
                        )
                        return@delete
                    }

                    val deleted = productRepository.deleteProduct(productId)
                    if (deleted) {
                        call.respond(
                            HttpStatusCode.OK,
                            SuccessResponse(true, "Товар успешно удален")
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("PRODUCT_NOT_FOUND", "Товар не найден")
                        )
                    }
                } catch (e: Exception) {
                    call.application.log.error("Delete product error", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("SERVER_ERROR", "Ошибка при удалении товара")
                    )
                }
            }

            /**
             * POST /api/products/{id}/favorite
             * Добавить товар в избранное
             */
            post("/{id}/favorite") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()
                    val productId = call.parameters["id"]?.toLongOrNull()

                    if (userId == null || productId == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_REQUEST", "Неверный запрос")
                        )
                        return@post
                    }

                    // Проверяем существование товара
                    val product = productRepository.findById(productId)
                    if (product == null) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("PRODUCT_NOT_FOUND", "Товар не найден")
                        )
                        return@post
                    }

                    val added = productRepository.addToFavorites(userId, productId)
                    if (added) {
                        call.respond(
                            HttpStatusCode.OK,
                            SuccessResponse(true, "Товар добавлен в избранное")
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.Conflict,
                            ErrorResponse("ALREADY_IN_FAVORITES", "Товар уже в избранном")
                        )
                    }
                } catch (e: Exception) {
                    call.application.log.error("Add to favorites error", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("SERVER_ERROR", "Ошибка при добавлении в избранное")
                    )
                }
            }

            /**
             * DELETE /api/products/{id}/favorite
             * Удалить товар из избранного
             */
            delete("/{id}/favorite") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()
                    val productId = call.parameters["id"]?.toLongOrNull()

                    if (userId == null || productId == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_REQUEST", "Неверный запрос")
                        )
                        return@delete
                    }

                    val removed = productRepository.removeFromFavorites(userId, productId)
                    if (removed) {
                        call.respond(
                            HttpStatusCode.OK,
                            SuccessResponse(true, "Товар удален из избранного")
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("NOT_IN_FAVORITES", "Товар не найден в избранном")
                        )
                    }
                } catch (e: Exception) {
                    call.application.log.error("Remove from favorites error", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("SERVER_ERROR", "Ошибка при удалении из избранного")
                    )
                }
            }

            /**
             * GET /api/products/favorites
             * Получить список избранных товаров
             */
            get("/favorites") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()

                    if (userId == null) {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse("UNAUTHORIZED", "Требуется авторизация")
                        )
                        return@get
                    }

                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull()?.coerceIn(1, 100) ?: 20
                    val offset = ((page - 1) * pageSize).toLong()

                    val favorites = productRepository.getFavorites(userId, limit = pageSize, offset = offset)
                    val total = productRepository.countFavorites(userId)
                    val totalPages = ceil(total.toDouble() / pageSize).toInt()

                    // Формируем расширенные данные для каждого избранного товара
                    val favoriteListItems = favorites.mapNotNull { product ->
                        // Загружаем пользователя
                        val user = userRepository.findById(product.userId)
                        if (user == null) {
                            return@mapNotNull null
                        }

                        // Загружаем категорию
                        val category = categoryRepository.findById(product.categoryId)
                        if (category == null) {
                            return@mapNotNull null
                        }

                        ProductListItem(
                            id = product.id,
                            title = product.title,
                            description = product.description,
                            price = product.price,
                            condition = product.condition,
                            images = product.images,
                            status = product.status,
                            views = product.views,
                            createdAt = product.createdAt,
                            updatedAt = product.updatedAt,
                            user = UserPublicInfo(
                                id = user.id,
                                name = user.name,
                                avatar = user.avatar,
                                phone = user.phone,
                                rating = user.rating,
                                isVerified = user.isVerified
                            ),
                            category = CategoryInfo(
                                id = category.id,
                                name = category.name,
                                icon = category.icon
                            ),
                            isFavorite = true  // Все товары в этом списке - избранные
                        )
                    }

                    val response = ProductListResponse(
                        products = favoriteListItems,
                        total = total,
                        page = page,
                        pageSize = pageSize,
                        totalPages = totalPages
                    )

                    call.respond(HttpStatusCode.OK, response)
                } catch (e: Exception) {
                    call.application.log.error("Get favorites error", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("SERVER_ERROR", "Ошибка при получении избранного")
                    )
                }
            }
        }
    }
}
