package info.javaway.sc.backend.data.tables

import info.javaway.sc.api.models.UserRole
import info.javaway.sc.backend.utils.SpaceDefaults
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Users : LongIdTable("users") {
    val phone = varchar("phone", 20).uniqueIndex()
    val email = varchar("email", 100).nullable()
    val passwordHash = varchar("password_hash", 100)
    val name = varchar("name", 100)
    val avatar = varchar("avatar", 500).nullable()
    val bio = text("bio").nullable()
    val rating = double("rating").nullable()
    val defaultSpaceId = long("default_space_id").default(SpaceDefaults.DEFAULT_SPACE_ID)
    val createdAt = timestamp("created_at").default(Instant.now())
    val isVerified = bool("is_verified").default(false)
    val role = enumerationByName("role", 20, UserRole::class).default(UserRole.USER)
}
