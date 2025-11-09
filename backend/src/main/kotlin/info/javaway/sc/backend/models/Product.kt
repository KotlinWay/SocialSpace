package info.javaway.sc.backend.models

import kotlinx.serialization.Serializable

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

@Serializable
data class CreateProductRequest(
    val title: String,
    val description: String,
    val price: Double,
    val categoryId: Long,
    val condition: ProductCondition,
    val images: List<String>
)

@Serializable
data class UpdateProductRequest(
    val title: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val categoryId: Long? = null,
    val condition: ProductCondition? = null,
    val status: ProductStatus? = null
)
