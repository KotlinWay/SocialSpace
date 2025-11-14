package info.javaway.sc.shared.presentation.screens.products.list

import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.arkivanov.decompose.ComponentContext
import info.javaway.sc.shared.domain.models.Product
import info.javaway.sc.shared.domain.models.ProductCondition
import info.javaway.sc.shared.domain.models.ProductStatus
import info.javaway.sc.shared.domain.repository.ProductRepository
import info.javaway.sc.shared.presentation.core.BaseComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest

interface ProductListComponent {
    val filters: StateFlow<ProductFiltersState>
    val productsFlow: Flow<PagingData<Product>>

    fun updateFilters(
        categoryId: Long? = null,
        status: ProductStatus? = null,
        condition: ProductCondition? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        search: String? = null
    )

    fun clearFilters()
}

class DefaultProductListComponent(
    componentContext: ComponentContext,
    private val productRepository: ProductRepository
) : BaseComponent(componentContext), ProductListComponent {

    private val _filters = MutableStateFlow(ProductFiltersState())
    override val filters: StateFlow<ProductFiltersState> = _filters.asStateFlow()

    override val productsFlow: Flow<PagingData<Product>> = _filters
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
        .cachedIn(componentScope)

    override fun updateFilters(
        categoryId: Long?,
        status: ProductStatus?,
        condition: ProductCondition?,
        minPrice: Double?,
        maxPrice: Double?,
        search: String?
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

    override fun clearFilters() {
        _filters.value = ProductFiltersState()
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
