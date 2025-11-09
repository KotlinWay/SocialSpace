package info.javaway.sc.shared.presentation.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import info.javaway.sc.shared.domain.models.RegisterRequest
import info.javaway.sc.shared.domain.models.Result
import info.javaway.sc.shared.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана регистрации
 */
class RegisterViewModel(
    private val authRepository: AuthRepository
) {
    var state by mutableStateOf(RegisterState())
        private set

    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    fun onPhoneChange(phone: String) {
        state = state.copy(phone = phone, error = null)
    }

    fun onNameChange(name: String) {
        state = state.copy(name = name, error = null)
    }

    fun onEmailChange(email: String) {
        state = state.copy(email = email, error = null)
    }

    fun onPasswordChange(password: String) {
        state = state.copy(password = password, error = null)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        state = state.copy(confirmPassword = confirmPassword, error = null)
    }

    fun register() {
        if (!validateInput()) {
            return
        }

        state = state.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val request = RegisterRequest(
                phone = state.phone.trim(),
                name = state.name.trim(),
                email = state.email.trim().ifBlank { null },
                password = state.password
            )

            when (val result = authRepository.register(request)) {
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
            !isValidPhone(state.phone) -> {
                state = state.copy(error = "Некорректный формат телефона (например: +79991234567)")
                return false
            }
            state.name.isBlank() -> {
                state = state.copy(error = "Введите ваше имя")
                return false
            }
            state.name.length < 2 -> {
                state = state.copy(error = "Имя должно содержать минимум 2 символа")
                return false
            }
            state.email.isNotBlank() && !isValidEmail(state.email) -> {
                state = state.copy(error = "Некорректный формат email")
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
            state.confirmPassword.isBlank() -> {
                state = state.copy(error = "Подтвердите пароль")
                return false
            }
            state.password != state.confirmPassword -> {
                state = state.copy(error = "Пароли не совпадают")
                return false
            }
        }
        return true
    }

    private fun isValidPhone(phone: String): Boolean {
        // Проверка формата телефона: +7XXXXXXXXXX (11 цифр после +7)
        val phoneRegex = """^\+7\d{10}$""".toRegex()
        return phoneRegex.matches(phone.trim())
    }

    private fun isValidEmail(email: String): Boolean {
        // Простая проверка email
        val emailRegex = """^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""".toRegex()
        return emailRegex.matches(email.trim())
    }
}

data class RegisterState(
    val phone: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
