package info.javaway.sc.shared.presentation.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import info.javaway.sc.api.models.User
import info.javaway.sc.shared.data.local.TokenManager
import info.javaway.sc.shared.domain.repository.AuthRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана профиля (MVI подход)
 */
class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Единое состояние экрана
    var state by mutableStateOf<ProfileState>(ProfileState.Loading)
        private set

    init {
        loadProfile()
    }

    /**
     * Загрузка профиля текущего пользователя
     */
    fun loadProfile() {
        state = ProfileState.Loading

        viewModelScope.launch {
            Napier.d("Loading current user profile", tag = "ProfileViewModel")

            authRepository.getCurrentUser()
                .onSuccess { user ->
                    Napier.d("Profile loaded successfully: ${user.name}", tag = "ProfileViewModel")
                    state = ProfileState.Success(user)
                }
                .onFailure { error ->
                    Napier.e("Failed to load profile: ${error.message}", tag = "ProfileViewModel")
                    state = ProfileState.Error(error.message ?: "Не удалось загрузить профиль")
                }
        }
    }

    /**
     * Выход из системы
     */
    fun logout() {
        Napier.d("Logging out user", tag = "ProfileViewModel")
        tokenManager.clearToken()
    }

    /**
     * Очистка ресурсов ViewModel
     */
    fun onCleared() {
        viewModelScope.cancel()
    }
}

/**
 * Состояния экрана профиля
 */
sealed interface ProfileState {
    /**
     * Загрузка профиля
     */
    data object Loading : ProfileState

    /**
     * Профиль успешно загружен
     */
    data class Success(val user: User) : ProfileState

    /**
     * Ошибка при загрузке профиля
     */
    data class Error(val message: String) : ProfileState
}
