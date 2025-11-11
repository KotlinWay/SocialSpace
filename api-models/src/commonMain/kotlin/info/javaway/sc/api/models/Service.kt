package info.javaway.sc.api.models

import kotlinx.serialization.Serializable

/**
 * Модель услуги
 */
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

/**
 * Статус услуги
 */
@Serializable
enum class ServiceStatus {
    ACTIVE,
    INACTIVE
}
