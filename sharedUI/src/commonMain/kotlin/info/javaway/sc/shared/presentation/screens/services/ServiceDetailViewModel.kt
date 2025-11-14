package info.javaway.sc.shared.presentation.screens.services

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import info.javaway.sc.shared.data.local.TokenManager
import info.javaway.sc.shared.domain.models.Service
import info.javaway.sc.shared.domain.repository.ServiceRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * ViewModel для детального экрана услуги (MVI подход)
 */
class ServiceDetailViewModel(
    private val serviceRepository: ServiceRepository,
    private val tokenManager: TokenManager,
    private val serviceId: Long
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Единое состояние экрана
    var state by mutableStateOf<ServiceDetailState>(ServiceDetailState.Loading)
        private set

    init {
        loadService()
    }

    /**
     * Загрузка услуги с сервера
     */
    fun loadService() {
        state = ServiceDetailState.Loading

        viewModelScope.launch {
            Napier.d("Loading service with id: $serviceId", tag = "ServiceDetailViewModel")

            serviceRepository.getService(serviceId)
                .onSuccess { service ->
                    Napier.d("Service loaded successfully: ${service.title}", tag = "ServiceDetailViewModel")

                    // Проверяем, является ли текущий пользователь владельцем услуги
                    val currentUserId = tokenManager.getToken()?.let {
                        // TODO: извлечь userId из JWT токена (можно парсить или добавить в TokenManager)
                        // Временно считаем что не владелец если не можем определить
                        null
                    }

                    val isOwner = currentUserId != null && currentUserId == service.user.id

                    state = ServiceDetailState.Success(
                        service = service,
                        isOwner = isOwner
                    )
                }
                .onFailure { error ->
                    Napier.e("Failed to load service: ${error.message}", error, tag = "ServiceDetailViewModel")
                    state = ServiceDetailState.Error(
                        message = error.message ?: "Не удалось загрузить услугу"
                    )
                }
        }
    }

    /**
     * Очистка ресурсов
     */
    fun onCleared() {
        viewModelScope.cancel()
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
