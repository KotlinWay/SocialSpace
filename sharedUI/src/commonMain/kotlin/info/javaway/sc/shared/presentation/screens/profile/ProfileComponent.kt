package info.javaway.sc.shared.presentation.screens.profile

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

interface ProfileComponent {
    val state: StateFlow<ProfileState>

    fun loadProfile()
    fun logout()
}

class DefaultProfileComponent(
    componentContext: ComponentContext,
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : BaseComponent(componentContext), ProfileComponent {

    private val _state = MutableStateFlow<ProfileState>(ProfileState.Loading)
    override val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    override fun loadProfile() {
        _state.value = ProfileState.Loading

        componentScope.launch {
            Napier.d("Loading current user profile", tag = "ProfileComponent")

            authRepository.getCurrentUser()
                .onSuccess { user ->
                    Napier.d("Profile loaded successfully: ${user.name}", tag = "ProfileComponent")
                    _state.value = ProfileState.Success(user)
                }
                .onFailure { error ->
                    Napier.e("Failed to load profile: ${error.message}", tag = "ProfileComponent")
                    _state.value = ProfileState.Error(error.message ?: "Не удалось загрузить профиль")
                }
        }
    }

    override fun logout() {
        Napier.d("Logging out user", tag = "ProfileComponent")
        tokenManager.clear()
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
