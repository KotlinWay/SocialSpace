package info.javaway.sc.api.models

import kotlinx.serialization.Serializable

/**
 * Запрос на регистрацию
 */
@Serializable
data class RegisterRequest(
    val phone: String,
    val email: String? = null,
    val name: String,
    val password: String
)

/**
 * Запрос на вход
 */
@Serializable
data class LoginRequest(
    val phone: String,
    val password: String
)

/**
 * Ответ при успешной аутентификации
 */
@Serializable
data class AuthResponse(
    val token: String,
    val user: User
)

/**
 * Запрос на обновление профиля
 */
@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val email: String? = null,
    val bio: String? = null
)

/**
 * Запрос на обновление токена
 */
@Serializable
data class RefreshTokenRequest(
    val token: String
)

/**
 * Ответ с токеном
 */
@Serializable
data class TokenResponse(
    val token: String,
    val expiresIn: Long
)
