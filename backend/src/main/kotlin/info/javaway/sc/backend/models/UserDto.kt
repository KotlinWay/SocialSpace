package info.javaway.sc.backend.models

import kotlinx.serialization.Serializable

/**
 * DTO для обновления профиля пользователя
 */
@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val email: String? = null,
    val bio: String? = null
)

/**
 * DTO для ответа с ошибкой
 */
@Serializable
data class ErrorResponse(
    val error: String,
    val message: String
)

/**
 * DTO для успешного ответа
 */
@Serializable
data class SuccessResponse(
    val success: Boolean,
    val message: String? = null
)

/**
 * DTO для обновления токена
 */
@Serializable
data class RefreshTokenRequest(
    val token: String
)

/**
 * DTO для ответа с токеном
 */
@Serializable
data class TokenResponse(
    val token: String,
    val expiresIn: Long
)

/**
 * DTO для публичного профиля пользователя (без конфиденциальных данных)
 */
@Serializable
data class PublicUserProfile(
    val id: Long,
    val name: String,
    val avatar: String? = null,
    val bio: String? = null,
    val rating: Double? = null,
    val isVerified: Boolean = false,
    val createdAt: String
)

/**
 * Расширение для преобразования User в PublicUserProfile
 */
fun User.toPublicProfile(): PublicUserProfile {
    return PublicUserProfile(
        id = id,
        name = name,
        avatar = avatar,
        bio = bio,
        rating = rating,
        isVerified = isVerified,
        createdAt = createdAt
    )
}

/**
 * DTO для ответа после загрузки файла
 */
@Serializable
data class FileUploadResponse(
    val success: Boolean,
    val url: String? = null,
    val fileName: String? = null,
    val message: String? = null
)
