package info.javaway.sc.api.models

import kotlinx.serialization.Serializable

/**
 * Модель товара
 */
@Serializable
data class Product(
    val id: Long,
    val userId: Long,
    val title: String,
    val description: String,
    val price: Double,
    val categoryId: Long,
    val condition: ProductCondition,
    val images: List<String>,
    val status: ProductStatus,
    val views: Int = 0,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Состояние товара
 */
@Serializable
enum class ProductCondition {
    NEW,
    USED
}

/**
 * Статус товара
 */
@Serializable
enum class ProductStatus {
    ACTIVE,
    SOLD,
    ARCHIVED
}
