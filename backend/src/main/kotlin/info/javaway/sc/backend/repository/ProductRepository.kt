package info.javaway.sc.backend.repository

import info.javaway.sc.backend.data.tables.Categories
import info.javaway.sc.backend.data.tables.Favorites
import info.javaway.sc.backend.data.tables.Products
import info.javaway.sc.backend.data.tables.Users
import info.javaway.sc.api.models.Product
import info.javaway.sc.api.models.ProductCondition
import info.javaway.sc.api.models.ProductStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

/**
 * Repository для работы с товарами в базе данных
 */
class ProductRepository {

    /**
     * Создать новый товар
     */
    fun createProduct(
        userId: Long,
        title: String,
        description: String,
        price: Double,
        categoryId: Long,
        condition: ProductCondition,
        images: List<String>
    ): Product? = transaction {
        val now = Instant.now()
        val imagesJson = Json.encodeToString(images)

        val insertStatement = Products.insert {
            it[Products.userId] = userId
            it[Products.title] = title
            it[Products.description] = description
            it[Products.price] = price
            it[Products.categoryId] = categoryId
            it[Products.condition] = condition
            it[Products.images] = imagesJson
            it[Products.status] = ProductStatus.ACTIVE
            it[Products.views] = 0
            it[Products.createdAt] = now
            it[Products.updatedAt] = now
        }

        insertStatement.resultedValues?.singleOrNull()?.let { rowToProduct(it) }
    }

    /**
     * Найти товар по ID
     */
    fun findById(productId: Long): Product? = transaction {
        Products.selectAll()
            .where { Products.id eq productId }
            .map { rowToProduct(it) }
            .singleOrNull()
    }

    /**
     * Найти все товары пользователя
     */
    fun findByUserId(userId: Long, limit: Int = 100, offset: Long = 0): List<Product> = transaction {
        Products.selectAll()
            .where { Products.userId eq userId }
            .limit(limit)
            .offset(offset)
            .orderBy(Products.createdAt to SortOrder.DESC)
            .map { rowToProduct(it) }
    }

    /**
     * Получить все товары с фильтрацией и пагинацией
     */
    fun getAllProducts(
        categoryId: Long? = null,
        status: ProductStatus? = null,
        condition: ProductCondition? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        searchQuery: String? = null,
        limit: Int = 20,
        offset: Long = 0
    ): List<Product> = transaction {
        println("🔍 ProductRepository.getAllProducts() вызван")
        println("   categoryId=$categoryId, status=$status, condition=$condition")
        println("   minPrice=$minPrice, maxPrice=$maxPrice, searchQuery=$searchQuery")
        println("   limit=$limit, offset=$offset")

        var query = Products.selectAll()

        // Фильтр по категории
        categoryId?.let {
            println("   ✅ Применяется фильтр по categoryId: $it")
            query = query.where { Products.categoryId eq it }
        } ?: println("   ℹ️  Фильтр по categoryId НЕ применяется (categoryId == null) - вернем ВСЕ товары")

        // Фильтр по статусу
        status?.let {
            println("   ✅ Применяется фильтр по status: $it")
            query = query.where { Products.status eq it }
        }

        // Фильтр по состоянию
        condition?.let {
            println("   ✅ Применяется фильтр по condition: $it")
            query = query.where { Products.condition eq it }
        }

        // Фильтр по минимальной цене
        minPrice?.let {
            println("   ✅ Применяется фильтр по minPrice: $it")
            query = query.where { Products.price greaterEq it }
        }

        // Фильтр по максимальной цене
        maxPrice?.let {
            println("   ✅ Применяется фильтр по maxPrice: $it")
            query = query.where { Products.price lessEq it }
        }

        // Поиск по названию и описанию
        searchQuery?.let { search ->
            println("   ✅ Применяется поиск по запросу: $search")
            val searchPattern = "%${search.lowercase()}%"
            query = query.where {
                (Products.title.lowerCase() like searchPattern) or
                (Products.description.lowerCase() like searchPattern)
            }
        }

        val results = query
            .limit(limit)
            .offset(offset)
            .orderBy(Products.createdAt to SortOrder.DESC)
            .map { rowToProduct(it) }

        println("   📦 Найдено товаров: ${results.size}")

        results
    }

    /**
     * Подсчитать общее количество товаров с учетом фильтров
     */
    fun countProducts(
        categoryId: Long? = null,
        status: ProductStatus? = null,
        condition: ProductCondition? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        searchQuery: String? = null
    ): Long = transaction {
        println("🔢 ProductRepository.countProducts() вызван")
        println("   categoryId=$categoryId, status=$status, condition=$condition")

        var query = Products.selectAll()

        categoryId?.let {
            println("   ✅ Применяется фильтр по categoryId: $it")
            query = query.where { Products.categoryId eq it }
        } ?: println("   ℹ️  Фильтр по categoryId НЕ применяется (categoryId == null)")

        status?.let {
            println("   ✅ Применяется фильтр по status: $it")
            query = query.where { Products.status eq it }
        }
        condition?.let {
            println("   ✅ Применяется фильтр по condition: $it")
            query = query.where { Products.condition eq it }
        }
        minPrice?.let {
            println("   ✅ Применяется фильтр по minPrice: $it")
            query = query.where { Products.price greaterEq it }
        }
        maxPrice?.let {
            println("   ✅ Применяется фильтр по maxPrice: $it")
            query = query.where { Products.price lessEq it }
        }

        searchQuery?.let { search ->
            println("   ✅ Применяется поиск по запросу: $search")
            val searchPattern = "%${search.lowercase()}%"
            query = query.where {
                (Products.title.lowerCase() like searchPattern) or
                (Products.description.lowerCase() like searchPattern)
            }
        }

        val count = query.count()
        println("   📊 Всего товаров (с учетом фильтров): $count")

        count
    }

    /**
     * Обновить товар
     */
    fun updateProduct(
        productId: Long,
        title: String? = null,
        description: String? = null,
        price: Double? = null,
        categoryId: Long? = null,
        condition: ProductCondition? = null,
        status: ProductStatus? = null,
        images: List<String>? = null
    ): Product? = transaction {
        val now = Instant.now()

        Products.update({ Products.id eq productId }) {
            title?.let { value -> it[Products.title] = value }
            description?.let { value -> it[Products.description] = value }
            price?.let { value -> it[Products.price] = value }
            categoryId?.let { value -> it[Products.categoryId] = value }
            condition?.let { value -> it[Products.condition] = value }
            status?.let { value -> it[Products.status] = value }
            images?.let { value -> it[Products.images] = Json.encodeToString(value) }
            it[Products.updatedAt] = now
        }

        findById(productId)
    }

    /**
     * Удалить товар
     */
    fun deleteProduct(productId: Long): Boolean = transaction {
        // Сначала удаляем все записи из favorites
        Favorites.deleteWhere { Favorites.productId eq productId }

        // Затем удаляем сам товар
        Products.deleteWhere { Products.id eq productId } > 0
    }

    /**
     * Увеличить счетчик просмотров товара
     */
    fun incrementViews(productId: Long): Product? = transaction {
        Products.update({ Products.id eq productId }) {
            it[Products.views] = Products.views + 1
        }

        findById(productId)
    }

    /**
     * Добавить товар в избранное
     */
    fun addToFavorites(userId: Long, productId: Long): Boolean = transaction {
        try {
            Favorites.insert {
                it[Favorites.userId] = userId
                it[Favorites.productId] = productId
                it[Favorites.createdAt] = Instant.now()
            }
            true
        } catch (e: Exception) {
            // Если запись уже существует (уникальный индекс), возвращаем false
            false
        }
    }

    /**
     * Удалить товар из избранного
     */
    fun removeFromFavorites(userId: Long, productId: Long): Boolean = transaction {
        Favorites.deleteWhere {
            (Favorites.userId eq userId) and (Favorites.productId eq productId)
        } > 0
    }

    /**
     * Проверить, находится ли товар в избранном у пользователя
     */
    fun isFavorite(userId: Long, productId: Long): Boolean = transaction {
        Favorites.selectAll()
            .where { (Favorites.userId eq userId) and (Favorites.productId eq productId) }
            .count() > 0
    }

    /**
     * Получить избранные товары пользователя
     */
    fun getFavorites(userId: Long, limit: Int = 100, offset: Long = 0): List<Product> = transaction {
        (Products innerJoin Favorites)
            .select(Products.columns)
            .where { Favorites.userId eq userId }
            .limit(limit)
            .offset(offset)
            .orderBy(Favorites.createdAt to SortOrder.DESC)
            .map { rowToProduct(it) }
    }

    /**
     * Подсчитать количество избранных товаров пользователя
     */
    fun countFavorites(userId: Long): Long = transaction {
        Favorites.selectAll()
            .where { Favorites.userId eq userId }
            .count()
    }

    /**
     * Проверить, принадлежит ли товар пользователю
     */
    fun isOwner(userId: Long, productId: Long): Boolean = transaction {
        Products.selectAll()
            .where { (Products.id eq productId) and (Products.userId eq userId) }
            .count() > 0
    }

    /**
     * Преобразовать строку БД в модель Product
     */
    private fun rowToProduct(row: ResultRow): Product {
        val imagesJson = row[Products.images]
        val images = try {
            Json.decodeFromString<List<String>>(imagesJson)
        } catch (e: Exception) {
            emptyList()
        }

        return Product(
            id = row[Products.id].value,
            userId = row[Products.userId].value,
            title = row[Products.title],
            description = row[Products.description],
            price = row[Products.price],
            categoryId = row[Products.categoryId].value,
            condition = row[Products.condition],
            images = images,
            status = row[Products.status],
            views = row[Products.views],
            createdAt = row[Products.createdAt].toString(),
            updatedAt = row[Products.updatedAt].toString()
        )
    }
}
