package info.javaway.sc.shared.domain.models

import kotlinx.serialization.Serializable

/**
 * Пользователь
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

@Serializable
enum class UserRole {
    USER,
    MODERATOR,
    ADMIN
}

/**
 * Публичная информация о пользователе (для списков товаров/услуг)
 */
@Serializable
data class UserPublicInfo(
    val id: Long,
    val name: String,
    val avatar: String?,
    val phone: String,
    val rating: Double?
)
