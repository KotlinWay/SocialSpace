package info.javaway.sc.shared.domain.models

/**
 * Domain модель услуги
 */
data class Service(
    val id: Long,
    val title: String,
    val description: String,
    val price: String?, // "1000" или "Договорная"
    val images: List<String>,
    val status: ServiceStatus,
    val views: Int,
    val createdAt: String,
    val updatedAt: String,
    val user: UserPublicInfo,
    val category: CategoryInfo
)

/**
 * Статус услуги
 */
enum class ServiceStatus {
    ACTIVE,
    INACTIVE
}
