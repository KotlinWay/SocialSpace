package info.javaway.sc.shared.presentation.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import info.javaway.sc.api.models.LoginRequest
import info.javaway.sc.shared.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана входа
 */
class LoginViewModel(
    private val authRepository: AuthRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Единое состояние экрана
    var state by mutableStateOf<LoginState>(LoginState.Idle)
        private set

    // Данные формы (отдельно от состояния загрузки)
    var phone by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    fun onPhoneChange(phone: String) {
        this.phone = phone
        // Сбрасываем ошибку при изменении данных
        if (state is LoginState.Error) {
            state = LoginState.Idle
        }
    }

    fun onPasswordChange(password: String) {
        this.password = password
        // Сбрасываем ошибку при изменении данных
        if (state is LoginState.Error) {
            state = LoginState.Idle
        }
    }

    fun login() {
        // Валидация
        val validationError = validateInput()
        if (validationError != null) {
            state = LoginState.Error(validationError)
            return
        }

        state = LoginState.Loading

        viewModelScope.launch {
            val request = LoginRequest(
                phone = phone.trim(),
                password = password
            )

            authRepository.login(request)
                .onSuccess {
                    state = LoginState.Success
                }
                .onFailure { error ->
                    state = LoginState.Error(error.message ?: "Неизвестная ошибка")
                }
        }
    }

    private fun validateInput(): String? {
        return when {
            phone.isBlank() -> "Введите номер телефона"
            password.isBlank() -> "Введите пароль"
            password.length < 6 -> "Пароль должен содержать минимум 6 символов"
            else -> null
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
 * Состояния экрана входа
 */
sealed interface LoginState {
    /**
     * Начальное состояние / форма готова к вводу
     */
    data object Idle : LoginState

    /**
     * Процесс входа
     */
    data object Loading : LoginState

    /**
     * Ошибка входа
     */
    data class Error(val message: String) : LoginState

    /**
     * Успешный вход
     */
    data object Success : LoginState
}
