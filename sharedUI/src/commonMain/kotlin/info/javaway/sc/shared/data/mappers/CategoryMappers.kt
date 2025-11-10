package info.javaway.sc.shared.data.mappers

import info.javaway.sc.api.models.Category as ApiCategory
import info.javaway.sc.api.models.CategoryInfo as ApiCategoryInfo
import info.javaway.sc.api.models.CategoryType as ApiCategoryType
import info.javaway.sc.shared.domain.models.Category
import info.javaway.sc.shared.domain.models.CategoryInfo
import info.javaway.sc.shared.domain.models.CategoryType

/**
 * Маппер: Category (API DTO) → Category (Domain)
 */
fun ApiCategory.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        icon = icon,
        type = type.toDomain()
    )
}

/**
 * Маппер: CategoryInfo (API DTO) → CategoryInfo (Domain)
 */
fun ApiCategoryInfo.toDomain(): CategoryInfo {
    return CategoryInfo(
        id = id,
        name = name,
        icon = icon
    )
}

/**
 * Маппер: CategoryType (API) → CategoryType (Domain)
 */
fun ApiCategoryType.toDomain(): CategoryType {
    return when (this) {
        ApiCategoryType.PRODUCT -> CategoryType.PRODUCT
        ApiCategoryType.SERVICE -> CategoryType.SERVICE
    }
}
