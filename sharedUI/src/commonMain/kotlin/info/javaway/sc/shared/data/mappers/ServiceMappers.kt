package info.javaway.sc.shared.data.mappers

import info.javaway.sc.api.models.Service as ApiService
import info.javaway.sc.api.models.ServiceStatus as ApiServiceStatus
import info.javaway.sc.api.models.ServiceResponse as ApiServiceResponse
import info.javaway.sc.shared.domain.models.Service
import info.javaway.sc.shared.domain.models.ServiceStatus

/**
 * Маппер: Service (API DTO) → Service (Domain)
 */
fun ApiService.toDomain(): Service {
    return Service(
        id = id,
        title = title,
        description = description,
        price = price,
        images = images,
        status = status.toDomain(),
        views = views,
        createdAt = createdAt,
        updatedAt = updatedAt,
        user = user.toDomain(),
        category = category.toDomain()
    )
}

/**
 * Маппер: ServiceResponse (API DTO) → Service (Domain)
 */
fun ApiServiceResponse.toDomain(): Service {
    return Service(
        id = service.id,
        title = service.title,
        description = service.description,
        price = service.price,
        images = service.images,
        status = service.status.toDomain(),
        views = service.views,
        createdAt = service.createdAt,
        updatedAt = service.updatedAt,
        user = user.toDomain(),
        category = category.toDomain()
    )
}

/**
 * Маппер: ServiceStatus (API) → ServiceStatus (Domain)
 */
fun ApiServiceStatus.toDomain(): ServiceStatus {
    return when (this) {
        ApiServiceStatus.ACTIVE -> ServiceStatus.ACTIVE
        ApiServiceStatus.INACTIVE -> ServiceStatus.INACTIVE
    }
}
