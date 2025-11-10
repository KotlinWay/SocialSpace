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
        val result = apiClient.getAllCategories()
        return when (result) {
            is Result.Success -> Result.Success(result.data.map { it.toDomain() })
            is Result.Error -> result
        }
    }

    override suspend fun getProductCategories(): kotlin.Result<List<Category>> {
        val result = apiClient.getProductCategories()
        return when (result) {
            is Result.Success -> Result.Success(result.data.map { it.toDomain() })
            is Result.Error -> result
        }
    }

    override suspend fun getServiceCategories(): kotlin.Result<List<Category>> {
        val result = apiClient.getServiceCategories()
        return when (result) {
            is Result.Success -> Result.Success(result.data.map { it.toDomain() })
            is Result.Error -> result
        }
    }
}
