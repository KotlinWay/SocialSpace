package info.javaway.sc.shared.presentation.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import info.javaway.sc.shared.data.local.TokenManager
import info.javaway.sc.shared.domain.models.Result
import info.javaway.sc.shared.domain.models.User
import info.javaway.sc.shared.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel для главного экрана
 */
class HomeViewModel(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) {
    var state by mutableStateOf(HomeState())
        private set

    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        state = state.copy(isLoading = true, error = null)

        viewModelScope.launch {
            when (val result = authRepository.getCurrentUser()) {
                is Result.Success -> {
                    state = state.copy(
                        isLoading = false,
                        user = result.data
                    )
                }
                is Result.Error -> {
                    state = state.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Result.Loading -> {
                    // Already in loading state
                }
            }
        }
    }

    fun logout() {
        tokenManager.clearToken()
        state = state.copy(isLoggedOut = true)
    }

    fun retry() {
        loadCurrentUser()
    }
}

data class HomeState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isLoggedOut: Boolean = false,
    val error: String? = null
)
