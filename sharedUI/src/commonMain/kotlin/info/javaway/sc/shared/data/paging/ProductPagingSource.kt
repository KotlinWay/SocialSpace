package info.javaway.sc.shared.data.paging

import info.javaway.sc.shared.data.api.ApiClient
import info.javaway.sc.shared.data.mappers.toDomain
import info.javaway.sc.shared.domain.models.Product
import info.javaway.sc.shared.domain.models.ProductCondition
import info.javaway.sc.shared.domain.models.ProductStatus
import info.javaway.sc.api.models.ProductCondition as ApiProductCondition
import info.javaway.sc.api.models.ProductStatus as ApiProductStatus

/**
 * Data class для фильтров товаров
 * Инкапсулирует все параметры фильтрации
 */
data class ProductFilters(
    val categoryId: Long? = null,
    val status: ProductStatus? = null,
    val condition: ProductCondition? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val search: String? = null
)

/**
 * PagingSource для загрузки товаров с сервера
 * Использует BasePagingSource для общей логики
 *
 * @param apiClient Клиент для взаимодействия с API
 * @param filters Фильтры для поиска товаров
 */
class ProductPagingSource(
    private val apiClient: ApiClient,
    private val filters: ProductFilters = ProductFilters()
) : BasePagingSource<Product>() {

    /**
     * Загрузка страницы товаров с применением фильтров
     */
    override suspend fun loadPage(page: Int, pageSize: Int): Result<List<Product>> {
        return apiClient.getProducts(
            categoryId = filters.categoryId,
            status = filters.status?.toApi(),
            condition = filters.condition?.toApi(),
            minPrice = filters.minPrice,
            maxPrice = filters.maxPrice,
            search = filters.search,
            page = page,
            pageSize = pageSize
        ).fold(
            onSuccess = { response ->
                // Преобразуем DTO в Domain модели
                val domainProducts = response.products.map { it.toDomain() }
                Result.success(domainProducts)
            },
            onFailure = { exception ->
                Result.failure(exception)
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
