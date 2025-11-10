package info.javaway.sc.shared.presentation.screens.products

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import info.javaway.sc.api.models.ProductResponse
import info.javaway.sc.shared.domain.repository.ProductRepository
import io.github.aakira.napier.Napier.e
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * ViewModel для списка товаров
 */
class ProductListViewModel(
    private val productRepository: ProductRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    var state by mutableStateOf<ProductListState>(ProductListState.Empty)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    private var currentPage = 1
    private val pageSize = 20
    private var hasMorePages = true

    init {
        loadProducts()
    }

    /**
     * Загрузка товаров (первая страница)
     */
    fun loadProducts() {
        if (state is ProductListState.Loading) {
            // Уже загружается
            return
        }

        state = ProductListState.Loading
        currentPage = 1
        hasMorePages = true

        viewModelScope.launch {
            val result = productRepository.getProducts(page = currentPage, pageSize = pageSize)
            result.onSuccess { productList ->
                if (productList.products.isEmpty()) {
                    state = ProductListState.Empty
                } else {
                    hasMorePages = currentPage < productList.totalPages
                    state = ProductListState.Success(
                        products = productList.products,
                        hasMore = hasMorePages
                    )
                }
            }.onFailure { exception ->
                e(exception, tag = "ProductListViewModel") { "Ошибка загрузки товаров" }
                state = ProductListState.Error(
                    message = exception.message ?: "Ошибка загрузки товаров"
                )
            }
        }
    }

    /**
     * Обновление списка (pull-to-refresh)
     */
    fun refreshProducts() {
        isRefreshing = true
        currentPage = 1
        hasMorePages = true

        viewModelScope.launch {
            val result = productRepository.getProducts(page = currentPage, pageSize = pageSize)
            result.onSuccess { productList ->
                if (productList.products.isEmpty()) {
                    state = ProductListState.Empty
                } else {
                    hasMorePages = currentPage < productList.totalPages
                    state = ProductListState.Success(
                        products = productList.products,
                        hasMore = hasMorePages
                    )
                }
            }.onFailure { exception ->
                e(exception, tag = "ProductListViewModel") { "Ошибка обновления товаров" }
                state = ProductListState.Error(
                    message = exception.message ?: "Ошибка обновления товаров"
                )
            }
            isRefreshing = false
        }
    }

    /**
     * Загрузка следующей страницы (пагинация)
     */
    fun loadNextPage() {
        val currentState = state
        if (currentState !is ProductListState.Success || !hasMorePages || currentState.isLoadingMore) {
            return
        }

        // Помечаем, что загружаем следующую страницу
        state = currentState.copy(isLoadingMore = true)

        viewModelScope.launch {
            val nextPage = currentPage + 1
            val result = productRepository.getProducts(page = nextPage, pageSize = pageSize)
            result.onSuccess { productList ->
                currentPage = nextPage
                hasMorePages = currentPage < productList.totalPages

                // Добавляем новые товары к существующим
                state = ProductListState.Success(
                    products = currentState.products + productList.products,
                    hasMore = hasMorePages,
                    isLoadingMore = false
                )
            }.onFailure { exception ->
                e(exception, tag = "ProductListViewModel") { "Ошибка загрузки следующей страницы" }
                // При ошибке загрузки страницы возвращаем предыдущее состояние
                state = currentState.copy(isLoadingMore = false)
            }
        }
    }

    /**
     * Очистка ресурсов
     */
    fun onCleared() {
        viewModelScope.cancel()
    }
}

/**
 * Состояния списка товаров
 */
sealed interface ProductListState {
    /**
     * Загрузка (первая загрузка)
     */
    data object Loading : ProductListState

    /**
     * Успешная загрузка
     */
    data class Success(
        val products: List<ProductResponse>,
        val hasMore: Boolean,
        val isLoadingMore: Boolean = false
    ) : ProductListState

    /**
     * Ошибка загрузки
     */
    data class Error(val message: String) : ProductListState

    /**
     * Список пуст
     */
    data object Empty : ProductListState
}
