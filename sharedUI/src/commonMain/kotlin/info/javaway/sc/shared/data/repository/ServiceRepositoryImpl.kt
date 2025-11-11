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
        return apiClient.getServices(
            categoryId = categoryId,
            status = status?.toApi(),
            search = search,
            page = page,
            pageSize = pageSize
        ).fold(
            onSuccess = { response ->
                kotlin.Result.success(response.services.map { it.toDomain() })
            },
            onFailure = { exception ->
                kotlin.Result.failure(exception)
            }
        )
    }

    override suspend fun getService(serviceId: Long): kotlin.Result<Service> {
        return apiClient.getService(serviceId).fold(
            onSuccess = { service ->
                kotlin.Result.success(service.toDomain())
            },
            onFailure = { exception ->
                kotlin.Result.failure(exception)
            }
        )
    }

    override suspend fun getMyServices(): kotlin.Result<List<Service>> {
        return apiClient.getMyServices().fold(
            onSuccess = { services ->
                kotlin.Result.success(services.map { it.toDomain() })
            },
            onFailure = { exception ->
                kotlin.Result.failure(exception)
            }
        )
    }

    // Маппер Domain → API для параметров фильтра
    private fun ServiceStatus.toApi(): ApiServiceStatus {
        return when (this) {
            ServiceStatus.ACTIVE -> ApiServiceStatus.ACTIVE
            ServiceStatus.INACTIVE -> ApiServiceStatus.INACTIVE
        }
    }
}
