package info.javaway.sc.shared.data.repository

import info.javaway.sc.shared.data.api.ApiClient
import info.javaway.sc.shared.domain.models.Category
import info.javaway.sc.shared.domain.models.Result
import info.javaway.sc.shared.domain.repository.CategoryRepository

/**
 * Реализация репозитория категорий
 */
class CategoryRepositoryImpl(
    private val apiClient: ApiClient
) : CategoryRepository {

    override suspend fun getAllCategories(): Result<List<Category>> {
        return apiClient.getAllCategories()
    }

    override suspend fun getProductCategories(): Result<List<Category>> {
        return apiClient.getProductCategories()
    }

    override suspend fun getServiceCategories(): Result<List<Category>> {
        return apiClient.getServiceCategories()
    }
}
