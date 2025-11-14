package info.javaway.sc.shared.presentation.screens.auth

import com.arkivanov.decompose.ComponentContext
import info.javaway.sc.shared.domain.repository.AuthRepository
import info.javaway.sc.shared.presentation.core.BaseComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Компонент для экрана регистрации.
 */
interface RegisterComponent {
    val state: StateFlow<RegisterState>
    val phone: StateFlow<String>
    val name: StateFlow<String>
    val email: StateFlow<String>
    val password: StateFlow<String>
    val confirmPassword: StateFlow<String>

    fun onPhoneChange(phone: String)
    fun onNameChange(name: String)
    fun onEmailChange(email: String)
    fun onPasswordChange(password: String)
    fun onConfirmPasswordChange(confirmPassword: String)
    fun register()
}

class DefaultRegisterComponent(
    componentContext: ComponentContext,
    private val authRepository: AuthRepository
) : BaseComponent(componentContext), RegisterComponent {

    private val _state = MutableStateFlow<RegisterState>(RegisterState.Idle)
    override val state: StateFlow<RegisterState> = _state.asStateFlow()

    private val _phone = MutableStateFlow("")
    override val phone: StateFlow<String> = _phone.asStateFlow()

    private val _name = MutableStateFlow("")
    override val name: StateFlow<String> = _name.asStateFlow()

    private val _email = MutableStateFlow("")
    override val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    override val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    override val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    override fun onPhoneChange(phone: String) {
        _phone.value = phone
        resetError()
    }

    override fun onNameChange(name: String) {
        _name.value = name
        resetError()
    }

    override fun onEmailChange(email: String) {
        _email.value = email
        resetError()
    }

    override fun onPasswordChange(password: String) {
        _password.value = password
        resetError()
    }

    override fun onConfirmPasswordChange(confirmPassword: String) {
        _confirmPassword.value = confirmPassword
        resetError()
    }

    override fun register() {
        val validationError = validateInput()
        if (validationError != null) {
            _state.value = RegisterState.Error(validationError)
            return
        }

        _state.value = RegisterState.Loading

        componentScope.launch {
            authRepository.register(
                phone = _phone.value.trim(),
                email = _email.value.trim().ifBlank { null },
                name = _name.value.trim(),
                password = _password.value
            )
                .onSuccess { _state.value = RegisterState.Success }
                .onFailure { error ->
                    _state.value = RegisterState.Error(error.message ?: "Неизвестная ошибка")
                }
        }
    }

    private fun resetError() {
        if (_state.value is RegisterState.Error) {
            _state.value = RegisterState.Idle
        }
    }

    private fun validateInput(): String? =
        when {
            _phone.value.isBlank() -> "Введите номер телефона"
            !isValidPhone(_phone.value) -> "Некорректный формат телефона (например: +79991234567)"
            _name.value.isBlank() -> "Введите ваше имя"
            _name.value.length < 2 -> "Имя должно содержать минимум 2 символа"
            _email.value.isNotBlank() && !isValidEmail(_email.value) -> "Некорректный формат email"
            _password.value.isBlank() -> "Введите пароль"
            _password.value.length < 6 -> "Пароль должен содержать минимум 6 символов"
            _confirmPassword.value.isBlank() -> "Подтвердите пароль"
            _password.value != _confirmPassword.value -> "Пароли не совпадают"
            else -> null
        }

    private fun isValidPhone(phone: String): Boolean {
        val phoneRegex = """^\+7\d{10}$""".toRegex()
        return phoneRegex.matches(phone.trim())
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = """^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""".toRegex()
        return emailRegex.matches(email.trim())
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
