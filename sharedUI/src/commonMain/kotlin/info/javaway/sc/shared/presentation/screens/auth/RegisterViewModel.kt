package info.javaway.sc.shared.presentation.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import info.javaway.sc.shared.domain.models.RegisterRequest
import info.javaway.sc.shared.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана регистрации
 */
class RegisterViewModel(
    private val authRepository: AuthRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Единое состояние экрана
    var state by mutableStateOf<RegisterState>(RegisterState.Idle)
        private set

    // Данные формы
    var phone by mutableStateOf("")
        private set

    var name by mutableStateOf("")
        private set

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var confirmPassword by mutableStateOf("")
        private set

    fun onPhoneChange(phone: String) {
        this.phone = phone
        if (state is RegisterState.Error) {
            state = RegisterState.Idle
        }
    }

    fun onNameChange(name: String) {
        this.name = name
        if (state is RegisterState.Error) {
            state = RegisterState.Idle
        }
    }

    fun onEmailChange(email: String) {
        this.email = email
        if (state is RegisterState.Error) {
            state = RegisterState.Idle
        }
    }

    fun onPasswordChange(password: String) {
        this.password = password
        if (state is RegisterState.Error) {
            state = RegisterState.Idle
        }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        this.confirmPassword = confirmPassword
        if (state is RegisterState.Error) {
            state = RegisterState.Idle
        }
    }

    fun register() {
        // Валидация
        val validationError = validateInput()
        if (validationError != null) {
            state = RegisterState.Error(validationError)
            return
        }

        state = RegisterState.Loading

        viewModelScope.launch {
            val request = RegisterRequest(
                phone = phone.trim(),
                name = name.trim(),
                email = email.trim().ifBlank { null },
                password = password
            )

            authRepository.register(request)
                .onSuccess {
                    state = RegisterState.Success
                }
                .onFailure { error ->
                    state = RegisterState.Error(error.message ?: "Неизвестная ошибка")
                }
        }
    }

    private fun validateInput(): String? {
        return when {
            phone.isBlank() -> "Введите номер телефона"
            !isValidPhone(phone) -> "Некорректный формат телефона (например: +79991234567)"
            name.isBlank() -> "Введите ваше имя"
            name.length < 2 -> "Имя должно содержать минимум 2 символа"
            email.isNotBlank() && !isValidEmail(email) -> "Некорректный формат email"
            password.isBlank() -> "Введите пароль"
            password.length < 6 -> "Пароль должен содержать минимум 6 символов"
            confirmPassword.isBlank() -> "Подтвердите пароль"
            password != confirmPassword -> "Пароли не совпадают"
            else -> null
        }
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

    /**
     * Очистка ресурсов
     */
    fun onCleared() {
        viewModelScope.cancel()
    }
}

/**
 * Состояния экрана регистрации
 */
sealed interface RegisterState {
    /**
     * Начальное состояние / форма готова к вводу
     */
    data object Idle : RegisterState

    /**
     * Процесс регистрации
     */
    data object Loading : RegisterState

    /**
     * Ошибка регистрации
     */
    data class Error(val message: String) : RegisterState

    /**
     * Успешная регистрация
     */
    data object Success : RegisterState
}
