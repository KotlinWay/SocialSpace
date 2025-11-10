package info.javaway.sc.shared.presentation.screens.auth

import info.javaway.sc.shared.domain.models.RegisterRequest
import info.javaway.sc.shared.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана регистрации
 */
class RegisterViewModel(
    private val authRepository: AuthRepository
) {
    private val _phone = MutableStateFlow("")
    val phone = _phone.asStateFlow()

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword = _confirmPassword.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()

    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    fun onPhoneChange(phone: String) {
        _phone.value = phone
        _error.value = null
    }

    fun onNameChange(name: String) {
        _name.value = name
        _error.value = null
    }

    fun onEmailChange(email: String) {
        _email.value = email
        _error.value = null
    }

    fun onPasswordChange(password: String) {
        _password.value = password
        _error.value = null
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _confirmPassword.value = confirmPassword
        _error.value = null
    }

    fun register() {
        if (!validateInput()) {
            return
        }

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            val request = RegisterRequest(
                phone = _phone.value.trim(),
                name = _name.value.trim(),
                email = _email.value.trim().ifBlank { null },
                password = _password.value
            )

            authRepository.register(request)
                .onSuccess {
                    _isLoading.value = false
                    _isSuccess.value = true
                }
                .onFailure { error ->
                    _isLoading.value = false
                    _error.value = error.message ?: "Неизвестная ошибка"
                }
        }
    }

    private fun validateInput(): Boolean {
        when {
            _phone.value.isBlank() -> {
                _error.value = "Введите номер телефона"
                return false
            }
            !isValidPhone(_phone.value) -> {
                _error.value = "Некорректный формат телефона (например: +79991234567)"
                return false
            }
            _name.value.isBlank() -> {
                _error.value = "Введите ваше имя"
                return false
            }
            _name.value.length < 2 -> {
                _error.value = "Имя должно содержать минимум 2 символа"
                return false
            }
            _email.value.isNotBlank() && !isValidEmail(_email.value) -> {
                _error.value = "Некорректный формат email"
                return false
            }
            _password.value.isBlank() -> {
                _error.value = "Введите пароль"
                return false
            }
            _password.value.length < 6 -> {
                _error.value = "Пароль должен содержать минимум 6 символов"
                return false
            }
            _confirmPassword.value.isBlank() -> {
                _error.value = "Подтвердите пароль"
                return false
            }
            _password.value != _confirmPassword.value -> {
                _error.value = "Пароли не совпадают"
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
