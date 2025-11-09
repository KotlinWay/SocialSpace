package info.javaway.sc.shared.domain.repository

import info.javaway.sc.shared.domain.models.*

/**
 * Репозиторий для работы с услугами
 */
interface ServiceRepository {
    suspend fun getServices(
        categoryId: Long? = null,
        status: ServiceStatus? = null,
        search: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): Result<ServiceListResponse>

    suspend fun getService(serviceId: Long): Result<ServiceResponse>
    suspend fun getMyServices(): Result<List<ServiceResponse>>
}
