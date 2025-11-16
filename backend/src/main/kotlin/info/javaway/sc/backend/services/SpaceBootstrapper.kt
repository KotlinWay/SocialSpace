package info.javaway.sc.backend.services

import info.javaway.sc.api.models.SpaceMemberRole
import info.javaway.sc.api.models.SpaceType
import info.javaway.sc.api.models.UserRole
import info.javaway.sc.backend.data.tables.Products
import info.javaway.sc.backend.data.tables.Services
import info.javaway.sc.backend.data.tables.SpaceMembers
import info.javaway.sc.backend.data.tables.Spaces
import info.javaway.sc.backend.data.tables.Users
import info.javaway.sc.backend.utils.PasswordHasher
import info.javaway.sc.backend.utils.SpaceDefaults
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.slf4j.Logger
import java.time.Instant

/**
 * Отвечает за создание демо-пространства и миграцию существующих данных.
 */
class SpaceBootstrapper(
    private val logger: Logger
) {

    /**
     * Создает (при необходимости) демо-владельца и пространство.
     */
    fun ensureDemoOwnerAndSpace(): Pair<Long, Long> {
        val ownerId = ensureDemoOwnerUser()
        val spaceId = ensureDemoSpace(ownerId)
        return ownerId to spaceId
    }

    /**
     * Подключает всех пользователей/данные к пространству.
     * Требует, чтобы таблицы SpaceMembers/Products/Services уже существовали.
     */
    fun ensureMembershipsAndData(spaceId: Long, ownerId: Long) {
        ensureOwnerMembership(spaceId, ownerId)
        ensureAllUsersInSpace(spaceId, ownerId)
        ensureProductsAttached(spaceId)
        ensureServicesAttached(spaceId)
    }

    private fun ensureDemoOwnerUser(): Long {
        val existing = Users
            .selectAll()
            .where { Users.phone eq SpaceDefaults.DEFAULT_SPACE_OWNER_PHONE }
            .firstOrNull()

        if (existing != null) {
            logger.info("Demo owner user already exists (id=${existing[Users.id].value})")
            return existing[Users.id].value
        }

        val userId = Users.insertIgnore {
            it[phone] = SpaceDefaults.DEFAULT_SPACE_OWNER_PHONE
            it[email] = "demo@pulsar.app"
            it[name] = SpaceDefaults.DEFAULT_SPACE_OWNER_NAME
            it[passwordHash] = PasswordHasher.hash(SpaceDefaults.DEFAULT_SPACE_OWNER_PASSWORD)
            it[avatar] = null
            it[bio] = "Системный владелец демо-пространства"
            it[rating] = null
            it[defaultSpaceId] = SpaceDefaults.DEFAULT_SPACE_ID
            it[createdAt] = Instant.now()
            it[isVerified] = true
            it[role] = UserRole.ADMIN
        }.resultedValues?.singleOrNull()?.get(Users.id)?.value

        logger.info("Created demo owner user (id=$userId)")
        return userId ?: SpaceDefaults.DEFAULT_SPACE_ID
    }

    private fun ensureDemoSpace(ownerId: Long): Long {
        val existingBySlug = Spaces
            .selectAll()
            .where { Spaces.slug eq SpaceDefaults.DEFAULT_SPACE_SLUG }
            .firstOrNull()
        if (existingBySlug != null) {
            logger.info("Demo space already exists (id=${existingBySlug[Spaces.id].value})")
            return existingBySlug[Spaces.id].value
        }

        val spaceId = Spaces.insertIgnore {
            it[id] = EntityID(SpaceDefaults.DEFAULT_SPACE_ID, Spaces)
            it[name] = SpaceDefaults.DEFAULT_SPACE_NAME
            it[slug] = SpaceDefaults.DEFAULT_SPACE_SLUG
            it[description] = SpaceDefaults.DEFAULT_SPACE_DESCRIPTION
            it[logo] = null
            it[type] = SpaceType.PUBLIC
            it[inviteCode] = null
            it[Spaces.ownerId] = EntityID(ownerId, Users)
            it[createdAt] = Instant.now()
        }.resultedValues?.singleOrNull()?.get(Spaces.id)?.value ?: SpaceDefaults.DEFAULT_SPACE_ID

        logger.info("Demo space ensured (id=$spaceId)")
        return spaceId
    }

    private fun ensureOwnerMembership(spaceId: Long, ownerId: Long) {
        SpaceMembers.insertIgnore {
            it[SpaceMembers.spaceId] = EntityID(spaceId, Spaces)
            it[SpaceMembers.userId] = EntityID(ownerId, Users)
            it[SpaceMembers.role] = SpaceMemberRole.OWNER
            it[SpaceMembers.joinedAt] = Instant.now()
        }
    }

    private fun ensureAllUsersInSpace(spaceId: Long, ownerId: Long) {
        val allUsers = Users
            .selectAll()
            .map { it[Users.id].value }

        Users.update({ Users.defaultSpaceId neq spaceId }) {
            it[Users.defaultSpaceId] = spaceId
        }

        allUsers.forEach { userId ->
            SpaceMembers.insertIgnore {
                it[SpaceMembers.spaceId] = EntityID(spaceId, Spaces)
                it[SpaceMembers.userId] = EntityID(userId, Users)
                it[SpaceMembers.role] = if (userId == ownerId) {
                    SpaceMemberRole.OWNER
                } else {
                    SpaceMemberRole.MEMBER
                }
                it[SpaceMembers.joinedAt] = Instant.now()
            }
        }
    }

    private fun ensureProductsAttached(spaceId: Long) {
        Products.update({ Products.spaceId neq EntityID(spaceId, Spaces) }) {
            it[Products.spaceId] = EntityID(spaceId, Spaces)
        }
    }

    private fun ensureServicesAttached(spaceId: Long) {
        Services.update({ Services.spaceId neq EntityID(spaceId, Spaces) }) {
            it[Services.spaceId] = EntityID(spaceId, Spaces)
        }
    }
}
