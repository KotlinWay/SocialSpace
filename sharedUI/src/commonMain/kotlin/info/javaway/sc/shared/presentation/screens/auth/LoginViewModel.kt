package info.javaway.sc.shared.presentation.screens.auth

import info.javaway.sc.shared.domain.models.LoginRequest
import info.javaway.sc.shared.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана входа
 */
class LoginViewModel(
    private val authRepository: AuthRepository
) {
    private val _phone = MutableStateFlow("")
    val phone = _phone.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

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

    fun onPasswordChange(password: String) {
        _password.value = password
        _error.value = null
    }

    fun login() {
        if (!validateInput()) {
            return
        }

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            val request = LoginRequest(
                phone = _phone.value.trim(),
                password = _password.value
            )

            authRepository.login(request)
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
            _password.value.isBlank() -> {
                _error.value = "Введите пароль"
                return false
            }
            _password.value.length < 6 -> {
                _error.value = "Пароль должен содержать минимум 6 символов"
                return false
            }
        }
        return true
    }
}
