package info.javaway.sc.shared.presentation.screens.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.javaway.sc.api.models.ProductCondition
import info.javaway.sc.api.models.UpdateProductRequest
import info.javaway.sc.shared.data.api.ApiClient
import info.javaway.sc.shared.domain.models.Category
import info.javaway.sc.shared.domain.repository.CategoryRepository
import info.javaway.sc.shared.domain.repository.ProductRepository
import info.javaway.sc.shared.utils.SelectedImage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для редактирования товара
 */
class EditProductViewModel(
    private val productId: Long,
    private val productRepository: ProductRepository,
    private val apiClient: ApiClient,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow<EditProductState>(EditProductState.Loading)
    val state: StateFlow<EditProductState> = _state.asStateFlow()

    // Состояние формы
    private val _formState = MutableStateFlow(EditProductFormState())
    val formState: StateFlow<EditProductFormState> = _formState.asStateFlow()

    // Список доступных категорий
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    init {
        loadProduct()
    }

    /**
     * Загрузка товара и категорий
     */
    private fun loadProduct() {
        viewModelScope.launch {
            _state.value = EditProductState.Loading
            try {
                // Загружаем категории и товар параллельно
                val categoriesResult = categoryRepository.getProductCategories()
                val productResult = productRepository.getProduct(productId)

                if (categoriesResult.isSuccess && productResult.isSuccess) {
                    val categories = categoriesResult.getOrNull()!!
                    val product = productResult.getOrNull()!!

                    _categories.value = categories

                    // Находим категорию товара
                    val category = categories.find { it.id == product.category.id }

                    // Предзаполняем форму данными товара
                    _formState.value = EditProductFormState(
                        title = product.title,
                        description = product.description,
                        price = product.price.toString(),
                        category = category,
                        condition = info.javaway.sc.api.models.ProductCondition.valueOf(product.condition.name),
                        existingImages = product.images,
                        newImages = emptyList(),
                        removedExistingImageIndices = emptySet()
                    )

                    _state.value = EditProductState.Form
                    Napier.d("EditProductViewModel: Loaded product ${product.id} and ${categories.size} categories")
                } else {
                    val error = categoriesResult.exceptionOrNull() ?: productResult.exceptionOrNull()
                    _state.value = EditProductState.Error(
                        error?.message ?: "Не удалось загрузить данные"
                    )
                    Napier.e("EditProductViewModel: Failed to load data", error)
                }
            } catch (e: Exception) {
                _state.value = EditProductState.Error(e.message ?: "Неизвестная ошибка")
                Napier.e("EditProductViewModel: Exception while loading product", e)
            }
        }
    }

    /**
     * Обновление названия товара
     */
    fun updateTitle(title: String) {
        _formState.value = _formState.value.copy(
            title = title,
            titleError = null
        )
    }

    /**
     * Обновление описания товара
     */
    fun updateDescription(description: String) {
        _formState.value = _formState.value.copy(
            description = description,
            descriptionError = null
        )
    }

    /**
     * Обновление цены товара
     */
    fun updatePrice(price: String) {
        _formState.value = _formState.value.copy(
            price = price,
            priceError = null
        )
    }

    /**
     * Выбор категории товара
     */
    fun selectCategory(category: Category) {
        _formState.value = _formState.value.copy(
            category = category,
            categoryError = null
        )
    }

    /**
     * Выбор состояния товара (NEW/USED)
     */
    fun selectCondition(condition: ProductCondition) {
        _formState.value = _formState.value.copy(condition = condition)
    }

    /**
     * Добавление новых выбранных изображений
     */
    fun selectImages(images: List<SelectedImage>) {
        val form = _formState.value
        val totalImages = form.existingImages.size - form.removedExistingImageIndices.size + form.newImages.size + images.size

        if (totalImages > 5) {
            _formState.value = _formState.value.copy(
                imagesError = "Можно загрузить максимум 5 изображений"
            )
            return
        }

        val updatedNewImages = form.newImages.toMutableList()
        updatedNewImages.addAll(images)
        _formState.value = _formState.value.copy(
            newImages = updatedNewImages,
            imagesError = null
        )
        Napier.d("EditProductViewModel: Added ${images.size} new images, total new: ${updatedNewImages.size}")
    }

    /**
     * Удаление существующего изображения по индексу
     */
    fun removeExistingImage(index: Int) {
        val form = _formState.value
        if (index in form.existingImages.indices) {
            val updatedRemovedIndices = form.removedExistingImageIndices.toMutableSet()
            updatedRemovedIndices.add(index)
            _formState.value = _formState.value.copy(
                removedExistingImageIndices = updatedRemovedIndices,
                imagesError = null
            )
            Napier.d("EditProductViewModel: Marked existing image at index $index for removal")
        }
    }

    /**
     * Удаление нового изображения по индексу
     */
    fun removeNewImage(index: Int) {
        val form = _formState.value
        if (index in form.newImages.indices) {
            val updatedNewImages = form.newImages.toMutableList()
            updatedNewImages.removeAt(index)
            _formState.value = _formState.value.copy(
                newImages = updatedNewImages,
                imagesError = null
            )
            Napier.d("EditProductViewModel: Removed new image at index $index, remaining: ${updatedNewImages.size}")
        }
    }

    /**
     * Обновление товара
     */
    fun updateProduct() {
        // Валидация
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            _state.value = EditProductState.Updating
            try {
                val form = _formState.value

                // 1. Загружаем новые изображения (если есть)
                val newImageUrls = if (form.newImages.isNotEmpty()) {
                    Napier.d("EditProductViewModel: Uploading ${form.newImages.size} new images...")
                    val imagesToUpload = form.newImages.map { image ->
                        Triple(image.bytes, image.name, image.mimeType)
                    }

                    val uploadResult = apiClient.uploadImages(imagesToUpload, "product")
                    if (uploadResult.isSuccess) {
                        uploadResult.getOrNull()!!
                    } else {
                        val exception = uploadResult.exceptionOrNull()!!
                        _state.value = EditProductState.Error(
                            exception.message ?: "Не удалось загрузить изображения"
                        )
                        Napier.e("EditProductViewModel: Failed to upload images", exception)
                        return@launch
                    }
                } else {
                    emptyList()
                }

                // 2. Формируем итоговый список изображений
                // Оставшиеся существующие + новые загруженные
                val remainingExistingImages = form.existingImages.filterIndexed { index, _ ->
                    index !in form.removedExistingImageIndices
                }
                val finalImages = remainingExistingImages + newImageUrls
                Napier.d("EditProductViewModel: Final images: ${finalImages.size} (${remainingExistingImages.size} existing + ${newImageUrls.size} new)")

                // 3. Создаем запрос на обновление
                val request = UpdateProductRequest(
                    title = form.title,
                    description = form.description,
                    price = form.price.toDouble(),
                    categoryId = form.category!!.id,
                    condition = form.condition,
                    images = finalImages
                )

                // 4. Отправляем запрос на обновление
                val updateResult = apiClient.updateProduct(productId, request)
                if (updateResult.isSuccess) {
                    val response = updateResult.getOrNull()!!
                    _state.value = EditProductState.Success(response.product.id)
                    Napier.d("EditProductViewModel: Product updated successfully: ${response.product.id}")
                } else {
                    val exception = updateResult.exceptionOrNull()!!
                    _state.value = EditProductState.Error(
                        exception.message ?: "Не удалось обновить товар"
                    )
                    Napier.e("EditProductViewModel: Failed to update product", exception)
                }
            } catch (e: Exception) {
                _state.value = EditProductState.Error(e.message ?: "Неизвестная ошибка")
                Napier.e("EditProductViewModel: Exception while updating product", e)
            }
        }
    }

    /**
     * Валидация формы
     */
    private fun validateForm(): Boolean {
        val form = _formState.value
        var hasErrors = false

        // Валидация названия
        if (form.title.isBlank()) {
            _formState.value = _formState.value.copy(titleError = "Введите название")
            hasErrors = true
        } else if (form.title.length > 200) {
            _formState.value = _formState.value.copy(titleError = "Название не должно превышать 200 символов")
            hasErrors = true
        }

        // Валидация описания
        if (form.description.isBlank()) {
            _formState.value = _formState.value.copy(descriptionError = "Введите описание")
            hasErrors = true
        }

        // Валидация цены
        val priceValue = form.price.toDoubleOrNull()
        if (priceValue == null) {
            _formState.value = _formState.value.copy(priceError = "Введите корректную цену")
            hasErrors = true
        } else if (priceValue < 0) {
            _formState.value = _formState.value.copy(priceError = "Цена не может быть отрицательной")
            hasErrors = true
        }

        // Валидация категории
        if (form.category == null) {
            _formState.value = _formState.value.copy(categoryError = "Выберите категорию")
            hasErrors = true
        }

        // Валидация изображений
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

    /**
     * Повтор загрузки при ошибке
     */
    fun retry() {
        loadProduct()
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
