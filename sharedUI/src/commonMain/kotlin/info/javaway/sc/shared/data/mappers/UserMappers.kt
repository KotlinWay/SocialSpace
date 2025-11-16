package info.javaway.sc.shared.data.mappers

import info.javaway.sc.api.models.User as ApiUser
import info.javaway.sc.api.models.UserPublicInfo as ApiUserPublicInfo
import info.javaway.sc.api.models.UserRole as ApiUserRole
import info.javaway.sc.api.models.AuthResponse as ApiAuthResponse
import info.javaway.sc.shared.domain.models.User
import info.javaway.sc.shared.domain.models.UserPublicInfo
import info.javaway.sc.shared.domain.models.UserRole
import info.javaway.sc.shared.domain.models.AuthResponse

/**
 * Маппер: User (API DTO) → User (Domain)
 */
fun ApiUser.toDomain(): User {
    return User(
        id = id,
        phone = phone,
        email = email,
        name = name,
        avatar = avatar,
        bio = bio,
        rating = rating,
        defaultSpaceId = defaultSpaceId,
        createdAt = createdAt,
        isVerified = isVerified,
        role = role.toDomain()
    )
}

/**
 * Маппер: UserPublicInfo (API DTO) → UserPublicInfo (Domain)
 */
fun ApiUserPublicInfo.toDomain(): UserPublicInfo {
    return UserPublicInfo(
        id = id,
        name = name,
        avatar = avatar,
        phone = phone,
        rating = rating,
        isVerified = isVerified
    )
}

/**
 * Маппер: UserRole (API) → UserRole (Domain)
 */
fun ApiUserRole.toDomain(): UserRole {
    return when (this) {
        ApiUserRole.USER -> UserRole.USER
        ApiUserRole.MODERATOR -> UserRole.MODERATOR
        ApiUserRole.ADMIN -> UserRole.ADMIN
    }
}

/**
 * Маппер: AuthResponse (API DTO) → AuthResponse (Domain)
 */
fun ApiAuthResponse.toDomain(): AuthResponse {
    return AuthResponse(
        token = token,
        user = user.toDomain()
    )
}
