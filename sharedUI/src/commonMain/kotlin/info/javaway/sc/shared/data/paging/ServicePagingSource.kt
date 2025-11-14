package info.javaway.sc.shared.data.paging

import info.javaway.sc.shared.data.api.ApiClient
import info.javaway.sc.shared.data.mappers.toDomain
import info.javaway.sc.shared.domain.models.Service
import info.javaway.sc.shared.domain.models.ServiceStatus
import info.javaway.sc.api.models.ServiceStatus as ApiServiceStatus

/**
 * Data class для фильтров услуг
 * Инкапсулирует все параметры фильтрации
 */
data class ServiceFilters(
    val categoryId: Long? = null,
    val status: ServiceStatus? = null,
    val search: String? = null
)

/**
 * PagingSource для загрузки услуг с сервера
 * Использует BasePagingSource для общей логики
 *
 * @param apiClient Клиент для взаимодействия с API
 * @param filters Фильтры для поиска услуг
 */
class ServicePagingSource(
    private val apiClient: ApiClient,
    private val filters: ServiceFilters = ServiceFilters()
) : BasePagingSource<Service>() {

    /**
     * Загрузка страницы услуг с применением фильтров
     */
    override suspend fun loadPage(page: Int, pageSize: Int): Result<List<Service>> {
        return apiClient.getServices(
            categoryId = filters.categoryId,
            status = filters.status?.toApi(),
            search = filters.search,
            page = page,
            pageSize = pageSize
        ).fold(
            onSuccess = { response ->
                // Преобразуем DTO в Domain модели
                val domainServices = response.services.map { it.toDomain() }
                Result.success(domainServices)
            },
            onFailure = { exception ->
                Result.failure(exception)
            }
        )
    }

    // Маппер Domain → API для параметра фильтра
    private fun ServiceStatus.toApi(): ApiServiceStatus {
        return when (this) {
            ServiceStatus.ACTIVE -> ApiServiceStatus.ACTIVE
            ServiceStatus.INACTIVE -> ApiServiceStatus.INACTIVE
        }
    }
}
