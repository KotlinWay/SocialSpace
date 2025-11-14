package info.javaway.sc.shared.presentation.screens.profile.products

import com.arkivanov.decompose.ComponentContext
import info.javaway.sc.shared.domain.models.Product
import info.javaway.sc.shared.domain.repository.ProductRepository
import info.javaway.sc.shared.presentation.core.BaseComponent
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface MyProductsComponent {
    val state: StateFlow<MyProductsState>

    fun loadMyProducts()
    fun deleteProduct(productId: Long)
}

class DefaultMyProductsComponent(
    componentContext: ComponentContext,
    private val productRepository: ProductRepository
) : BaseComponent(componentContext), MyProductsComponent {

    private val _state = MutableStateFlow<MyProductsState>(MyProductsState.Loading)
    override val state: StateFlow<MyProductsState> = _state.asStateFlow()

    init {
        loadMyProducts()
    }

    override fun loadMyProducts() {
        _state.value = MyProductsState.Loading

        componentScope.launch {
            Napier.d("Loading my products", tag = "MyProductsComponent")

            productRepository.getMyProducts()
                .onSuccess { products ->
                    Napier.d("My products loaded: ${products.size}", tag = "MyProductsComponent")
                    _state.value = if (products.isEmpty()) {
                        MyProductsState.Empty
                    } else {
                        MyProductsState.Success(products)
                    }
                }
                .onFailure { error ->
                    Napier.e("Failed to load my products: ${error.message}", tag = "MyProductsComponent")
                    _state.value = MyProductsState.Error(
                        message = error.message ?: "Не удалось загрузить товары"
                    )
                }
        }
    }

    override fun deleteProduct(productId: Long) {
        val currentState = _state.value
        if (currentState !is MyProductsState.Success) return

        val updatedProducts = currentState.products.filter { it.id != productId }
        _state.value = if (updatedProducts.isEmpty()) {
            MyProductsState.Empty
        } else {
            MyProductsState.Success(updatedProducts)
        }

        componentScope.launch {
            Napier.d("Deleting product $productId", tag = "MyProductsComponent")

            productRepository.deleteProduct(productId)
                .onFailure { error ->
                    Napier.e("Failed to delete product: ${error.message}", tag = "MyProductsComponent")
                    _state.value = currentState
                }
        }
    }
}

/**
 * Состояния экрана "Мои товары"
 */
sealed interface MyProductsState {
    /**
     * Загрузка данных
     */
    data object Loading : MyProductsState

    /**
     * Товары загружены успешно
     */
    data class Success(val products: List<Product>) : MyProductsState

    /**
     * Список товаров пуст
     */
    data object Empty : MyProductsState

    /**
     * Ошибка загрузки
     */
    data class Error(val message: String) : MyProductsState
}
