package info.javaway.sc.backend.services

import info.javaway.sc.api.models.CreateSpaceRequest
import info.javaway.sc.api.models.JoinSpaceRequest
import info.javaway.sc.api.models.Space
import info.javaway.sc.api.models.SpaceListResponse
import info.javaway.sc.api.models.SpaceMemberResponse
import info.javaway.sc.api.models.SpaceMemberRole
import info.javaway.sc.api.models.SpaceResponse
import info.javaway.sc.api.models.SpaceType
import info.javaway.sc.api.models.UpdateSpaceRequest
import info.javaway.sc.api.models.User
import info.javaway.sc.api.models.UserPublicInfo
import info.javaway.sc.backend.repository.SpaceRepository
import info.javaway.sc.backend.repository.UserRepository
import io.ktor.http.HttpStatusCode

/**
 * Бизнес-логика для управления пространствами и членством.
 */
class SpaceService(
    private val spaceRepository: SpaceRepository = SpaceRepository(),
    private val userRepository: UserRepository = UserRepository()
) {

    class SpaceException(
        val status: HttpStatusCode,
        val code: String,
        override val message: String
    ) : RuntimeException(message)

    fun listSpaces(
        currentUserId: Long?,
        type: SpaceType?,
        search: String?,
        page: Int,
        pageSize: Int
    ): SpaceListResponse {
        val offset = ((page - 1) * pageSize).toLong()
        val spaces = spaceRepository.listSpaces(type, search, limit = pageSize, offset = offset)
        val total = spaceRepository.countSpaces(type, search)
        val responses = spaces.map { buildSpaceResponse(it, currentUserId) }
        return SpaceListResponse(
            spaces = responses,
            total = total
        )
    }

    fun getSpace(spaceId: Long, currentUserId: Long?): SpaceResponse {
        val space = spaceRepository.findById(spaceId)
            ?: throw SpaceException(HttpStatusCode.NotFound, "SPACE_NOT_FOUND", "Пространство не найдено")
        return buildSpaceResponse(space, currentUserId)
    }

    fun createSpace(ownerId: Long, request: CreateSpaceRequest): SpaceResponse {
        if (spaceRepository.findBySlug(request.slug) != null) {
            throw SpaceException(HttpStatusCode.Conflict, "SPACE_SLUG_EXISTS", "Пространство с таким slug уже существует")
        }

        if (request.type == SpaceType.PRIVATE && request.inviteCode.isNullOrBlank()) {
            throw SpaceException(HttpStatusCode.BadRequest, "INVITE_REQUIRED", "Для приватного пространства нужен inviteCode")
        }

        val space = spaceRepository.createSpace(
            ownerId = ownerId,
            name = request.name,
            slug = request.slug,
            description = request.description,
            type = request.type,
            inviteCode = request.inviteCode,
            logo = request.logo
        ) ?: throw SpaceException(HttpStatusCode.InternalServerError, "SPACE_CREATE_FAILED", "Не удалось создать пространство")

        // Создаем членство владельца
        spaceRepository.addMember(space.id, ownerId, SpaceMemberRole.OWNER)

        return buildSpaceResponse(space, ownerId)
    }

    fun updateSpace(spaceId: Long, ownerId: Long, request: UpdateSpaceRequest): SpaceResponse {
        val member = spaceRepository.getMember(spaceId, ownerId)
            ?: throw SpaceException(HttpStatusCode.Forbidden, "SPACE_ACCESS_DENIED", "Вы не состоите в пространстве")

        if (member.role != SpaceMemberRole.OWNER && member.role != SpaceMemberRole.ADMIN) {
            throw SpaceException(HttpStatusCode.Forbidden, "SPACE_FORBIDDEN", "Недостаточно прав для редактирования пространства")
        }

        val updated = spaceRepository.updateSpace(
            spaceId = spaceId,
            name = request.name,
            description = request.description,
            type = request.type,
            logo = request.logo
        ) ?: throw SpaceException(HttpStatusCode.InternalServerError, "SPACE_UPDATE_FAILED", "Не удалось обновить пространство")

        return buildSpaceResponse(updated, ownerId)
    }

    fun joinSpace(spaceId: Long, userId: Long, request: JoinSpaceRequest): SpaceResponse {
        val space = spaceRepository.findById(spaceId)
            ?: throw SpaceException(HttpStatusCode.NotFound, "SPACE_NOT_FOUND", "Пространство не найдено")

        val existing = spaceRepository.getMember(spaceId, userId)
        if (existing != null) {
            return buildSpaceResponse(space, userId)
        }

        if (space.type == SpaceType.PRIVATE) {
            if (space.inviteCode.isNullOrBlank() || request.inviteCode.isNullOrBlank()) {
                throw SpaceException(HttpStatusCode.Forbidden, "INVITE_REQUIRED", "Для приватного пространства требуется код приглашения")
            }

            if (!space.inviteCode.equals(request.inviteCode, ignoreCase = true)) {
                throw SpaceException(HttpStatusCode.Forbidden, "INVITE_INVALID", "Неверный код приглашения")
            }
        }

        spaceRepository.addMember(spaceId, userId, SpaceMemberRole.MEMBER)
        return buildSpaceResponse(space, userId)
    }

    fun listMembers(spaceId: Long): List<SpaceMemberResponse> {
        val space = spaceRepository.findById(spaceId)
            ?: throw SpaceException(HttpStatusCode.NotFound, "SPACE_NOT_FOUND", "Пространство не найдено")

        return spaceRepository.listMembers(space.id)
            .mapNotNull { member ->
                val user = userRepository.findById(member.userId) ?: return@mapNotNull null
                SpaceMemberResponse(
                    id = member.id,
                    user = user.toPublicInfo(),
                    role = member.role,
                    joinedAt = member.joinedAt
                )
            }
    }

    fun ensureMembership(spaceId: Long, userId: Long): SpaceMemberRole {
        val membership = spaceRepository.getMember(spaceId, userId)
            ?: throw SpaceException(HttpStatusCode.Forbidden, "SPACE_ACCESS_DENIED", "Пользователь не состоит в пространстве")
        return membership.role
    }

    private fun buildSpaceResponse(space: Space, currentUserId: Long?): SpaceResponse {
        val owner = userRepository.findById(space.ownerId)
            ?: throw SpaceException(HttpStatusCode.InternalServerError, "OWNER_NOT_FOUND", "Владелец пространства не найден")

        val currentRole = currentUserId?.let { userId ->
            spaceRepository.getMember(space.id, userId)?.role
        }

        return SpaceResponse(
            space = space,
            owner = owner.toPublicInfo(),
            currentUserRole = currentRole
        )
    }

    private fun User.toPublicInfo(): UserPublicInfo =
        UserPublicInfo(
            id = id,
            name = name,
            avatar = avatar,
            phone = phone,
            rating = rating,
            isVerified = isVerified
        )
}
