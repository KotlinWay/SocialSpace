package info.javaway.sc.shared.domain.models

import kotlinx.serialization.Serializable

/**
 * Товар
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

@Serializable
enum class ProductCondition {
    NEW,
    USED
}

@Serializable
enum class ProductStatus {
    ACTIVE,
    SOLD,
    ARCHIVED
}

/**
 * Расширенный ответ с информацией о товаре
 */
@Serializable
data class ProductResponse(
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
 * Список товаров с пагинацией
 */
@Serializable
data class ProductListResponse(
    val products: List<ProductResponse>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)
