package info.javaway.sc.shared.domain.models

/**
 * Domain модель товара
 * Используется внутри приложения для бизнес-логики и UI
 */
data class Product(
    val id: Long,
    val title: String,
    val description: String,
    val price: Double,
    val condition: ProductCondition,
    val images: List<String>,
    val status: ProductStatus,
    val views: Int,
    val createdAt: String,
    val updatedAt: String,
    val user: UserPublicInfo,
    val category: CategoryInfo,
    val isFavorite: Boolean = false
)

/**
 * Состояние товара (новое/б/у)
 */
enum class ProductCondition {
    NEW,
    USED
}

/**
 * Статус товара в системе
 */
enum class ProductStatus {
    ACTIVE,
    SOLD,
    ARCHIVED
}
