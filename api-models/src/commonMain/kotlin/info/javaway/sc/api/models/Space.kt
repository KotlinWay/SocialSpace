package info.javaway.sc.api.models

import kotlinx.serialization.Serializable

/**
 * Базовая информация о пространстве (ЖК)
 */
@Serializable
data class Space(
    val id: Long,
    val name: String,
    val slug: String,
    val description: String? = null,
    val logo: String? = null,
    val type: SpaceType,
    val inviteCode: String? = null,
    val ownerId: Long,
    val membersCount: Int,
    val createdAt: String
)

/**
 * Участник пространства
 */
@Serializable
data class SpaceMember(
    val id: Long,
    val spaceId: Long,
    val userId: Long,
    val role: SpaceMemberRole,
    val joinedAt: String
)

/**
 * Тип пространства (публичное или приватное)
 */
@Serializable
enum class SpaceType {
    PUBLIC,
    PRIVATE
}

/**
 * Роль участника пространства
 */
@Serializable
enum class SpaceMemberRole {
    OWNER,
    ADMIN,
    MEMBER
}
