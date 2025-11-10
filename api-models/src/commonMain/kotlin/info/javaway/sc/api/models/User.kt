package info.javaway.sc.api.models

import kotlinx.serialization.Serializable

/**
 * Модель пользователя (полная)
 */
@Serializable
data class User(
    val id: Long,
    val phone: String,
    val email: String? = null,
    val name: String,
    val avatar: String? = null,
    val bio: String? = null,
    val rating: Double? = null,
    val createdAt: String,
    val isVerified: Boolean = false,
    val role: UserRole = UserRole.USER
)

/**
 * Роли пользователей
 */
@Serializable
enum class UserRole {
    USER,
    MODERATOR,
    ADMIN
}

/**
 * Публичная информация о пользователе (используется в списках товаров/услуг)
 */
@Serializable
data class UserPublicInfo(
    val id: Long,
    val name: String,
    val avatar: String?,
    val phone: String,
    val rating: Double?,
    val isVerified: Boolean = false
)

/**
 * Публичный профиль пользователя (без конфиденциальных данных)
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
 * Расширение для преобразования User в UserPublicInfo
 */
fun User.toPublicInfo(): UserPublicInfo {
    return UserPublicInfo(
        id = id,
        name = name,
        avatar = avatar,
        phone = phone,
        rating = rating,
        isVerified = isVerified
    )
}
