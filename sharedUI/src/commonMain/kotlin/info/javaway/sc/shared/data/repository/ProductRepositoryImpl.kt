package info.javaway.sc.shared.data.repository

import info.javaway.sc.shared.data.api.ApiClient
import info.javaway.sc.shared.data.mappers.toDomain
import info.javaway.sc.shared.domain.models.Product
import info.javaway.sc.shared.domain.models.ProductCondition
import info.javaway.sc.shared.domain.models.ProductStatus
import info.javaway.sc.shared.domain.repository.ProductRepository
import info.javaway.sc.api.models.ProductCondition as ApiProductCondition
import info.javaway.sc.api.models.ProductStatus as ApiProductStatus

/**
 * Реализация репозитория товаров
 * Преобразует DTO в Domain модели
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
    ): Result<List<Product>> {
        val result = apiClient.getProducts(
            categoryId = categoryId,
            status = status?.toApi(),
            condition = condition?.toApi(),
            minPrice = minPrice,
            maxPrice = maxPrice,
            search = search,
            page = page,
            pageSize = pageSize
        )

        return when (result) {
            is Result.Success -> {
                val domainProducts = result.data.products.map { it.toDomain() }
                Result.Success(domainProducts)
            }
            is Result.Error -> result
        }
    }

    override suspend fun getProduct(productId: Long): kotlin.Result<Product> {
        val result = apiClient.getProduct(productId)
        return when (result) {
            is Result.Success -> Result.Success(result.data.toDomain())
            is Result.Error -> result
        }
    }

    override suspend fun getMyProducts(): kotlin.Result<List<Product>> {
        val result = apiClient.getMyProducts()
        return when (result) {
            is Result.Success -> Result.Success(result.data.map { it.toDomain() })
            is Result.Error -> result
        }
    }

    override suspend fun getFavoriteProducts(page: Int, pageSize: Int): kotlin.Result<List<Product>> {
        val result = apiClient.getFavoriteProducts(page, pageSize)
        return when (result) {
            is Result.Success -> Result.Success(result.data.products.map { it.toDomain() })
            is Result.Error -> result
        }
    }

    override suspend fun addToFavorites(productId: Long): kotlin.Result<Unit> {
        val result = apiClient.addToFavorites(productId)
        return when (result) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> result
        }
    }

    override suspend fun removeFromFavorites(productId: Long): kotlin.Result<Unit> {
        val result = apiClient.removeFromFavorites(productId)
        return when (result) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> result
        }
    }

    // Мапперы Domain → API для параметров фильтров
    private fun ProductStatus.toApi(): ApiProductStatus {
        return when (this) {
            ProductStatus.ACTIVE -> ApiProductStatus.ACTIVE
            ProductStatus.SOLD -> ApiProductStatus.SOLD
            ProductStatus.ARCHIVED -> ApiProductStatus.ARCHIVED
        }
    }

    private fun ProductCondition.toApi(): ApiProductCondition {
        return when (this) {
            ProductCondition.NEW -> ApiProductCondition.NEW
            ProductCondition.USED -> ApiProductCondition.USED
        }
    }
}
