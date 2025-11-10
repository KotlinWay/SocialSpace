package info.javaway.sc.shared.data.repository

import info.javaway.sc.shared.data.api.ApiClient
import info.javaway.sc.api.models.*
import info.javaway.sc.shared.domain.repository.ProductRepository

/**
 * Реализация репозитория товаров
 */
class ProductRepositoryImpl(
    private val apiClient: ApiClient
) : ProductRepository {

    override suspend fun getProducts(
        categoryId: Long?,
        status: ProductStatus?,
        condition: ProductCondition?,
        minPrice: Double?,
        maxPrice: Double?,
        search: String?,
        page: Int,
        pageSize: Int
    ): Result<ProductListResponse> {
        return apiClient.getProducts(
            categoryId = categoryId,
            status = status,
            condition = condition,
            minPrice = minPrice,
            maxPrice = maxPrice,
            search = search,
            page = page,
            pageSize = pageSize
        )
    }

    override suspend fun getProduct(productId: Long): kotlin.Result<ProductResponse> {
        return apiClient.getProduct(productId)
    }

    override suspend fun getMyProducts(): kotlin.Result<List<ProductResponse>> {
        return apiClient.getMyProducts()
    }

    override suspend fun getFavoriteProducts(page: Int, pageSize: Int): kotlin.Result<ProductListResponse> {
        return apiClient.getFavoriteProducts(page, pageSize)
    }

    override suspend fun addToFavorites(productId: Long): kotlin.Result<SuccessResponse> {
        return apiClient.addToFavorites(productId)
    }

    override suspend fun removeFromFavorites(productId: Long): kotlin.Result<SuccessResponse> {
        return apiClient.removeFromFavorites(productId)
    }
}
