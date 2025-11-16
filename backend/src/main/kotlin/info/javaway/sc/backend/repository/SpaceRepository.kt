package info.javaway.sc.backend.repository

import info.javaway.sc.api.models.Space
import info.javaway.sc.api.models.SpaceMember
import info.javaway.sc.api.models.SpaceMemberRole
import info.javaway.sc.api.models.SpaceType
import info.javaway.sc.backend.data.tables.SpaceMembers
import info.javaway.sc.backend.data.tables.Spaces
import info.javaway.sc.backend.data.tables.Users
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant

/**
 * Repository для работы с пространствами и участниками.
 */
class SpaceRepository {

    fun createSpace(
        ownerId: Long,
        name: String,
        slug: String,
        description: String?,
        type: SpaceType,
        inviteCode: String?,
        logo: String?
    ): Space? = transaction {
        val insertStatement = Spaces.insert {
            it[Spaces.name] = name
            it[Spaces.slug] = slug
            it[Spaces.description] = description
            it[Spaces.logo] = logo
            it[Spaces.type] = type
            it[Spaces.inviteCode] = inviteCode
            it[Spaces.ownerId] = EntityID(ownerId, Users)
            it[Spaces.createdAt] = Instant.now()
        }

        insertStatement.resultedValues?.singleOrNull()?.let { rowToSpace(it, membersCount = 0) }
    }

    fun updateSpace(
        spaceId: Long,
        name: String? = null,
        description: String? = null,
        type: SpaceType? = null,
        logo: String? = null
    ): Space? = transaction {
        Spaces.update({ Spaces.id eq spaceId }) {
            name?.let { value -> it[Spaces.name] = value }
            description?.let { value -> it[Spaces.description] = value }
            type?.let { value -> it[Spaces.type] = value }
            logo?.let { value -> it[Spaces.logo] = value }
        }

        findById(spaceId)
    }

    fun deleteSpace(spaceId: Long): Boolean = transaction {
        Spaces.deleteWhere { Spaces.id eq spaceId } > 0
    }

    fun findById(spaceId: Long): Space? = transaction {
        Spaces
            .selectAll().where { Spaces.id eq spaceId }
            .limit(1)
            .map { row ->
                val membersCount =
                    SpaceMembers.selectAll().where { SpaceMembers.spaceId eq row[Spaces.id] }.count().toInt()
                rowToSpace(row, membersCount)
            }
            .singleOrNull()
    }

    fun findBySlug(slug: String): Space? = transaction {
        Spaces
            .selectAll().where { Spaces.slug eq slug }
            .limit(1)
            .map { row ->
                val membersCount =
                    SpaceMembers.selectAll().where { SpaceMembers.spaceId eq row[Spaces.id] }.count().toInt()
                rowToSpace(row, membersCount)
            }
            .singleOrNull()
    }

    fun listSpaces(
        type: SpaceType? = null,
        searchQuery: String? = null,
        limit: Int = 20,
        offset: Long = 0
    ): List<Space> = transaction {
        val query = Spaces.selectAll()
        applyFilters(query, type, searchQuery)

        query
            .orderBy(Spaces.createdAt to SortOrder.DESC)
            .limit(limit)
            .offset(offset)
            .map { row ->
                val membersCount =
                    SpaceMembers.selectAll().where { SpaceMembers.spaceId eq row[Spaces.id] }.count().toInt()
                rowToSpace(row, membersCount)
            }
    }

    fun countSpaces(type: SpaceType? = null, searchQuery: String? = null): Long = transaction {
        val query = Spaces.selectAll()
        applyFilters(query, type, searchQuery)
        query.count()
    }

    fun addMember(
        spaceId: Long,
        userId: Long,
        role: SpaceMemberRole
    ): SpaceMember? = transaction {
        val insert = SpaceMembers.insertIgnore {
            it[SpaceMembers.spaceId] = EntityID(spaceId, Spaces)
            it[SpaceMembers.userId] = EntityID(userId, Users)
            it[SpaceMembers.role] = role
            it[SpaceMembers.joinedAt] = Instant.now()
        }

        val memberId = insert.resultedValues?.singleOrNull()?.get(SpaceMembers.id)?.value
        memberId?.let { getMemberById(it) } ?: getMember(spaceId, userId)
    }

    fun updateMemberRole(
        spaceId: Long,
        userId: Long,
        role: SpaceMemberRole
    ): SpaceMember? = transaction {
        SpaceMembers.update({
            (SpaceMembers.spaceId eq EntityID(spaceId, Spaces)) and
                (SpaceMembers.userId eq EntityID(userId, Users))
        }) {
            it[SpaceMembers.role] = role
        }
        getMember(spaceId, userId)
    }

    fun removeMember(spaceId: Long, userId: Long): Boolean = transaction {
        SpaceMembers.deleteWhere {
            (SpaceMembers.spaceId eq EntityID(spaceId, Spaces)) and
                (SpaceMembers.userId eq EntityID(userId, Users))
        } > 0
    }

    fun getMember(spaceId: Long, userId: Long): SpaceMember? = transaction {
        SpaceMembers
            .selectAll().where {
                (SpaceMembers.spaceId eq EntityID(spaceId, Spaces)) and
                        (SpaceMembers.userId eq EntityID(userId, Users))
            }
            .limit(1)
            .map { rowToMember(it) }
            .singleOrNull()
    }

    fun listMembers(spaceId: Long, limit: Int = 100, offset: Long = 0): List<SpaceMember> = transaction {
        SpaceMembers
            .selectAll().where { SpaceMembers.spaceId eq EntityID(spaceId, Spaces) }
            .orderBy(SpaceMembers.joinedAt to SortOrder.DESC)
            .limit(limit)
            .offset(offset)
            .map { rowToMember(it) }
    }

    fun countMembers(spaceId: Long): Long = transaction {
        SpaceMembers
            .selectAll().where { SpaceMembers.spaceId eq EntityID(spaceId, Spaces) }
            .count()
    }

    private fun applyFilters(query: Query, type: SpaceType?, search: String?) {
        search?.let { searchQuery ->
            val pattern = "%${searchQuery.lowercase()}%"
            query.andWhere {
                (Spaces.name.lowerCase() like pattern) or
                    (Spaces.description.lowerCase() like pattern)
            }
        }
        type?.let { spaceType ->
            query.andWhere { Spaces.type eq spaceType }
        }
    }

    private fun rowToSpace(row: ResultRow, membersCount: Int): Space =
        Space(
            id = row[Spaces.id].value,
            name = row[Spaces.name],
            slug = row[Spaces.slug],
            description = row[Spaces.description],
            logo = row[Spaces.logo],
            type = row[Spaces.type],
            inviteCode = row[Spaces.inviteCode],
            ownerId = row[Spaces.ownerId].value,
            membersCount = membersCount,
            createdAt = row[Spaces.createdAt].toString()
        )

    private fun rowToMember(row: ResultRow): SpaceMember =
        SpaceMember(
            id = row[SpaceMembers.id].value,
            spaceId = row[SpaceMembers.spaceId].value,
            userId = row[SpaceMembers.userId].value,
            role = row[SpaceMembers.role],
            joinedAt = row[SpaceMembers.joinedAt].toString()
        )

    private fun getMemberById(memberId: Long): SpaceMember? = transaction {
        SpaceMembers
            .selectAll().where { SpaceMembers.id eq memberId }
            .limit(1)
            .map { rowToMember(it) }
            .singleOrNull()
    }
}
