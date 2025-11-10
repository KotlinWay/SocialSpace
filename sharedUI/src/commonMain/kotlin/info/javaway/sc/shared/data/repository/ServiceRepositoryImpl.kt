package info.javaway.sc.shared.data.repository

import info.javaway.sc.shared.data.api.ApiClient
import info.javaway.sc.shared.data.mappers.toDomain
import info.javaway.sc.shared.domain.models.Service
import info.javaway.sc.shared.domain.models.ServiceStatus
import info.javaway.sc.shared.domain.repository.ServiceRepository
import info.javaway.sc.api.models.ServiceStatus as ApiServiceStatus

/**
 * Реализация репозитория услуг
 * Преобразует DTO в Domain модели
 */
class ServiceRepositoryImpl(
    private val apiClient: ApiClient
) : ServiceRepository {

    override suspend fun getServices(
        categoryId: Long?,
        status: ServiceStatus?,
        search: String?,
        page: Int,
        pageSize: Int
    ): kotlin.Result<List<Service>> {
        val result = apiClient.getServices(
            categoryId = categoryId,
            status = status?.toApi(),
            search = search,
            page = page,
            pageSize = pageSize
        )

        return when (result) {
            is Result.Success -> Result.Success(result.data.services.map { it.toDomain() })
            is Result.Error -> result
        }
    }

    override suspend fun getService(serviceId: Long): kotlin.Result<Service> {
        val result = apiClient.getService(serviceId)
        return when (result) {
            is Result.Success -> Result.Success(result.data.toDomain())
            is Result.Error -> result
        }
    }

    override suspend fun getMyServices(): kotlin.Result<List<Service>> {
        val result = apiClient.getMyServices()
        return when (result) {
            is Result.Success -> Result.Success(result.data.map { it.toDomain() })
            is Result.Error -> result
        }
    }

    // Маппер Domain → API для параметров фильтра
    private fun ServiceStatus.toApi(): ApiServiceStatus {
        return when (this) {
            ServiceStatus.ACTIVE -> ApiServiceStatus.ACTIVE
            ServiceStatus.INACTIVE -> ApiServiceStatus.INACTIVE
        }
    }
}
