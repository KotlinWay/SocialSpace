package info.javaway.sc.shared.presentation.screens.services.detail

import com.arkivanov.decompose.ComponentContext
import info.javaway.sc.shared.domain.models.Service
import info.javaway.sc.shared.domain.repository.AuthRepository
import info.javaway.sc.shared.domain.repository.ServiceRepository
import info.javaway.sc.shared.presentation.core.BaseComponent
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface ServiceDetailComponent {
    val state: StateFlow<ServiceDetailState>

    fun loadService()
}

class DefaultServiceDetailComponent(
    componentContext: ComponentContext,
    private val serviceRepository: ServiceRepository,
    private val authRepository: AuthRepository,
    private val serviceId: Long
) : BaseComponent(componentContext), ServiceDetailComponent {

    private val _state = MutableStateFlow<ServiceDetailState>(ServiceDetailState.Loading)
    override val state: StateFlow<ServiceDetailState> = _state.asStateFlow()

    init {
        loadService()
    }

    override fun loadService() {
        _state.value = ServiceDetailState.Loading

        componentScope.launch {
            Napier.d("Loading service with id: $serviceId", tag = "ServiceDetailComponent")

            serviceRepository.getService(serviceId)
                .onSuccess { service ->
                    val currentUserId = authRepository.getUserId()
                    val isOwner = currentUserId != null && currentUserId == service.user.id

                    _state.value = ServiceDetailState.Success(
                        service = service,
                        isOwner = isOwner
                    )
                }
                .onFailure { error ->
                    Napier.e("Failed to load service: ${error.message}", error, tag = "ServiceDetailComponent")
                    _state.value = ServiceDetailState.Error(
                        message = error.message ?: "Не удалось загрузить услугу"
                    )
                }
        }
    }
}

/**
 * Состояния экрана деталей услуги
 */
sealed interface ServiceDetailState {
    /**
     * Загрузка услуги
     */
    data object Loading : ServiceDetailState

    /**
     * Услуга успешно загружена
     */
    data class Success(
        val service: Service,
        val isOwner: Boolean
    ) : ServiceDetailState

    /**
     * Ошибка загрузки услуги
     */
    data class Error(val message: String) : ServiceDetailState
}
