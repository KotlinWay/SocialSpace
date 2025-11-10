package info.javaway.sc.shared.presentation.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import info.javaway.sc.shared.data.local.TokenManager
import info.javaway.sc.api.models.User
import info.javaway.sc.shared.domain.repository.AuthRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * ViewModel для главного экрана
 */
class HomeViewModel(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Единое состояние экрана
    var state by mutableStateOf<HomeState>(HomeState.Loading)
        private set

    // Флаг выхода из системы (отдельно от основного состояния)
    var isLoggedOut by mutableStateOf(false)
        private set

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        state = HomeState.Loading

        viewModelScope.launch {
            Napier.d { "HomeViewModel: Starting loadCurrentUser" }
            Napier.d { "Token: ${tokenManager.getToken()}" }

            authRepository.getCurrentUser()
                .onSuccess { user ->
                    Napier.d { "User loaded successfully: $user" }
                    state = HomeState.Success(user)
                }
                .onFailure { error ->
                    Napier.e(error) { "Error loading user: ${error.message}" }
                    state = HomeState.Error(error.message ?: "Неизвестная ошибка")
                }
        }
    }

    fun logout() {
        tokenManager.clear()
        isLoggedOut = true
    }

    fun retry() {
        loadCurrentUser()
    }

    /**
     * Очистка ресурсов
     */
    fun onCleared() {
        viewModelScope.cancel()
    }
}

/**
 * Состояния главного экрана
 */
sealed interface HomeState {
    /**
     * Загрузка профиля пользователя
     */
    data object Loading : HomeState

    /**
     * Профиль успешно загружен
     */
    data class Success(val user: User) : HomeState

    /**
     * Ошибка загрузки профиля
     */
    data class Error(val message: String) : HomeState
}
