package info.javaway.sc.shared.domain.repository

import info.javaway.sc.shared.domain.models.Service
import info.javaway.sc.shared.domain.models.ServiceStatus

/**
 * Репозиторий для работы с услугами
 * Возвращает Domain модели
 */
interface ServiceRepository {
    suspend fun getServices(
        categoryId: Long? = null,
        status: ServiceStatus? = null,
        search: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): kotlin.Result<List<Service>>

    suspend fun getService(serviceId: Long): kotlin.Result<Service>
    suspend fun getMyServices(): kotlin.Result<List<Service>>
}
