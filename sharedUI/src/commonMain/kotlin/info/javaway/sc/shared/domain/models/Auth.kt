package info.javaway.sc.shared.domain.models

/**
 * Domain модель ответа при успешной аутентификации
 */
data class AuthResponse(
    val token: String,
    val user: User
)
