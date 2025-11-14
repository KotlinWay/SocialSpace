package info.javaway.sc.shared.presentation.screens.services

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import info.javaway.sc.shared.domain.models.Service
import info.javaway.sc.shared.domain.repository.ServiceRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана "Мои услуги" (MVI подход)
 */
class MyServicesViewModel(
    private val serviceRepository: ServiceRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Единое состояние экрана
    var state by mutableStateOf<MyServicesState>(MyServicesState.Loading)
        private set

    init {
        loadMyServices()
    }

    /**
     * Загрузка списка своих услуг
     */
    fun loadMyServices() {
        state = MyServicesState.Loading

        viewModelScope.launch {
            Napier.d("Loading my services", tag = "MyServicesViewModel")

            serviceRepository.getMyServices()
                .onSuccess { services ->
                    Napier.d("My services loaded successfully: ${services.size} items", tag = "MyServicesViewModel")

                    state = if (services.isEmpty()) {
                        MyServicesState.Empty
                    } else {
                        MyServicesState.Success(services)
                    }
                }
                .onFailure { error ->
                    Napier.e("Failed to load my services: ${error.message}", tag = "MyServicesViewModel")
                    state = MyServicesState.Error(
                        message = error.message ?: "Не удалось загрузить услуги"
                    )
                }
        }
    }

    /**
     * Удаление услуги
     */
    fun deleteService(serviceId: Long) {
        // Сохраняем текущий список для возможного отката
        val currentState = state
        if (currentState !is MyServicesState.Success) return

        // Оптимистичное обновление UI - удаляем услугу из списка сразу
        val updatedServices = currentState.services.filter { it.id != serviceId }
        state = if (updatedServices.isEmpty()) {
            MyServicesState.Empty
        } else {
            MyServicesState.Success(updatedServices)
        }

        viewModelScope.launch {
            Napier.d("Deleting service with id: $serviceId", tag = "MyServicesViewModel")

            serviceRepository.deleteService(serviceId)
                .onSuccess {
                    Napier.d("Service deleted successfully", tag = "MyServicesViewModel")
                    // UI уже обновлен оптимистично, ничего не делаем
                }
                .onFailure { error ->
                    Napier.e("Failed to delete service: ${error.message}", tag = "MyServicesViewModel")

                    // Откатываем изменения при ошибке
                    state = currentState

                    // TODO: Показать snackbar с ошибкой
                    // Пока просто логируем
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
