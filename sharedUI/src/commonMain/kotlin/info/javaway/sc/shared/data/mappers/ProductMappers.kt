package info.javaway.sc.shared.data.mappers

import info.javaway.sc.api.models.ProductListItem as ApiProductListItem
import info.javaway.sc.api.models.ProductResponse as ApiProductResponse
import info.javaway.sc.api.models.ProductCondition as ApiProductCondition
import info.javaway.sc.api.models.ProductStatus as ApiProductStatus
import info.javaway.sc.shared.domain.models.Product
import info.javaway.sc.shared.domain.models.ProductCondition
import info.javaway.sc.shared.domain.models.ProductStatus

/**
 * Маппер: ProductListItem (API DTO) → Product (Domain)
 */
fun ApiProductListItem.toDomain(): Product {
    return Product(
        id = id,
        spaceId = spaceId,
        title = title,
        description = description,
        price = price,
        condition = condition.toDomain(),
        images = images,
        status = status.toDomain(),
        views = views,
        createdAt = createdAt,
        updatedAt = updatedAt,
        user = user.toDomain(),
        category = category.toDomain(),
        isFavorite = isFavorite
    )
}

/**
 * Маппер: ProductResponse (API DTO) → Product (Domain)
 */
fun ApiProductResponse.toDomain(): Product {
    return Product(
        id = product.id,
        spaceId = product.spaceId,
        title = product.title,
        description = product.description,
        price = product.price,
        condition = product.condition.toDomain(),
        images = product.images,
        status = product.status.toDomain(),
        views = product.views,
        createdAt = product.createdAt,
        updatedAt = product.updatedAt,
        user = user.toDomain(),
        category = category.toDomain(),
        isFavorite = isFavorite
    )
}

/**
 * Маппер: ProductCondition (API) → ProductCondition (Domain)
 */
fun ApiProductCondition.toDomain(): ProductCondition {
    return when (this) {
        ApiProductCondition.NEW -> ProductCondition.NEW
        ApiProductCondition.USED -> ProductCondition.USED
    }
}

/**
 * Маппер: ProductStatus (API) → ProductStatus (Domain)
 */
fun ApiProductStatus.toDomain(): ProductStatus {
    return when (this) {
        ApiProductStatus.ACTIVE -> ProductStatus.ACTIVE
        ApiProductStatus.SOLD -> ProductStatus.SOLD
        ApiProductStatus.ARCHIVED -> ProductStatus.ARCHIVED
    }
}
