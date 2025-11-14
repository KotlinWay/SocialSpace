package info.javaway.sc.shared.presentation.screens.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.javaway.sc.api.models.CreateServiceRequest
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
 * ViewModel для создания услуги
 */
class CreateServiceViewModel(
    private val apiClient: ApiClient,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow<CreateServiceState>(CreateServiceState.Loading)
    val state: StateFlow<CreateServiceState> = _state.asStateFlow()

    // Состояние формы
    private val _formState = MutableStateFlow(ServiceFormState())
    val formState: StateFlow<ServiceFormState> = _formState.asStateFlow()

    // Список доступных категорий
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    init {
        loadCategories()
    }

    /**
     * Загрузка категорий услуг
     */
    private fun loadCategories() {
        viewModelScope.launch {
            _state.value = CreateServiceState.Loading
            try {
                val result = categoryRepository.getServiceCategories()
                if (result.isSuccess) {
                    val categories = result.getOrNull()!!
                    _categories.value = categories
                    _state.value = CreateServiceState.Form
                    Napier.d("CreateServiceViewModel: Loaded ${categories.size} categories")
                } else {
                    val exception = result.exceptionOrNull()!!
                    _state.value = CreateServiceState.Error(
                        exception.message ?: "Не удалось загрузить категории"
                    )
                    Napier.e("CreateServiceViewModel: Failed to load categories", exception)
                }
            } catch (e: Exception) {
                _state.value = CreateServiceState.Error(e.message ?: "Неизвестная ошибка")
                Napier.e("CreateServiceViewModel: Exception while loading categories", e)
            }
        }
    }

    /**
     * Обновление названия услуги
     */
    fun updateTitle(title: String) {
        _formState.value = _formState.value.copy(
            title = title,
            titleError = null
        )
    }

    /**
     * Обновление описания услуги
     */
    fun updateDescription(description: String) {
        _formState.value = _formState.value.copy(
            description = description,
            descriptionError = null
        )
    }

    /**
     * Обновление цены услуги (опциональное, может быть "Договорная")
     */
    fun updatePrice(price: String) {
        _formState.value = _formState.value.copy(
            price = price,
            priceError = null
        )
    }

    /**
     * Переключение "Договорная цена"
     */
    fun toggleNegotiablePrice(isNegotiable: Boolean) {
        _formState.value = _formState.value.copy(
            isNegotiable = isNegotiable,
            price = if (isNegotiable) "" else _formState.value.price,
            priceError = null
        )
    }

    /**
     * Выбор категории услуги
     */
    fun selectCategory(category: Category) {
        _formState.value = _formState.value.copy(
            category = category,
            categoryError = null
        )
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
        Napier.d("CreateServiceViewModel: Added ${images.size} images, total: ${currentImages.size}")
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
            Napier.d("CreateServiceViewModel: Removed image at index $index, remaining: ${currentImages.size}")
        }
    }

    /**
     * Создание услуги
     */
    fun createService() {
        // Валидация
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            _state.value = CreateServiceState.Creating
            try {
                val form = _formState.value

                // 1. Загрузка изображений на сервер
                Napier.d("CreateServiceViewModel: Uploading ${form.selectedImages.size} images...")
                val imagesToUpload = form.selectedImages.map { image ->
                    Triple(image.bytes, image.name, image.mimeType)
                }

                val uploadResult = apiClient.uploadImages(imagesToUpload, "service")
                if (uploadResult.isSuccess) {
                    val imageUrls = uploadResult.getOrNull()!!
                    Napier.d("CreateServiceViewModel: Uploaded images: $imageUrls")

                    // 2. Создание услуги с URL изображений
                    val request = CreateServiceRequest(
                        title = form.title,
                        description = form.description,
                        price = if (form.isNegotiable) null else form.price.ifBlank { null },
                        categoryId = form.category!!.id,
                        images = imageUrls
                    )

                    val createResult = apiClient.createService(request)
                    if (createResult.isSuccess) {
                        val response = createResult.getOrNull()!!
                        _state.value = CreateServiceState.Success(response.service.id)
                        Napier.d("CreateServiceViewModel: Service created successfully with id: ${response.service.id}")
                    } else {
                        val exception = createResult.exceptionOrNull()!!
                        _state.value = CreateServiceState.Error(
                            exception.message ?: "Не удалось создать услугу"
                        )
                        Napier.e("CreateServiceViewModel: Failed to create service", exception)
                    }
                } else {
                    val exception = uploadResult.exceptionOrNull()!!
                    _state.value = CreateServiceState.Error(
                        exception.message ?: "Не удалось загрузить изображения"
                    )
                    Napier.e("CreateServiceViewModel: Failed to upload images", exception)
                }
            } catch (e: Exception) {
                _state.value = CreateServiceState.Error(e.message ?: "Неизвестная ошибка")
                Napier.e("CreateServiceViewModel: Exception while creating service", e)
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

        // Валидация цены (только если не "Договорная")
        if (!form.isNegotiable && form.price.isNotBlank()) {
            val priceValue = form.price.toDoubleOrNull()
            if (priceValue == null) {
                _formState.value = _formState.value.copy(priceError = "Введите корректную цену")
                hasErrors = true
            } else if (priceValue < 0) {
                _formState.value = _formState.value.copy(priceError = "Цена не может быть отрицательной")
                hasErrors = true
            }
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
 * Состояние экрана создания услуги
 */
sealed interface CreateServiceState {
    /** Загрузка категорий */
    data object Loading : CreateServiceState

    /** Форма готова к заполнению */
    data object Form : CreateServiceState

    /** Создание услуги в процессе */
    data object Creating : CreateServiceState

    /** Услуга успешно создана */
    data class Success(val serviceId: Long) : CreateServiceState

    /** Ошибка */
    data class Error(val message: String) : CreateServiceState
}

/**
 * Состояние формы создания услуги
 */
data class ServiceFormState(
    val title: String = "",
    val titleError: String? = null,

    val description: String = "",
    val descriptionError: String? = null,

    val price: String = "",
    val priceError: String? = null,
    val isNegotiable: Boolean = false, // "Договорная" цена

    val category: Category? = null,
    val categoryError: String? = null,

    val selectedImages: List<SelectedImage> = emptyList(),
    val imagesError: String? = null
)
