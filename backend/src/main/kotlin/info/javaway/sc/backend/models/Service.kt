package info.javaway.sc.backend.models

import kotlinx.serialization.Serializable

@Serializable
data class Service(
    val id: Long,
    val userId: Long,
    val title: String,
    val description: String,
    val categoryId: Long,
    val price: String? = null, // "1000" или "Договорная"
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

@Serializable
data class CreateServiceRequest(
    val title: String,
    val description: String,
    val categoryId: Long,
    val price: String? = null,
    val images: List<String>
)

@Serializable
data class UpdateServiceRequest(
    val title: String? = null,
    val description: String? = null,
    val categoryId: Long? = null,
    val price: String? = null,
    val status: ServiceStatus? = null
)
