package info.javaway.sc.shared.presentation.screens.products

import androidx.paging.PagingData
import androidx.paging.cachedIn
import info.javaway.sc.shared.domain.models.Product
import info.javaway.sc.shared.domain.models.ProductCondition
import info.javaway.sc.shared.domain.models.ProductStatus
import info.javaway.sc.shared.domain.repository.ProductRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest

/**
 * ViewModel для списка товаров с Paging 3
 */
class ProductListViewModel(
    private val productRepository: ProductRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Фильтры для товаров
    private val _filters = MutableStateFlow(ProductFiltersState())
    val filters: StateFlow<ProductFiltersState> = _filters.asStateFlow()

    /**
     * Flow с пагинированными товарами
     * При изменении фильтров автоматически создается новый PagingSource
     */
    val productsFlow: Flow<PagingData<Product>> = _filters
        .flatMapLatest { filters ->
            productRepository.getProductsPaged(
                categoryId = filters.categoryId,
                status = filters.status,
                condition = filters.condition,
                minPrice = filters.minPrice,
                maxPrice = filters.maxPrice,
                search = filters.search
            )
        }
        .cachedIn(viewModelScope) // Кэширование в scope

    /**
     * Обновить фильтры
     * Автоматически триггерит новую загрузку через productsFlow
     */
    fun updateFilters(
        categoryId: Long? = null,
        status: ProductStatus? = null,
        condition: ProductCondition? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        search: String? = null
    ) {
        _filters.value = ProductFiltersState(
            categoryId = categoryId,
            status = status,
            condition = condition,
            minPrice = minPrice,
            maxPrice = maxPrice,
            search = search
        )
    }

    /**
     * Сбросить все фильтры
     */
    fun clearFilters() {
        _filters.value = ProductFiltersState()
    }

    /**
     * Очистка ресурсов
     */
    fun onCleared() {
        viewModelScope.cancel()
    }
}

/**
 * Состояние фильтров для товаров
 */
data class ProductFiltersState(
    val categoryId: Long? = null,
    val status: ProductStatus? = null,
    val condition: ProductCondition? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val search: String? = null
)
