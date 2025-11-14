package info.javaway.sc.shared.presentation.screens.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.javaway.sc.api.models.CreateProductRequest
import info.javaway.sc.api.models.ProductCondition
import info.javaway.sc.shared.data.api.ApiClient
import info.javaway.sc.shared.domain.models.Category
import info.javaway.sc.shared.domain.repository.CategoryRepository
import info.javaway.sc.shared.utils.SelectedImage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для создания товара
 */
class CreateProductViewModel(
    private val apiClient: ApiClient,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow<CreateProductState>(CreateProductState.Loading)
    val state: StateFlow<CreateProductState> = _state.asStateFlow()

    // Состояние формы
    private val _formState = MutableStateFlow(ProductFormState())
    val formState: StateFlow<ProductFormState> = _formState.asStateFlow()

    // Список доступных категорий
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    init {
        loadCategories()
    }

    /**
     * Загрузка категорий товаров
     */
    private fun loadCategories() {
        viewModelScope.launch {
            _state.value = CreateProductState.Loading
            try {
                val result = categoryRepository.getProductCategories()
                if (result.isSuccess) {
                    val categories = result.getOrNull()!!
                    _categories.value = categories
                    _state.value = CreateProductState.Form
                    Napier.d("CreateProductViewModel: Loaded ${categories.size} categories")
                } else {
                    val exception = result.exceptionOrNull()!!
                    _state.value = CreateProductState.Error(
                        exception.message ?: "Не удалось загрузить категории"
                    )
                    Napier.e("CreateProductViewModel: Failed to load categories", exception)
                }
            } catch (e: Exception) {
                _state.value = CreateProductState.Error(e.message ?: "Неизвестная ошибка")
                Napier.e("CreateProductViewModel: Exception while loading categories", e)
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
     * Добавление выбранных изображений
     */
    fun selectImages(images: List<SelectedImage>) {
        val currentImages = _formState.value.selectedImages.toMutableList()
        val totalImages = currentImages.size + images.size

        if (totalImages > 5) {
            _formState.value = _formState.value.copy(
                imagesError = "Можно загрузить максимум 5 изображений"
            )
            return
        }

        currentImages.addAll(images)
        _formState.value = _formState.value.copy(
            selectedImages = currentImages,
            imagesError = null
        )
        Napier.d("CreateProductViewModel: Added ${images.size} images, total: ${currentImages.size}")
    }

    /**
     * Удаление изображения по индексу
     */
    fun removeImage(index: Int) {
        val currentImages = _formState.value.selectedImages.toMutableList()
        if (index in currentImages.indices) {
            currentImages.removeAt(index)
            _formState.value = _formState.value.copy(
                selectedImages = currentImages,
                imagesError = null
            )
            Napier.d("CreateProductViewModel: Removed image at index $index, remaining: ${currentImages.size}")
        }
    }

    /**
     * Создание товара
     */
    fun createProduct() {
        // Валидация
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            _state.value = CreateProductState.Creating
            try {
                val form = _formState.value

                // 1. Загрузка изображений на сервер
                Napier.d("CreateProductViewModel: Uploading ${form.selectedImages.size} images...")
                val imagesToUpload = form.selectedImages.map { image ->
                    Triple(image.bytes, image.name, image.mimeType)
                }

                val uploadResult = apiClient.uploadImages(imagesToUpload, "product")
                if (uploadResult.isSuccess) {
                    val imageUrls = uploadResult.getOrNull()!!
                    Napier.d("CreateProductViewModel: Uploaded images: $imageUrls")

                    // 2. Создание товара с URL изображений
                    val request = CreateProductRequest(
                        title = form.title,
                        description = form.description,
                        price = form.price.toDouble(),
                        categoryId = form.category!!.id,
                        condition = form.condition,
                        images = imageUrls
                    )

                    val createResult = apiClient.createProduct(request)
                    if (createResult.isSuccess) {
                        val product = createResult.getOrNull()!!
                        _state.value = CreateProductState.Success(product.id)
                        Napier.d("CreateProductViewModel: Product created successfully with id: ${product.id}")
                    } else {
                        val exception = createResult.exceptionOrNull()!!
                        _state.value = CreateProductState.Error(
                            exception.message ?: "Не удалось создать товар"
                        )
                        Napier.e("CreateProductViewModel: Failed to create product", exception)
                    }
                } else {
                    val exception = uploadResult.exceptionOrNull()!!
                    _state.value = CreateProductState.Error(
                        exception.message ?: "Не удалось загрузить изображения"
                    )
                    Napier.e("CreateProductViewModel: Failed to upload images", exception)
                }
            } catch (e: Exception) {
                _state.value = CreateProductState.Error(e.message ?: "Неизвестная ошибка")
                Napier.e("CreateProductViewModel: Exception while creating product", e)
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
        if (form.selectedImages.isEmpty()) {
            _formState.value = _formState.value.copy(imagesError = "Добавьте хотя бы одно изображение")
            hasErrors = true
        } else if (form.selectedImages.size > 5) {
            _formState.value = _formState.value.copy(imagesError = "Можно загрузить максимум 5 изображений")
            hasErrors = true
        }

        return !hasErrors
    }

    /**
     * Повтор загрузки категорий при ошибке
     */
    fun retry() {
        loadCategories()
    }
}

/**
 * Состояние экрана создания товара
 */
sealed interface CreateProductState {
    /** Загрузка категорий */
    data object Loading : CreateProductState

    /** Форма готова к заполнению */
    data object Form : CreateProductState

    /** Создание товара в процессе */
    data object Creating : CreateProductState

    /** Товар успешно создан */
    data class Success(val productId: Long) : CreateProductState

    /** Ошибка */
    data class Error(val message: String) : CreateProductState
}

/**
 * Состояние формы создания товара
 */
data class ProductFormState(
    val title: String = "",
    val titleError: String? = null,

    val description: String = "",
    val descriptionError: String? = null,

    val price: String = "",
    val priceError: String? = null,

    val category: Category? = null,
    val categoryError: String? = null,

    val condition: ProductCondition = ProductCondition.USED,

    val selectedImages: List<SelectedImage> = emptyList(),
    val imagesError: String? = null
)
