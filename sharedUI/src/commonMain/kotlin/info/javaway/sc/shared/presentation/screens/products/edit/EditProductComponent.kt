package info.javaway.sc.shared.presentation.screens.products.edit

import com.arkivanov.decompose.ComponentContext
import info.javaway.sc.api.models.ProductCondition
import info.javaway.sc.api.models.UpdateProductRequest
import info.javaway.sc.shared.data.api.ApiClient
import info.javaway.sc.shared.domain.models.Category
import info.javaway.sc.shared.domain.repository.CategoryRepository
import info.javaway.sc.shared.domain.repository.ProductRepository
import info.javaway.sc.shared.presentation.core.BaseComponent
import info.javaway.sc.shared.utils.SelectedImage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface EditProductComponent {
    val state: StateFlow<EditProductState>
    val formState: StateFlow<EditProductFormState>
    val categories: StateFlow<List<Category>>

    fun updateTitle(title: String)
    fun updateDescription(description: String)
    fun updatePrice(price: String)
    fun selectCategory(category: Category)
    fun selectCondition(condition: ProductCondition)
    fun selectImages(images: List<SelectedImage>)
    fun removeExistingImage(index: Int)
    fun removeNewImage(index: Int)
    fun updateProduct()
    fun retry()
}

class DefaultEditProductComponent(
    componentContext: ComponentContext,
    private val productId: Long,
    private val productRepository: ProductRepository,
    private val apiClient: ApiClient,
    private val categoryRepository: CategoryRepository
) : BaseComponent(componentContext), EditProductComponent {

    private val _state = MutableStateFlow<EditProductState>(EditProductState.Loading)
    override val state: StateFlow<EditProductState> = _state.asStateFlow()

    private val _formState = MutableStateFlow(EditProductFormState())
    override val formState: StateFlow<EditProductFormState> = _formState.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    override val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    init {
        loadProduct()
    }

    override fun updateTitle(title: String) {
        _formState.value = _formState.value.copy(title = title, titleError = null)
    }

    override fun updateDescription(description: String) {
        _formState.value = _formState.value.copy(description = description, descriptionError = null)
    }

    override fun updatePrice(price: String) {
        _formState.value = _formState.value.copy(price = price, priceError = null)
    }

    override fun selectCategory(category: Category) {
        _formState.value = _formState.value.copy(category = category, categoryError = null)
    }

    override fun selectCondition(condition: ProductCondition) {
        _formState.value = _formState.value.copy(condition = condition)
    }

    override fun selectImages(images: List<SelectedImage>) {
        val form = _formState.value
        val totalImages = form.existingImages.size - form.removedExistingImageIndices.size + form.newImages.size + images.size

        if (totalImages > 5) {
            _formState.value = _formState.value.copy(imagesError = "Можно загрузить максимум 5 изображений")
            return
        }

        val updatedNewImages = form.newImages.toMutableList().apply { addAll(images) }
        _formState.value = _formState.value.copy(newImages = updatedNewImages, imagesError = null)
        Napier.d("EditProductComponent: Added ${images.size} new images, total new: ${updatedNewImages.size}")
    }

    override fun removeExistingImage(index: Int) {
        val form = _formState.value
        if (index in form.existingImages.indices) {
            val updatedRemovedIndices = form.removedExistingImageIndices.toMutableSet().apply { add(index) }
            _formState.value = _formState.value.copy(
                removedExistingImageIndices = updatedRemovedIndices,
                imagesError = null
            )
            Napier.d("EditProductComponent: Marked existing image $index for removal")
        }
    }

    override fun removeNewImage(index: Int) {
        val form = _formState.value
        if (index in form.newImages.indices) {
            val updatedNewImages = form.newImages.toMutableList().apply { removeAt(index) }
            _formState.value = _formState.value.copy(newImages = updatedNewImages, imagesError = null)
            Napier.d("EditProductComponent: Removed new image $index, remaining: ${updatedNewImages.size}")
        }
    }

    override fun updateProduct() {
        if (!validateForm()) {
            return
        }

        componentScope.launch {
            _state.value = EditProductState.Updating
            try {
                val form = _formState.value

                val newImageUrls = if (form.newImages.isNotEmpty()) {
                    Napier.d("EditProductComponent: Uploading ${form.newImages.size} new images...")
                    val imagesToUpload = form.newImages.map { Triple(it.bytes, it.name, it.mimeType) }

                    val uploadResult = apiClient.uploadImages(imagesToUpload, "product")
                    if (uploadResult.isSuccess) {
                        uploadResult.getOrNull()!!
                    } else {
                        val exception = uploadResult.exceptionOrNull()!!
                        _state.value = EditProductState.Error(
                            exception.message ?: "Не удалось загрузить изображения"
                        )
                        Napier.e("EditProductComponent: Failed to upload images", exception)
                        return@launch
                    }
                } else {
                    emptyList()
                }

                val remainingExistingImages = form.existingImages.filterIndexed { index, _ ->
                    index !in form.removedExistingImageIndices
                }
                val finalImages = remainingExistingImages + newImageUrls
                Napier.d(
                    "EditProductComponent: Final images ${finalImages.size} (${remainingExistingImages.size} existing + ${newImageUrls.size} new)"
                )

                val request = UpdateProductRequest(
                    title = form.title,
                    description = form.description,
                    price = form.price.toDouble(),
                    categoryId = form.category!!.id,
                    condition = form.condition,
                    images = finalImages
                )

                val updateResult = apiClient.updateProduct(productId, request)
                if (updateResult.isSuccess) {
                    val response = updateResult.getOrNull()!!
                    _state.value = EditProductState.Success(response.product.id)
                    Napier.d("EditProductComponent: Product updated ${response.product.id}")
                } else {
                    val exception = updateResult.exceptionOrNull()!!
                    _state.value = EditProductState.Error(
                        exception.message ?: "Не удалось обновить товар"
                    )
                    Napier.e("EditProductComponent: Failed to update product", exception)
                }
            } catch (e: Exception) {
                _state.value = EditProductState.Error(e.message ?: "Неизвестная ошибка")
                Napier.e("EditProductComponent: Exception while updating product", e)
            }
        }
    }

    override fun retry() {
        loadProduct()
    }

    private fun loadProduct() {
        componentScope.launch {
            _state.value = EditProductState.Loading
            try {
                val categoriesResult = categoryRepository.getProductCategories()
                val productResult = productRepository.getProduct(productId)

                if (categoriesResult.isSuccess && productResult.isSuccess) {
                    val categories = categoriesResult.getOrNull()!!
                    val product = productResult.getOrNull()!!

                    _categories.value = categories

                    val category = categories.find { it.id == product.category.id }
                    _formState.value = EditProductFormState(
                        title = product.title,
                        description = product.description,
                        price = product.price.toString(),
                        category = category,
                        condition = ProductCondition.valueOf(product.condition.name),
                        existingImages = product.images
                    )

                    _state.value = EditProductState.Form
                    Napier.d("EditProductComponent: Loaded product ${product.id} and ${categories.size} categories")
                } else {
                    val error = categoriesResult.exceptionOrNull() ?: productResult.exceptionOrNull()
                    _state.value = EditProductState.Error(error?.message ?: "Не удалось загрузить данные")
                    Napier.e("EditProductComponent: Failed to load data", error)
                }
            } catch (e: Exception) {
                _state.value = EditProductState.Error(e.message ?: "Неизвестная ошибка")
                Napier.e("EditProductComponent: Exception while loading product", e)
            }
        }
    }

    private fun validateForm(): Boolean {
        val form = _formState.value
        var hasErrors = false

        if (form.title.isBlank()) {
            _formState.value = _formState.value.copy(titleError = "Введите название")
            hasErrors = true
        } else if (form.title.length > 200) {
            _formState.value = _formState.value.copy(titleError = "Название не должно превышать 200 символов")
            hasErrors = true
        }

        if (form.description.isBlank()) {
            _formState.value = _formState.value.copy(descriptionError = "Введите описание")
            hasErrors = true
        }

        val priceValue = form.price.toDoubleOrNull()
        if (priceValue == null) {
            _formState.value = _formState.value.copy(priceError = "Введите корректную цену")
            hasErrors = true
        } else if (priceValue < 0) {
            _formState.value = _formState.value.copy(priceError = "Цена не может быть отрицательной")
            hasErrors = true
        }

        if (form.category == null) {
            _formState.value = _formState.value.copy(categoryError = "Выберите категорию")
            hasErrors = true
        }

        val totalImages = form.existingImages.size - form.removedExistingImageIndices.size + form.newImages.size
        if (totalImages == 0) {
            _formState.value = _formState.value.copy(imagesError = "Добавьте хотя бы одно изображение")
            hasErrors = true
        } else if (totalImages > 5) {
            _formState.value = _formState.value.copy(imagesError = "Можно загрузить максимум 5 изображений")
            hasErrors = true
        }

        return !hasErrors
    }
}

/**
 * Состояние экрана редактирования товара
 */
sealed interface EditProductState {
    /** Загрузка товара и категорий */
    data object Loading : EditProductState

    /** Форма готова к редактированию */
    data object Form : EditProductState

    /** Обновление товара в процессе */
    data object Updating : EditProductState

    /** Товар успешно обновлен */
    data class Success(val productId: Long) : EditProductState

    /** Ошибка */
    data class Error(val message: String) : EditProductState
}

/**
 * Состояние формы редактирования товара
 */
data class EditProductFormState(
    val title: String = "",
    val titleError: String? = null,

    val description: String = "",
    val descriptionError: String? = null,

    val price: String = "",
    val priceError: String? = null,

    val category: Category? = null,
    val categoryError: String? = null,

    val condition: ProductCondition = ProductCondition.USED,

    // Существующие изображения (URL с сервера)
    val existingImages: List<String> = emptyList(),

    // Новые выбранные изображения (локальные файлы)
    val newImages: List<SelectedImage> = emptyList(),

    // Индексы удалённых существующих изображений
    val removedExistingImageIndices: Set<Int> = emptySet(),

    val imagesError: String? = null
)
