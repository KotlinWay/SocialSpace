package info.javaway.sc.shared.data.mappers

import info.javaway.sc.api.models.Space as ApiSpace
import info.javaway.sc.api.models.SpaceMember as ApiSpaceMember
import info.javaway.sc.api.models.SpaceMemberRole as ApiSpaceMemberRole
import info.javaway.sc.api.models.SpaceResponse
import info.javaway.sc.api.models.SpaceType as ApiSpaceType
import info.javaway.sc.shared.domain.models.Space
import info.javaway.sc.shared.domain.models.SpaceMember
import info.javaway.sc.shared.domain.models.SpaceMemberRole
import info.javaway.sc.shared.domain.models.SpaceType
import info.javaway.sc.shared.domain.models.UserPublicInfo

fun SpaceResponse.toDomain(): Space =
    Space(
        id = space.id,
        name = space.name,
        slug = space.slug,
        description = space.description,
        logo = space.logo,
        type = space.type.toDomain(),
        inviteCode = space.inviteCode,
        ownerId = space.ownerId,
        membersCount = space.membersCount,
        createdAt = space.createdAt,
        currentUserRole = currentUserRole?.toDomain()
    )

fun ApiSpace.toDomain(currentRole: ApiSpaceMemberRole? = null): Space =
    Space(
        id = id,
        name = name,
        slug = slug,
        description = description,
        logo = logo,
        type = type.toDomain(),
        inviteCode = inviteCode,
        ownerId = ownerId,
        membersCount = membersCount,
        createdAt = createdAt,
        currentUserRole = currentRole?.toDomain()
    )

fun ApiSpaceMember.toDomain(user: UserPublicInfo): SpaceMember =
    SpaceMember(
        id = id,
        user = user,
        role = role.toDomain(),
        joinedAt = joinedAt
    )

fun ApiSpaceType.toDomain(): SpaceType =
    when (this) {
        ApiSpaceType.PUBLIC -> SpaceType.PUBLIC
        ApiSpaceType.PRIVATE -> SpaceType.PRIVATE
    }

fun ApiSpaceMemberRole.toDomain(): SpaceMemberRole =
    when (this) {
        ApiSpaceMemberRole.OWNER -> SpaceMemberRole.OWNER
        ApiSpaceMemberRole.ADMIN -> SpaceMemberRole.ADMIN
        ApiSpaceMemberRole.MEMBER -> SpaceMemberRole.MEMBER
    }

fun UserPublicInfo.toDomain(): info.javaway.sc.shared.domain.models.UserPublicInfo =
    info.javaway.sc.shared.domain.models.UserPublicInfo(
        id = id,
        name = name,
        avatar = avatar,
        phone = phone,
        rating = rating,
        isVerified = isVerified
    )
