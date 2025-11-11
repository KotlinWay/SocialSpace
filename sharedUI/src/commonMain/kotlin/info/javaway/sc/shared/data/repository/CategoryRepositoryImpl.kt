package info.javaway.sc.shared.data.repository

import info.javaway.sc.shared.data.api.ApiClient
import info.javaway.sc.shared.data.mappers.toDomain
import info.javaway.sc.shared.domain.models.Category
import info.javaway.sc.shared.domain.repository.CategoryRepository

/**
 * Реализация репозитория категорий
 * Преобразует DTO в Domain модели
 */
class CategoryRepositoryImpl(
    private val apiClient: ApiClient
) : CategoryRepository {

    override suspend fun getAllCategories(): kotlin.Result<List<Category>> {
        return apiClient.getAllCategories().fold(
            onSuccess = { categories ->
                kotlin.Result.success(categories.map { it.toDomain() })
            },
            onFailure = { exception ->
                kotlin.Result.failure(exception)
            }
        )
    }

    override suspend fun getProductCategories(): kotlin.Result<List<Category>> {
        return apiClient.getProductCategories().fold(
            onSuccess = { categories ->
                kotlin.Result.success(categories.map { it.toDomain() })
            },
            onFailure = { exception ->
                kotlin.Result.failure(exception)
            }
        )
    }

    override suspend fun getServiceCategories(): kotlin.Result<List<Category>> {
        return apiClient.getServiceCategories().fold(
            onSuccess = { categories ->
                kotlin.Result.success(categories.map { it.toDomain() })
            },
            onFailure = { exception ->
                kotlin.Result.failure(exception)
            }
        )
    }
}
