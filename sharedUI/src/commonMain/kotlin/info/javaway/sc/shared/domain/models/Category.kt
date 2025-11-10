package info.javaway.sc.shared.domain.models

/**
 * Domain модель категории
 */
data class Category(
    val id: Long,
    val name: String,
    val icon: String?,
    val type: CategoryType
)

/**
 * Тип категории
 */
enum class CategoryType {
    PRODUCT,
    SERVICE
}

/**
 * Краткая информация о категории
 * Используется в списках товаров/услуг
 */
data class CategoryInfo(
    val id: Long,
    val name: String,
    val icon: String?
)
