package info.javaway.sc.shared.domain.models

import kotlinx.serialization.Serializable

/**
 * Категория товаров/услуг
 */
@Serializable
data class Category(
    val id: Long,
    val name: String,
    val icon: String? = null,
    val type: CategoryType
)

@Serializable
enum class CategoryType {
    PRODUCT,
    SERVICE
}

/**
 * Краткая информация о категории
 */
@Serializable
data class CategoryInfo(
    val id: Long,
    val name: String,
    val icon: String?
)
