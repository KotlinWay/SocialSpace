package info.javaway.sc.shared.domain.repository

import info.javaway.sc.api.models.*

/**
 * Репозиторий для работы с товарами
 */
interface ProductRepository {
    suspend fun getProducts(
        categoryId: Long? = null,
        status: ProductStatus? = null,
        condition: ProductCondition? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        search: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): Result<ProductListResponse>

    suspend fun getProduct(productId: Long): Result<ProductResponse>
    suspend fun getMyProducts(): Result<List<ProductResponse>>
    suspend fun getFavoriteProducts(page: Int = 1, pageSize: Int = 20): Result<ProductListResponse>
    suspend fun addToFavorites(productId: Long): Result<SuccessResponse>
    suspend fun removeFromFavorites(productId: Long): Result<SuccessResponse>
}
