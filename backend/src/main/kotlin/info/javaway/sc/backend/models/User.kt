package info.javaway.sc.backend.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

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

@Serializable
data class RegisterRequest(
    val phone: String,
    val email: String? = null,
    val name: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val phone: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: User
)
