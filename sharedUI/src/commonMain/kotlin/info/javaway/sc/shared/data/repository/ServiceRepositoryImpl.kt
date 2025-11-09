package info.javaway.sc.shared.data.repository

import info.javaway.sc.shared.data.api.ApiClient
import info.javaway.sc.shared.domain.models.*
import info.javaway.sc.shared.domain.repository.ServiceRepository

/**
 * Реализация репозитория услуг
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
    ): Result<ServiceListResponse> {
        return apiClient.getServices(
            categoryId = categoryId,
            status = status,
            search = search,
            page = page,
            pageSize = pageSize
        )
    }

    override suspend fun getService(serviceId: Long): Result<ServiceResponse> {
        return apiClient.getService(serviceId)
    }

    override suspend fun getMyServices(): Result<List<ServiceResponse>> {
        return apiClient.getMyServices()
    }
}
