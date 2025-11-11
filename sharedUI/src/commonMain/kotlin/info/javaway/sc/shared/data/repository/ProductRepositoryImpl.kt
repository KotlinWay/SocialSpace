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
    ): kotlin.Result<List<Product>> {
        return apiClient.getProducts(
            categoryId = categoryId,
            status = status?.toApi(),
            condition = condition?.toApi(),
            minPrice = minPrice,
            maxPrice = maxPrice,
            search = search,
            page = page,
            pageSize = pageSize
        ).fold(
            onSuccess = { response ->
                val domainProducts = response.products.map { it.toDomain() }
                kotlin.Result.success(domainProducts)
            },
            onFailure = { exception ->
                kotlin.Result.failure(exception)
            }
        )
    }

    override suspend fun getProduct(productId: Long): kotlin.Result<Product> {
        return apiClient.getProduct(productId).fold(
            onSuccess = { product ->
                kotlin.Result.success(product.toDomain())
            },
            onFailure = { exception ->
                kotlin.Result.failure(exception)
            }
        )
    }

    override suspend fun getMyProducts(): kotlin.Result<List<Product>> {
        return apiClient.getMyProducts().fold(
            onSuccess = { products ->
                kotlin.Result.success(products.map { it.toDomain() })
            },
            onFailure = { exception ->
                kotlin.Result.failure(exception)
            }
        )
    }

    override suspend fun getFavoriteProducts(page: Int, pageSize: Int): kotlin.Result<List<Product>> {
        return apiClient.getFavoriteProducts(page, pageSize).fold(
            onSuccess = { response ->
                kotlin.Result.success(response.products.map { it.toDomain() })
            },
            onFailure = { exception ->
                kotlin.Result.failure(exception)
            }
        )
    }

    override suspend fun addToFavorites(productId: Long): kotlin.Result<Unit> {
        return apiClient.addToFavorites(productId).fold(
            onSuccess = {
                kotlin.Result.success(Unit)
            },
            onFailure = { exception ->
                kotlin.Result.failure(exception)
            }
        )
    }

    override suspend fun removeFromFavorites(productId: Long): kotlin.Result<Unit> {
        return apiClient.removeFromFavorites(productId).fold(
            onSuccess = {
                kotlin.Result.success(Unit)
            },
            onFailure = { exception ->
                kotlin.Result.failure(exception)
            }
        )
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
