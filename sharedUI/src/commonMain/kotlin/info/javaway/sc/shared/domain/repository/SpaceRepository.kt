package info.javaway.sc.shared.domain.repository

import info.javaway.sc.shared.domain.models.Space
import info.javaway.sc.shared.domain.models.SpaceMember
import info.javaway.sc.shared.domain.models.SpaceType

interface SpaceRepository {
    suspend fun getSpaces(
        type: SpaceType? = null,
        search: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): Result<List<Space>>

    suspend fun getSpace(spaceId: Long): Result<Space>

    suspend fun createSpace(
        name: String,
        slug: String,
        description: String?,
        type: SpaceType,
        inviteCode: String? = null,
        logo: String? = null
    ): Result<Space>

    suspend fun joinSpace(spaceId: Long, inviteCode: String? = null): Result<Space>

    suspend fun getMembers(spaceId: Long): Result<List<SpaceMember>>
}
