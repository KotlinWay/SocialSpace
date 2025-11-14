package info.javaway.sc.shared.presentation.screens.products

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import info.javaway.sc.shared.data.local.TokenManager
import info.javaway.sc.shared.domain.models.Product
import info.javaway.sc.shared.domain.repository.ProductRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * ViewModel для детального экрана товара (MVI подход)
 */
class ProductDetailViewModel(
    private val productRepository: ProductRepository,
    private val tokenManager: TokenManager,
    private val productId: Long
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Единое состояние экрана
    var state by mutableStateOf<ProductDetailState>(ProductDetailState.Loading)
        private set

    init {
        loadProduct()
    }

    /**
     * Загрузка товара с сервера
     */
    fun loadProduct() {
        state = ProductDetailState.Loading

        viewModelScope.launch {
            Napier.d("Loading product with id: $productId", tag = "ProductDetailViewModel")

            productRepository.getProduct(productId)
                .onSuccess { product ->
                    Napier.d("Product loaded successfully: ${product.title}", tag = "ProductDetailViewModel")

                    // Проверяем, является ли текущий пользователь владельцем товара
                    val currentUserId = tokenManager.getToken()?.let {
                        // TODO: извлечь userId из JWT токена (можно парсить или добавить в TokenManager)
                        // Временно считаем что не владелец если не можем определить
                        null
                    }

                    val isOwner = currentUserId != null && currentUserId == product.user.id

                    state = ProductDetailState.Success(
                        product = product,
                        isFavorite = product.isFavorite, // TODO: проверить isFavorite через API
                        isOwner = isOwner
                    )
                }
                .onFailure { error ->
                    Napier.e("Failed to load product: ${error.message}", error, tag = "ProductDetailViewModel")
                    state = ProductDetailState.Error(
                        message = error.message ?: "Не удалось загрузить товар"
                    )
                }
        }
    }

    /**
     * Переключить состояние избранного (добавить/удалить)
     */
    fun toggleFavorite() {
        val currentState = state as? ProductDetailState.Success ?: return

        // Оптимистичное обновление UI
        state = currentState.copy(
            isFavorite = !currentState.isFavorite,
            isTogglingFavorite = true
        )

        viewModelScope.launch {
            val result = if (currentState.isFavorite) {
                // Было в избранном -> удаляем
                productRepository.removeFromFavorites(productId)
            } else {
                // Не было в избранном -> добавляем
                productRepository.addToFavorites(productId)
            }

            result
                .onSuccess {
                    Napier.d("Favorite toggled successfully", tag = "ProductDetailViewModel")
                    // Оставляем оптимистичное обновление
                    state = (state as ProductDetailState.Success).copy(
                        isTogglingFavorite = false
                    )
                }
                .onFailure { error ->
                    Napier.e("Failed to toggle favorite: ${error.message}", error, tag = "ProductDetailViewModel")
                    // Откатываем изменение
                    state = currentState.copy(
                        isTogglingFavorite = false
                    )
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
 * Состояния экрана деталей товара
 */
sealed interface ProductDetailState {
    /**
     * Загрузка товара
     */
    data object Loading : ProductDetailState

    /**
     * Товар успешно загружен
     */
    data class Success(
        val product: Product,
        val isFavorite: Boolean,
        val isOwner: Boolean,
        val isTogglingFavorite: Boolean = false
    ) : ProductDetailState

    /**
     * Ошибка загрузки товара
     */
    data class Error(val message: String) : ProductDetailState
}
