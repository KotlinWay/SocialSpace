package info.javaway.sc.shared.presentation.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import info.javaway.sc.shared.domain.models.LoginRequest
import info.javaway.sc.shared.domain.models.Result
import info.javaway.sc.shared.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана входа
 */
class LoginViewModel(
    private val authRepository: AuthRepository
) {
    var state by mutableStateOf(LoginState())
        private set

    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    fun onPhoneChange(phone: String) {
        state = state.copy(phone = phone, error = null)
    }

    fun onPasswordChange(password: String) {
        state = state.copy(password = password, error = null)
    }

    fun login() {
        if (!validateInput()) {
            return
        }

        state = state.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val request = LoginRequest(
                phone = state.phone.trim(),
                password = state.password
            )

            when (val result = authRepository.login(request)) {
                is Result.Success -> {
                    state = state.copy(isLoading = false, isSuccess = true)
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

    private fun validateInput(): Boolean {
        when {
            state.phone.isBlank() -> {
                state = state.copy(error = "Введите номер телефона")
                return false
            }
            state.password.isBlank() -> {
                state = state.copy(error = "Введите пароль")
                return false
            }
            state.password.length < 6 -> {
                state = state.copy(error = "Пароль должен содержать минимум 6 символов")
                return false
            }
        }
        return true
    }
}

data class LoginState(
    val phone: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
