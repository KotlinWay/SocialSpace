package info.javaway.sc.shared.presentation.screens.home

import info.javaway.sc.shared.data.local.TokenManager
import info.javaway.sc.shared.domain.models.User
import info.javaway.sc.shared.domain.repository.AuthRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для главного экрана
 */
class HomeViewModel(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) {
    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut = _isLoggedOut.asStateFlow()

    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            println("🔍 HomeViewModel: Starting loadCurrentUser")
            println("🔍 Token: ${tokenManager.getToken()}")

            authRepository.getCurrentUser()
                .onSuccess { user ->
                    println("✅ User loaded successfully: $user")
                    _isLoading.value = false
                    _user.value = user
                }
                .onFailure { error ->
                    println("❌ Error loading user: ${error.message}")
                    Napier.e { error.message ?: "Unknown error" }
                    _isLoading.value = false
                    _error.value = error.message ?: "Неизвестная ошибка"
                }
        }
    }

    fun logout() {
        tokenManager.clear()
        _isLoggedOut.value = true
    }

    fun retry() {
        loadCurrentUser()
    }
}
