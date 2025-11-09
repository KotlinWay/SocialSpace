package info.javaway.sc.shared.domain.models

import kotlinx.serialization.Serializable

/**
 * Услуга
 */
@Serializable
data class Service(
    val id: Long,
    val userId: Long,
    val title: String,
    val description: String,
    val categoryId: Long,
    val price: String? = null,
    val images: List<String>,
    val status: ServiceStatus,
    val views: Int = 0,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
enum class ServiceStatus {
    ACTIVE,
    INACTIVE
}

/**
 * Расширенный ответ с информацией об услуге
 */
@Serializable
data class ServiceResponse(
    val id: Long,
    val title: String,
    val description: String,
    val price: String?,
    val images: List<String>,
    val status: ServiceStatus,
    val views: Int,
    val createdAt: String,
    val updatedAt: String,
    val user: UserPublicInfo,
    val category: CategoryInfo
)

/**
 * Список услуг с пагинацией
 */
@Serializable
data class ServiceListResponse(
    val services: List<ServiceResponse>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)
