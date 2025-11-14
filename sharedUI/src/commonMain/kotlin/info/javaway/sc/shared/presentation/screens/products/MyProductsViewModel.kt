package info.javaway.sc.shared.presentation.screens.products

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import info.javaway.sc.shared.domain.models.Product
import info.javaway.sc.shared.domain.repository.ProductRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана "Мои товары" (MVI подход)
 */
class MyProductsViewModel(
    private val productRepository: ProductRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Единое состояние экрана
    var state by mutableStateOf<MyProductsState>(MyProductsState.Loading)
        private set

    init {
        loadMyProducts()
    }

    /**
     * Загрузка списка своих товаров
     */
    fun loadMyProducts() {
        state = MyProductsState.Loading

        viewModelScope.launch {
            Napier.d("Loading my products", tag = "MyProductsViewModel")

            productRepository.getMyProducts()
                .onSuccess { products ->
                    Napier.d("My products loaded successfully: ${products.size} items", tag = "MyProductsViewModel")

                    state = if (products.isEmpty()) {
                        MyProductsState.Empty
                    } else {
                        MyProductsState.Success(products)
                    }
                }
                .onFailure { error ->
                    Napier.e("Failed to load my products: ${error.message}", tag = "MyProductsViewModel")
                    state = MyProductsState.Error(
                        message = error.message ?: "Не удалось загрузить товары"
                    )
                }
        }
    }

    /**
     * Удаление товара
     */
    fun deleteProduct(productId: Long) {
        // Сохраняем текущий список для возможного отката
        val currentState = state
        if (currentState !is MyProductsState.Success) return

        // Оптимистичное обновление UI - удаляем товар из списка сразу
        val updatedProducts = currentState.products.filter { it.id != productId }
        state = if (updatedProducts.isEmpty()) {
            MyProductsState.Empty
        } else {
            MyProductsState.Success(updatedProducts)
        }

        viewModelScope.launch {
            Napier.d("Deleting product with id: $productId", tag = "MyProductsViewModel")

            productRepository.deleteProduct(productId)
                .onSuccess {
                    Napier.d("Product deleted successfully", tag = "MyProductsViewModel")
                    // UI уже обновлен оптимистично, ничего не делаем
                }
                .onFailure { error ->
                    Napier.e("Failed to delete product: ${error.message}", tag = "MyProductsViewModel")

                    // Откатываем изменения при ошибке
                    state = currentState

                    // TODO: Показать snackbar с ошибкой
                    // Пока просто логируем
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
