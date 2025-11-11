package info.javaway.sc.api.models

import kotlinx.serialization.Serializable

/**
 * Модель категории
 */
@Serializable
data class Category(
    val id: Long,
    val name: String,
    val icon: String? = null,
    val type: CategoryType
)

/**
 * Тип категории
 */
@Serializable
enum class CategoryType {
    PRODUCT,
    SERVICE
}

/**
 * Краткая информация о категории (используется в списках товаров/услуг)
 */
@Serializable
data class CategoryInfo(
    val id: Long,
    val name: String,
    val icon: String?
)

/**
 * Расширение для преобразования Category в CategoryInfo
 */
fun Category.toInfo(): CategoryInfo {
    return CategoryInfo(
        id = id,
        name = name,
        icon = icon
    )
}
