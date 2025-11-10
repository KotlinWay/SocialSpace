package info.javaway.sc.shared.presentation.screens.products

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import info.javaway.sc.shared.domain.models.ProductListResponse
import info.javaway.sc.shared.domain.models.ProductResponse
import info.javaway.sc.shared.domain.models.Result
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

    var state by mutableStateOf<ProductListState>(ProductListState.Loading)
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
            when (val result = productRepository.getProducts(page = currentPage, pageSize = pageSize)) {
                is Result.Success -> {
                    if (result.data.products.isEmpty()) {
                        state = ProductListState.Empty
                    } else {
                        hasMorePages = currentPage < result.data.totalPages
                        state = ProductListState.Success(
                            products = result.data.products,
                            hasMore = hasMorePages
                        )
                    }
                }
                is Result.Error -> {
                    e("ProductListViewModel", "Ошибка загрузки товаров: ${result.message}")
                    state = ProductListState.Error(
                        message = result.message ?: "Ошибка загрузки товаров"
                    )
                }
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
            when (val result = productRepository.getProducts(page = currentPage, pageSize = pageSize)) {
                is Result.Success -> {
                    if (result.data.products.isEmpty()) {
                        state = ProductListState.Empty
                    } else {
                        hasMorePages = currentPage < result.data.totalPages
                        state = ProductListState.Success(
                            products = result.data.products,
                            hasMore = hasMorePages
                        )
                    }
                }
                is Result.Error -> {
                    e("ProductListViewModel", "Ошибка обновления товаров: ${result.message}")
                    state = ProductListState.Error(
                        message = result.message ?: "Ошибка обновления товаров"
                    )
                }
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
            when (val result = productRepository.getProducts(page = nextPage, pageSize = pageSize)) {
                is Result.Success -> {
                    currentPage = nextPage
                    hasMorePages = currentPage < result.data.totalPages

                    // Добавляем новые товары к существующим
                    state = ProductListState.Success(
                        products = currentState.products + result.data.products,
                        hasMore = hasMorePages,
                        isLoadingMore = false
                    )
                }
                is Result.Error -> {
                    e("ProductListViewModel", "Ошибка загрузки следующей страницы: ${result.message}")
                    // При ошибке загрузки страницы возвращаем предыдущее состояние
                    state = currentState.copy(isLoadingMore = false)
                }
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
