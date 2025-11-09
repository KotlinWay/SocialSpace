package info.javaway.sc.backend.data.tables

import info.javaway.sc.backend.models.UserRole
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object Users : LongIdTable("users") {
    val phone = varchar("phone", 20).uniqueIndex()
    val email = varchar("email", 100).nullable()
    val passwordHash = varchar("password_hash", 100)
    val name = varchar("name", 100)
    val avatar = varchar("avatar", 500).nullable()
    val bio = text("bio").nullable()
    val rating = double("rating").nullable()
    val createdAt = datetime("created_at")
    val isVerified = bool("is_verified").default(false)
    val role = enumerationByName("role", 20, UserRole::class).default(UserRole.USER)
}
