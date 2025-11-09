package info.javaway.sc.shared.domain.repository

import info.javaway.sc.shared.domain.models.Category
import info.javaway.sc.shared.domain.models.Result

/**
 * Репозиторий для работы с категориями
 */
interface CategoryRepository {
    suspend fun getAllCategories(): Result<List<Category>>
    suspend fun getProductCategories(): Result<List<Category>>
    suspend fun getServiceCategories(): Result<List<Category>>
}
