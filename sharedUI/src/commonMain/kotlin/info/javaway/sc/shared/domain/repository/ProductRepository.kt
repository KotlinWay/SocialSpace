package info.javaway.sc.shared.domain.repository

import androidx.paging.PagingData
import info.javaway.sc.shared.domain.models.Product
import info.javaway.sc.shared.domain.models.ProductCondition
import info.javaway.sc.shared.domain.models.ProductStatus
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий для работы с товарами
 * Возвращает Domain модели
 */
interface ProductRepository {
    /**
     * Получить список товаров с пагинацией через Paging 3
     * Рекомендуется использовать этот метод вместо getProducts()
     *
     * @param categoryId Фильтр по категории
     * @param status Фильтр по статусу
     * @param condition Фильтр по состоянию
     * @param minPrice Минимальная цена
     * @param maxPrice Максимальная цена
     * @param search Поисковый запрос
     * @return Flow с PagingData товаров
     */
    fun getProductsPaged(
        categoryId: Long? = null,
        status: ProductStatus? = null,
        condition: ProductCondition? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        search: String? = null
    ): Flow<PagingData<Product>>

    /**
     * Получить список товаров с фильтрацией и пагинацией (Legacy)
     * Используйте getProductsPaged() для лучшей производительности
     *
     * @deprecated Используйте getProductsPaged() вместо этого метода
     */
    @Deprecated("Use getProductsPaged() instead")
    suspend fun getProducts(
        categoryId: Long? = null,
        status: ProductStatus? = null,
        condition: ProductCondition? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        search: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): Result<List<Product>>

    /**
     * Получить товар по ID
     */
    suspend fun getProduct(productId: Long): Result<Product>

    /**
     * Получить мои товары
     */
    suspend fun getMyProducts(): Result<List<Product>>

    /**
     * Получить избранные товары
     */
    suspend fun getFavoriteProducts(page: Int = 1, pageSize: Int = 20): Result<List<Product>>

    /**
     * Добавить товар в избранное
     */
    suspend fun addToFavorites(productId: Long): Result<Unit>

    /**
     * Удалить товар из избранного
     */
    suspend fun removeFromFavorites(productId: Long): Result<Unit>

    /**
     * Удалить товар
     */
    suspend fun deleteProduct(productId: Long): Result<Unit>
}
