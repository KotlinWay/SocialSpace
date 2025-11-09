package info.javaway.sc.backend.services

/**
 * Сервис для валидации входных данных
 */
object ValidationService {

    /**
     * Валидация номера телефона
     * Формат: +7XXXXXXXXXX или 8XXXXXXXXXX (10 цифр после кода)
     */
    fun validatePhone(phone: String): ValidationResult {
        if (phone.isBlank()) {
            return ValidationResult(false, "Номер телефона не может быть пустым")
        }

        val cleanPhone = phone.replace(Regex("[\\s\\-()]"), "")

        val phoneRegex = Regex("^(\\+7|8|7)?\\d{10}$")
        if (!phoneRegex.matches(cleanPhone)) {
            return ValidationResult(
                false,
                "Неверный формат номера телефона. Используйте формат: +7XXXXXXXXXX"
            )
        }

        return ValidationResult(true)
    }

    /**
     * Валидация email
     */
    fun validateEmail(email: String?): ValidationResult {
        if (email.isNullOrBlank()) {
            return ValidationResult(true) // Email опциональный
        }

        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
        if (!emailRegex.matches(email)) {
            return ValidationResult(false, "Неверный формат email")
        }

        return ValidationResult(true)
    }

    /**
     * Валидация пароля
     * Требования: минимум 6 символов
     */
    fun validatePassword(password: String): ValidationResult {
        if (password.isBlank()) {
            return ValidationResult(false, "Пароль не может быть пустым")
        }

        if (password.length < 6) {
            return ValidationResult(false, "Пароль должен содержать минимум 6 символов")
        }

        if (password.length > 100) {
            return ValidationResult(false, "Пароль слишком длинный (максимум 100 символов)")
        }

        return ValidationResult(true)
    }

    /**
     * Валидация имени пользователя
     */
    fun validateName(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult(false, "Имя не может быть пустым")
        }

        if (name.length < 2) {
            return ValidationResult(false, "Имя должно содержать минимум 2 символа")
        }

        if (name.length > 100) {
            return ValidationResult(false, "Имя слишком длинное (максимум 100 символов)")
        }

        return ValidationResult(true)
    }

    /**
     * Валидация биографии
     */
    fun validateBio(bio: String?): ValidationResult {
        if (bio.isNullOrBlank()) {
            return ValidationResult(true) // Bio опциональная
        }

        if (bio.length > 500) {
            return ValidationResult(false, "Биография слишком длинная (максимум 500 символов)")
        }

        return ValidationResult(true)
    }

    /**
     * Нормализация номера телефона к единому формату
     * Преобразует в формат: +7XXXXXXXXXX
     */
    fun normalizePhone(phone: String): String {
        val cleanPhone = phone.replace(Regex("[\\s\\-()]"), "")

        return when {
            cleanPhone.startsWith("+7") -> cleanPhone
            cleanPhone.startsWith("8") -> "+7${cleanPhone.substring(1)}"
            cleanPhone.startsWith("7") -> "+$cleanPhone"
            else -> "+7$cleanPhone"
        }
    }
}

/**
 * Результат валидации
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)
