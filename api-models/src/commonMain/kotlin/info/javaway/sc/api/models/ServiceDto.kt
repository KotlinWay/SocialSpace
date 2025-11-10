package info.javaway.sc.api.models

import kotlinx.serialization.Serializable

/**
 * Запрос на создание услуги
 */
@Serializable
data class CreateServiceRequest(
    val title: String,
    val description: String,
    val categoryId: Long,
    val price: String? = null,
    val images: List<String>
)

/**
 * Запрос на обновление услуги
 */
@Serializable
data class UpdateServiceRequest(
    val title: String? = null,
    val description: String? = null,
    val categoryId: Long? = null,
    val price: String? = null,
    val status: ServiceStatus? = null,
    val images: List<String>? = null
)

/**
 * Ответ с деталями услуги (включает информацию о пользователе и категории)
 */
@Serializable
data class ServiceResponse(
    val service: Service,
    val user: UserPublicInfo,
    val category: CategoryInfo
)

/**
 * Ответ со списком услуг (с пагинацией)
 */
@Serializable
data class ServiceListResponse(
    val services: List<Service>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)
