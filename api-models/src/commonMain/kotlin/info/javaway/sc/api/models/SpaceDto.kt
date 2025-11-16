package info.javaway.sc.api.models

import kotlinx.serialization.Serializable

/**
 * Запрос на создание пространства
 */
@Serializable
data class CreateSpaceRequest(
    val name: String,
    val slug: String,
    val description: String? = null,
    val type: SpaceType,
    val inviteCode: String? = null,
    val logo: String? = null
)

/**
 * Запрос на обновление пространства
 */
@Serializable
data class UpdateSpaceRequest(
    val name: String? = null,
    val description: String? = null,
    val type: SpaceType? = null,
    val logo: String? = null
)

/**
 * Запрос на присоединение к приватному пространству
 */
@Serializable
data class JoinSpaceRequest(
    val inviteCode: String? = null
)

/**
 * Ответ с информацией о пространстве
 */
@Serializable
data class SpaceResponse(
    val space: Space,
    val owner: UserPublicInfo,
    val currentUserRole: SpaceMemberRole? = null
)

/**
 * Ответ со списком пространств
 */
@Serializable
data class SpaceListResponse(
    val spaces: List<SpaceResponse>,
    val total: Long
)

/**
 * Ответ об участнике пространства
 */
@Serializable
data class SpaceMemberResponse(
    val id: Long,
    val user: UserPublicInfo,
    val role: SpaceMemberRole,
    val joinedAt: String
)
