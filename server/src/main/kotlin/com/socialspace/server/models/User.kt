package com.socialspace.server.models

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

/**
 * Роль пользователя в системе
 */
enum class UserRole {
    USER,       // Обычный пользователь
    MODERATOR,  // Модератор
    ADMIN       // Администратор
}

/**
 * Таблица пользователей в БД
 */
object Users : UUIDTable("users") {
    val phone = varchar("phone", 20).uniqueIndex()
    val email = varchar("email", 255).nullable()
    val name = varchar("name", 255)
    val avatar = varchar("avatar", 500).nullable()
    val bio = text("bio").nullable()
    val rating = float("rating").default(0f)
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    val lastActive = timestamp("last_active").clientDefault { Instant.now() }
    val role = enumerationByName("role", 20, UserRole::class).default(UserRole.USER)
}

/**
 * DTO для пользователя (Data Transfer Object)
 */
data class UserDTO(
    val id: String,
    val phone: String,
    val email: String?,
    val name: String,
    val avatar: String?,
    val bio: String?,
    val rating: Float,
    val createdAt: String,
    val lastActive: String,
    val role: UserRole
)

/**
 * DTO для создания нового пользователя
 */
data class CreateUserDTO(
    val phone: String,
    val email: String?,
    val name: String,
    val password: String  // В будущем будет хешироваться
)

/**
 * DTO для обновления профиля пользователя
 */
data class UpdateUserDTO(
    val name: String?,
    val email: String?,
    val bio: String?,
    val avatar: String?
)
