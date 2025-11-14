package info.javaway.sc.shared.presentation.screens.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.javaway.sc.api.models.UpdateServiceRequest
import info.javaway.sc.shared.data.api.ApiClient
import info.javaway.sc.shared.domain.models.Category
import info.javaway.sc.shared.domain.repository.CategoryRepository
import info.javaway.sc.shared.domain.repository.ServiceRepository
import info.javaway.sc.shared.utils.SelectedImage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для редактирования услуги
 */
class EditServiceViewModel(
    private val serviceId: Long,
    private val serviceRepository: ServiceRepository,
    private val apiClient: ApiClient,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow<EditServiceState>(EditServiceState.Loading)
    val state: StateFlow<EditServiceState> = _state.asStateFlow()

    // Состояние формы
    private val _formState = MutableStateFlow(EditServiceFormState())
    val formState: StateFlow<EditServiceFormState> = _formState.asStateFlow()

    // Список доступных категорий
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    init {
        loadService()
    }

    /**
     * Загрузка услуги и категорий
     */
    private fun loadService() {
        viewModelScope.launch {
            _state.value = EditServiceState.Loading
            try {
                // Загружаем категории и услугу параллельно
                val categoriesResult = categoryRepository.getServiceCategories()
                val serviceResult = serviceRepository.getService(serviceId)

                if (categoriesResult.isSuccess && serviceResult.isSuccess) {
                    val categories = categoriesResult.getOrNull()!!
                    val service = serviceResult.getOrNull()!!

                    _categories.value = categories

                    // Находим категорию услуги
                    val category = categories.find { it.id == service.category.id }

                    // Определяем договорная ли цена
                    val isNegotiable = service.price == null

                    // Предзаполняем форму данными услуги
                    _formState.value = EditServiceFormState(
                        title = service.title,
                        description = service.description,
                        price = service.price ?: "",
                        isNegotiable = isNegotiable,
                        category = category,
                        existingImages = service.images,
                        newImages = emptyList(),
                        removedExistingImageIndices = emptySet()
                    )

                    _state.value = EditServiceState.Form
                    Napier.d("EditServiceViewModel: Loaded service ${service.id} and ${categories.size} categories")
                } else {
                    val error = categoriesResult.exceptionOrNull() ?: serviceResult.exceptionOrNull()
                    _state.value = EditServiceState.Error(
                        error?.message ?: "Не удалось загрузить данные"
                    )
                    Napier.e("EditServiceViewModel: Failed to load data", error)
                }
            } catch (e: Exception) {
                _state.value = EditServiceState.Error(e.message ?: "Неизвестная ошибка")
                Napier.e("EditServiceViewModel: Exception while loading service", e)
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
     * Обновление цены услуги
     */
    fun updatePrice(price: String) {
        _formState.value = _formState.value.copy(
            price = price,
            priceError = null
        )
    }

    /**
     * Переключение режима "Договорная цена"
     */
    fun toggleNegotiablePrice() {
        val currentState = _formState.value
        _formState.value = currentState.copy(
            isNegotiable = !currentState.isNegotiable,
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
        Napier.d("EditServiceViewModel: Added ${images.size} new images, total new: ${updatedNewImages.size}")
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
            Napier.d("EditServiceViewModel: Marked existing image at index $index for removal")
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
            Napier.d("EditServiceViewModel: Removed new image at index $index, remaining: ${updatedNewImages.size}")
        }
    }

    /**
     * Обновление услуги
     */
    fun updateService() {
        // Валидация
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            _state.value = EditServiceState.Updating
            try {
                val form = _formState.value

                // 1. Загружаем новые изображения (если есть)
                val newImageUrls = if (form.newImages.isNotEmpty()) {
                    Napier.d("EditServiceViewModel: Uploading ${form.newImages.size} new images...")
                    val imagesToUpload = form.newImages.map { image ->
                        Triple(image.bytes, image.name, image.mimeType)
                    }

                    val uploadResult = apiClient.uploadImages(imagesToUpload, "service")
                    if (uploadResult.isSuccess) {
                        uploadResult.getOrNull()!!
                    } else {
                        val exception = uploadResult.exceptionOrNull()!!
                        _state.value = EditServiceState.Error(
                            exception.message ?: "Не удалось загрузить изображения"
                        )
                        Napier.e("EditServiceViewModel: Failed to upload images", exception)
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
                Napier.d("EditServiceViewModel: Final images: ${finalImages.size} (${remainingExistingImages.size} existing + ${newImageUrls.size} new)")

                // 3. Создаем запрос на обновление
                val request = UpdateServiceRequest(
                    title = form.title,
                    description = form.description,
                    price = if (form.isNegotiable) null else form.price,
                    categoryId = form.category!!.id,
                    images = finalImages
                )

                // 4. Отправляем запрос на обновление
                val updateResult = apiClient.updateService(serviceId, request)
                if (updateResult.isSuccess) {
                    val response = updateResult.getOrNull()!!
                    _state.value = EditServiceState.Success(response.service.id)
                    Napier.d("EditServiceViewModel: Service updated successfully: ${response.service.id}")
                } else {
                    val exception = updateResult.exceptionOrNull()!!
                    _state.value = EditServiceState.Error(
                        exception.message ?: "Не удалось обновить услугу"
                    )
                    Napier.e("EditServiceViewModel: Failed to update service", exception)
                }
            } catch (e: Exception) {
                _state.value = EditServiceState.Error(e.message ?: "Неизвестная ошибка")
                Napier.e("EditServiceViewModel: Exception while updating service", e)
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

        // Валидация цены (только если не договорная)
        if (!form.isNegotiable) {
            if (form.price.isBlank()) {
                _formState.value = _formState.value.copy(priceError = "Введите цену или выберите 'Договорная'")
                hasErrors = true
            }
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
        loadService()
    }
}

/**
 * Состояние экрана редактирования услуги
 */
sealed interface EditServiceState {
    /** Загрузка услуги и категорий */
    data object Loading : EditServiceState

    /** Форма готова к редактированию */
    data object Form : EditServiceState

    /** Обновление услуги в процессе */
    data object Updating : EditServiceState

    /** Услуга успешно обновлена */
    data class Success(val serviceId: Long) : EditServiceState

    /** Ошибка */
    data class Error(val message: String) : EditServiceState
}

/**
 * Состояние формы редактирования услуги
 */
data class EditServiceFormState(
    val title: String = "",
    val titleError: String? = null,

    val description: String = "",
    val descriptionError: String? = null,

    val price: String = "",
    val priceError: String? = null,
    val isNegotiable: Boolean = false,

    val category: Category? = null,
    val categoryError: String? = null,

    // Существующие изображения (URL с сервера)
    val existingImages: List<String> = emptyList(),

    // Новые выбранные изображения (локальные файлы)
    val newImages: List<SelectedImage> = emptyList(),

    // Индексы удалённых существующих изображений
    val removedExistingImageIndices: Set<Int> = emptySet(),

    val imagesError: String? = null
)
