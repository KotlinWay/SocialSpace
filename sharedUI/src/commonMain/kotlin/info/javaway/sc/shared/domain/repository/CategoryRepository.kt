package info.javaway.sc.shared.domain.repository

import info.javaway.sc.api.models.Category

/**
 * Репозиторий для работы с категориями
 */
interface CategoryRepository {
    suspend fun getAllCategories(): kotlin.Result<List<Category>>
    suspend fun getProductCategories(): kotlin.Result<List<Category>>
    suspend fun getServiceCategories(): kotlin.Result<List<Category>>
}
