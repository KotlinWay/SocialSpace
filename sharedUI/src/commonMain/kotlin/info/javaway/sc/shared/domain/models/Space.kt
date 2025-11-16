package info.javaway.sc.shared.domain.models

/**
 * Domain модель пространства (ЖК).
 */
data class Space(
    val id: Long,
    val name: String,
    val slug: String,
    val description: String?,
    val logo: String?,
    val type: SpaceType,
    val inviteCode: String?,
    val ownerId: Long,
    val membersCount: Int,
    val createdAt: String,
    val currentUserRole: SpaceMemberRole?
)

/**
 * Domain модель участника пространства.
 */
data class SpaceMember(
    val id: Long,
    val user: UserPublicInfo,
    val role: SpaceMemberRole,
    val joinedAt: String
)

enum class SpaceType {
    PUBLIC,
    PRIVATE
}

enum class SpaceMemberRole {
    OWNER,
    ADMIN,
    MEMBER
}
