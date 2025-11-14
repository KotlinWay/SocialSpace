package info.javaway.sc.shared.presentation.screens.services

import com.arkivanov.decompose.ComponentContext
import info.javaway.sc.shared.domain.models.Service
import info.javaway.sc.shared.domain.repository.ServiceRepository
import info.javaway.sc.shared.presentation.core.BaseComponent
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface MyServicesComponent {
    val state: StateFlow<MyServicesState>

    fun loadMyServices()
    fun deleteService(serviceId: Long)
}

class DefaultMyServicesComponent(
    componentContext: ComponentContext,
    private val serviceRepository: ServiceRepository
) : BaseComponent(componentContext), MyServicesComponent {

    private val _state = MutableStateFlow<MyServicesState>(MyServicesState.Loading)
    override val state: StateFlow<MyServicesState> = _state.asStateFlow()

    init {
        loadMyServices()
    }

    override fun loadMyServices() {
        _state.value = MyServicesState.Loading

        componentScope.launch {
            Napier.d("Loading my services", tag = "MyServicesComponent")

            serviceRepository.getMyServices()
                .onSuccess { services ->
                    Napier.d("My services loaded: ${services.size}", tag = "MyServicesComponent")
                    _state.value = if (services.isEmpty()) {
                        MyServicesState.Empty
                    } else {
                        MyServicesState.Success(services)
                    }
                }
                .onFailure { error ->
                    Napier.e("Failed to load my services: ${error.message}", tag = "MyServicesComponent")
                    _state.value = MyServicesState.Error(
                        message = error.message ?: "Не удалось загрузить услуги"
                    )
                }
        }
    }

    override fun deleteService(serviceId: Long) {
        val currentState = _state.value
        if (currentState !is MyServicesState.Success) return

        val updatedServices = currentState.services.filter { it.id != serviceId }
        _state.value = if (updatedServices.isEmpty()) {
            MyServicesState.Empty
        } else {
            MyServicesState.Success(updatedServices)
        }

        componentScope.launch {
            Napier.d("Deleting service $serviceId", tag = "MyServicesComponent")

            serviceRepository.deleteService(serviceId)
                .onFailure { error ->
                    Napier.e("Failed to delete service: ${error.message}", tag = "MyServicesComponent")
                    _state.value = currentState
                }
        }
    }
}

/**
 * Состояния экрана "Мои услуги"
 */
sealed interface MyServicesState {
    /**
     * Загрузка данных
     */
    data object Loading : MyServicesState

    /**
     * Услуги загружены успешно
     */
    data class Success(val services: List<Service>) : MyServicesState

    /**
     * Список услуг пуст
     */
    data object Empty : MyServicesState

    /**
     * Ошибка загрузки
     */
    data class Error(val message: String) : MyServicesState
}
