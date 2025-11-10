package info.javaway.sc.api.models

import kotlinx.serialization.Serializable

/**
 * Запрос на создание товара
 */
@Serializable
data class CreateProductRequest(
    val title: String,
    val description: String,
    val price: Double,
    val categoryId: Long,
    val condition: ProductCondition,
    val images: List<String>
)

/**
 * Запрос на обновление товара
 */
@Serializable
data class UpdateProductRequest(
    val title: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val categoryId: Long? = null,
    val condition: ProductCondition? = null,
    val status: ProductStatus? = null,
    val images: List<String>? = null
)

/**
 * Ответ с деталями товара (включает информацию о пользователе и категории)
 */
@Serializable
data class ProductResponse(
    val product: Product,
    val user: UserPublicInfo,
    val category: CategoryInfo,
    val isFavorite: Boolean = false
)

/**
 * Элемент списка товаров (flat структура для списка)
 */
@Serializable
data class ProductListItem(
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
 * Ответ со списком товаров (с пагинацией)
 */
@Serializable
data class ProductListResponse(
    val products: List<ProductListItem>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)
