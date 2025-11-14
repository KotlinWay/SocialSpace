package info.javaway.sc.shared.domain.repository

import androidx.paging.PagingData
import info.javaway.sc.shared.data.paging.ServiceFilters
import info.javaway.sc.shared.domain.models.Service
import info.javaway.sc.shared.domain.models.ServiceStatus
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий для работы с услугами
 * Возвращает Domain модели
 */
interface ServiceRepository {
    /**
     * Получить список услуг с пагинацией через Paging 3
     * @param filters Фильтры для поиска услуг
     * @return Flow с PagingData<Service>
     */
    fun getServicesPaged(filters: ServiceFilters = ServiceFilters()): Flow<PagingData<Service>>

    /**
     * Получить список услуг (устаревший метод, используйте getServicesPaged)
     * @deprecated Используйте getServicesPaged() для автоматической пагинации
     */
    @Deprecated("Используйте getServicesPaged() для автоматической пагинации через Paging 3")
    suspend fun getServices(
        categoryId: Long? = null,
        status: ServiceStatus? = null,
        search: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): kotlin.Result<List<Service>>

    suspend fun getService(serviceId: Long): kotlin.Result<Service>
    suspend fun getMyServices(): kotlin.Result<List<Service>>
    suspend fun deleteService(serviceId: Long): kotlin.Result<Unit>
}
