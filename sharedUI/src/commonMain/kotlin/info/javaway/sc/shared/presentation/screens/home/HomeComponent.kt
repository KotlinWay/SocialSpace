package info.javaway.sc.shared.presentation.screens.home

import com.arkivanov.decompose.ComponentContext
import info.javaway.sc.shared.data.local.TokenManager
import info.javaway.sc.shared.domain.models.User
import info.javaway.sc.shared.domain.repository.AuthRepository
import info.javaway.sc.shared.presentation.core.BaseComponent
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Компонент для главного экрана.
 */
interface HomeComponent {
    val state: StateFlow<HomeState>
    val isLoggedOut: StateFlow<Boolean>

    fun logout()
    fun retry()
}

class DefaultHomeComponent(
    componentContext: ComponentContext,
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : BaseComponent(componentContext), HomeComponent {

    private val _state = MutableStateFlow<HomeState>(HomeState.Loading)
    override val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _isLoggedOut = MutableStateFlow(false)
    override val isLoggedOut: StateFlow<Boolean> = _isLoggedOut.asStateFlow()

    init {
        loadCurrentUser()
    }

    override fun logout() {
        tokenManager.clear()
        _isLoggedOut.value = true
    }

    override fun retry() {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        _state.value = HomeState.Loading

        componentScope.launch {
            Napier.d { "HomeComponent: Starting loadCurrentUser" }
            Napier.d { "Token: ${tokenManager.getToken()}" }

            authRepository.getCurrentUser()
                .onSuccess { user ->
                    Napier.d { "User loaded successfully: $user" }
                    _state.value = HomeState.Success(user)
                }
                .onFailure { error ->
                    Napier.e(error) { "Error loading user: ${error.message}" }
                    _state.value = HomeState.Error(error.message ?: "Неизвестная ошибка")
                }
        }
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
