package info.javaway.sc.backend.data.tables

import info.javaway.sc.api.models.SpaceMemberRole
import info.javaway.sc.api.models.SpaceType
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object Spaces : LongIdTable("spaces") {
    val name = varchar("name", 200)
    val slug = varchar("slug", 200).uniqueIndex()
    val description = text("description").nullable()
    val logo = varchar("logo", 500).nullable()
    val type = enumerationByName("type", 20, SpaceType::class)
    val inviteCode = varchar("invite_code", 64).nullable()
    val ownerId = reference("owner_id", Users, onDelete = ReferenceOption.CASCADE)
    val createdAt = timestamp("created_at").default(Instant.now())
}

object SpaceMembers : LongIdTable("space_members") {
    val spaceId = reference("space_id", Spaces, onDelete = ReferenceOption.CASCADE)
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val role = enumerationByName("role", 20, SpaceMemberRole::class)
    val joinedAt = timestamp("joined_at").default(Instant.now())

    init {
        uniqueIndex("uq_space_members_space_user", spaceId, userId)
    }
}
