package info.javaway.sc.shared.presentation.screens.auth

import com.arkivanov.decompose.ComponentContext
import info.javaway.sc.shared.domain.repository.AuthRepository
import info.javaway.sc.shared.presentation.core.BaseComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Компонент для экрана входа.
 */
interface LoginComponent {
    val state: StateFlow<LoginState>
    val phone: StateFlow<String>
    val password: StateFlow<String>

    fun onPhoneChange(phone: String)
    fun onPasswordChange(password: String)
    fun login()
}

class DefaultLoginComponent(
    componentContext: ComponentContext,
    private val authRepository: AuthRepository
) : BaseComponent(componentContext), LoginComponent {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    override val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _phone = MutableStateFlow("")
    override val phone: StateFlow<String> = _phone.asStateFlow()

    private val _password = MutableStateFlow("")
    override val password: StateFlow<String> = _password.asStateFlow()

    override fun onPhoneChange(phone: String) {
        _phone.value = phone
        if (_state.value is LoginState.Error) {
            _state.value = LoginState.Idle
        }
    }

    override fun onPasswordChange(password: String) {
        _password.value = password
        if (_state.value is LoginState.Error) {
            _state.value = LoginState.Idle
        }
    }

    override fun login() {
        val validationError = validateInput()
        if (validationError != null) {
            _state.value = LoginState.Error(validationError)
            return
        }

        _state.value = LoginState.Loading

        componentScope.launch {
            authRepository.login(
                phone = _phone.value.trim(),
                password = _password.value
            )
                .onSuccess { _state.value = LoginState.Success }
                .onFailure { error ->
                    _state.value = LoginState.Error(error.message ?: "Неизвестная ошибка")
                }
        }
    }

    private fun validateInput(): String? =
        when {
            _phone.value.isBlank() -> "Введите номер телефона"
            _password.value.isBlank() -> "Введите пароль"
            _password.value.length < 6 -> "Пароль должен содержать минимум 6 символов"
            else -> null
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
