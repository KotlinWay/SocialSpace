package info.javaway.sc.shared.domain.models

/**
 * Domain модель пользователя (полная)
 */
data class User(
    val id: Long,
    val phone: String,
    val email: String?,
    val name: String,
    val avatar: String?,
    val bio: String?,
    val rating: Double?,
    val defaultSpaceId: Long?,
    val createdAt: String,
    val isVerified: Boolean = false,
    val role: UserRole = UserRole.USER
)

/**
 * Роли пользователей
 */
enum class UserRole {
    USER,
    MODERATOR,
    ADMIN
}

/**
 * Публичная информация о пользователе
 * Используется в списках товаров/услуг
 */
data class UserPublicInfo(
    val id: Long,
    val name: String,
    val avatar: String?,
    val phone: String,
    val rating: Double?,
    val isVerified: Boolean = false
)
