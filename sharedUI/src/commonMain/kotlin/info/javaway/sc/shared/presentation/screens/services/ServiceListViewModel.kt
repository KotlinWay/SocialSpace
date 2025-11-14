package info.javaway.sc.shared.presentation.screens.services

import androidx.paging.PagingData
import androidx.paging.cachedIn
import info.javaway.sc.shared.data.paging.ServiceFilters
import info.javaway.sc.shared.domain.models.Service
import info.javaway.sc.shared.domain.models.ServiceStatus
import info.javaway.sc.shared.domain.repository.ServiceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest

/**
 * ViewModel для списка услуг с Paging 3
 */
class ServiceListViewModel(
    private val serviceRepository: ServiceRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Фильтры для услуг
    private val _filters = MutableStateFlow(ServiceFiltersState())
    val filters: StateFlow<ServiceFiltersState> = _filters.asStateFlow()

    /**
     * Flow с пагинированными услугами
     * При изменении фильтров автоматически создается новый PagingSource
     */
    val servicesFlow: Flow<PagingData<Service>> = _filters
        .flatMapLatest { filters ->
            serviceRepository.getServicesPaged(
                ServiceFilters(
                    categoryId = filters.categoryId,
                    status = filters.status,
                    search = filters.search
                )
            )
        }
        .cachedIn(viewModelScope) // Кэширование в scope

    /**
     * Обновить фильтры
     * Автоматически триггерит новую загрузку через servicesFlow
     */
    fun updateFilters(
        categoryId: Long? = null,
        status: ServiceStatus? = null,
        search: String? = null
    ) {
        _filters.value = ServiceFiltersState(
            categoryId = categoryId,
            status = status,
            search = search
        )
    }

    /**
     * Сбросить все фильтры
     */
    fun clearFilters() {
        _filters.value = ServiceFiltersState()
    }

    /**
     * Очистка ресурсов
     */
    fun onCleared() {
        viewModelScope.cancel()
    }
}

/**
 * Состояние фильтров для услуг
 */
data class ServiceFiltersState(
    val categoryId: Long? = null,
    val status: ServiceStatus? = null,
    val search: String? = null
)
