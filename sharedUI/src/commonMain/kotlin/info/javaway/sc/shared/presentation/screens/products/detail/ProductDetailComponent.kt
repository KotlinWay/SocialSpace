package info.javaway.sc.shared.presentation.screens.products.detail

import com.arkivanov.decompose.ComponentContext
import info.javaway.sc.shared.domain.models.Product
import info.javaway.sc.shared.domain.repository.AuthRepository
import info.javaway.sc.shared.domain.repository.ProductRepository
import info.javaway.sc.shared.presentation.core.BaseComponent
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface ProductDetailComponent {
    val state: StateFlow<ProductDetailState>

    fun loadProduct()
    fun toggleFavorite()
}

class DefaultProductDetailComponent(
    componentContext: ComponentContext,
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository,
    private val productId: Long
) : BaseComponent(componentContext), ProductDetailComponent {

    private val _state = MutableStateFlow<ProductDetailState>(ProductDetailState.Loading)
    override val state: StateFlow<ProductDetailState> = _state.asStateFlow()

    init {
        loadProduct()
    }

    override fun loadProduct() {
        _state.value = ProductDetailState.Loading

        componentScope.launch {
            Napier.d("Loading product with id: $productId", tag = "ProductDetailComponent")

            productRepository.getProduct(productId)
                .onSuccess { product ->
                    val currentUserId = authRepository.getUserId()
                    val isOwner = currentUserId != null && currentUserId == product.user.id

                    _state.value = ProductDetailState.Success(
                        product = product,
                        isFavorite = product.isFavorite,
                        isOwner = isOwner
                    )
                }
                .onFailure { error ->
                    Napier.e("Failed to load product: ${error.message}", error, tag = "ProductDetailComponent")
                    _state.value = ProductDetailState.Error(
                        message = error.message ?: "Не удалось загрузить товар"
                    )
                }
        }
    }

    override fun toggleFavorite() {
        val currentState = _state.value as? ProductDetailState.Success ?: return

        _state.value = currentState.copy(
            isFavorite = !currentState.isFavorite,
            isTogglingFavorite = true
        )

        componentScope.launch {
            val result = if (currentState.isFavorite) {
                productRepository.removeFromFavorites(productId)
            } else {
                productRepository.addToFavorites(productId)
            }

            result
                .onSuccess {
                    _state.value = (_state.value as ProductDetailState.Success).copy(
                        isTogglingFavorite = false
                    )
                }
                .onFailure { error ->
                    Napier.e("Failed to toggle favorite: ${error.message}", error, tag = "ProductDetailComponent")
                    _state.value = currentState.copy(isTogglingFavorite = false)
                }
        }
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
