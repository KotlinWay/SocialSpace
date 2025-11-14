package info.javaway.sc.shared.presentation.screens.services.list

import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.arkivanov.decompose.ComponentContext
import info.javaway.sc.shared.data.paging.ServiceFilters
import info.javaway.sc.shared.domain.models.Service
import info.javaway.sc.shared.domain.models.ServiceStatus
import info.javaway.sc.shared.domain.repository.ServiceRepository
import info.javaway.sc.shared.presentation.core.BaseComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest

interface ServiceListComponent {
    val filters: StateFlow<ServiceFiltersState>
    val servicesFlow: Flow<PagingData<Service>>

    fun updateFilters(
        categoryId: Long? = null,
        status: ServiceStatus? = null,
        search: String? = null
    )

    fun clearFilters()
}

class DefaultServiceListComponent(
    componentContext: ComponentContext,
    private val serviceRepository: ServiceRepository
) : BaseComponent(componentContext), ServiceListComponent {

    private val _filters = MutableStateFlow(ServiceFiltersState())
    override val filters: StateFlow<ServiceFiltersState> = _filters.asStateFlow()

    override val servicesFlow: Flow<PagingData<Service>> = _filters
        .flatMapLatest { filters ->
            serviceRepository.getServicesPaged(
                ServiceFilters(
                    categoryId = filters.categoryId,
                    status = filters.status,
                    search = filters.search
                )
            )
        }
        .cachedIn(componentScope)

    override fun updateFilters(
        categoryId: Long?,
        status: ServiceStatus?,
        search: String?
    ) {
        _filters.value = ServiceFiltersState(
            categoryId = categoryId,
            status = status,
            search = search
        )
    }

    override fun clearFilters() {
        _filters.value = ServiceFiltersState()
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
