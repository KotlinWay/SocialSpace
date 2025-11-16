package info.javaway.sc.shared.data.repository

import info.javaway.sc.api.models.CreateSpaceRequest
import info.javaway.sc.api.models.JoinSpaceRequest
import info.javaway.sc.shared.data.api.ApiClient
import info.javaway.sc.shared.data.mappers.toDomain
import info.javaway.sc.shared.domain.models.Space
import info.javaway.sc.shared.domain.models.SpaceMember
import info.javaway.sc.shared.domain.models.SpaceType
import info.javaway.sc.shared.domain.repository.SpaceRepository

class SpaceRepositoryImpl(
    private val apiClient: ApiClient
) : SpaceRepository {

    override suspend fun getSpaces(
        type: SpaceType?,
        search: String?,
        page: Int,
        pageSize: Int
    ): Result<List<Space>> {
        return apiClient.getSpaces(
            type = type?.toApi(),
            search = search,
            page = page,
            pageSize = pageSize
        ).mapCatching { response ->
            response.spaces.map { it.toDomain() }
        }
    }

    override suspend fun getSpace(spaceId: Long): Result<Space> {
        return apiClient.getSpace(spaceId)
            .mapCatching { it.toDomain() }
    }

    override suspend fun createSpace(
        name: String,
        slug: String,
        description: String?,
        type: SpaceType,
        inviteCode: String?,
        logo: String?
    ): Result<Space> {
        val request = CreateSpaceRequest(
            name = name,
            slug = slug,
            description = description,
            type = type.toApi(),
            inviteCode = inviteCode,
            logo = logo
        )

        return apiClient.createSpace(request)
            .mapCatching { it.toDomain() }
    }

    override suspend fun joinSpace(spaceId: Long, inviteCode: String?): Result<Space> {
        val request = JoinSpaceRequest(inviteCode = inviteCode)
        return apiClient.joinSpace(spaceId, request)
            .mapCatching { it.toDomain() }
    }

    override suspend fun getMembers(spaceId: Long): Result<List<SpaceMember>> {
        return apiClient.getSpaceMembers(spaceId).mapCatching { members ->
            members.map { response ->
                response.toDomain()
            }
        }
    }

    private fun SpaceType.toApi(): info.javaway.sc.api.models.SpaceType =
        when (this) {
            SpaceType.PUBLIC -> info.javaway.sc.api.models.SpaceType.PUBLIC
            SpaceType.PRIVATE -> info.javaway.sc.api.models.SpaceType.PRIVATE
        }

    private fun info.javaway.sc.api.models.SpaceMemberResponse.toDomain(): SpaceMember =
        SpaceMember(
            id = id,
            user = user.toDomain(),
            role = role.toDomain(),
            joinedAt = joinedAt
        )
}
