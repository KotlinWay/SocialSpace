package info.javaway.sc.shared.presentation.screens.services

import com.arkivanov.decompose.ComponentContext
import info.javaway.sc.api.models.UpdateServiceRequest
import info.javaway.sc.shared.data.api.ApiClient
import info.javaway.sc.shared.domain.models.Category
import info.javaway.sc.shared.domain.repository.CategoryRepository
import info.javaway.sc.shared.domain.repository.ServiceRepository
import info.javaway.sc.shared.presentation.core.BaseComponent
import info.javaway.sc.shared.utils.SelectedImage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface EditServiceComponent {
    val state: StateFlow<EditServiceState>
    val formState: StateFlow<EditServiceFormState>
    val categories: StateFlow<List<Category>>

    fun updateTitle(title: String)
    fun updateDescription(description: String)
    fun updatePrice(price: String)
    fun toggleNegotiablePrice()
    fun selectCategory(category: Category)
    fun selectImages(images: List<SelectedImage>)
    fun removeExistingImage(index: Int)
    fun removeNewImage(index: Int)
    fun updateService()
    fun retry()
}

class DefaultEditServiceComponent(
    componentContext: ComponentContext,
    private val serviceId: Long,
    private val serviceRepository: ServiceRepository,
    private val apiClient: ApiClient,
    private val categoryRepository: CategoryRepository
) : BaseComponent(componentContext), EditServiceComponent {

    private val _state = MutableStateFlow<EditServiceState>(EditServiceState.Loading)
    override val state: StateFlow<EditServiceState> = _state.asStateFlow()

    private val _formState = MutableStateFlow(EditServiceFormState())
    override val formState: StateFlow<EditServiceFormState> = _formState.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    override val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    init {
        loadService()
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

    override fun toggleNegotiablePrice() {
        val currentState = _formState.value
        _formState.value = currentState.copy(isNegotiable = !currentState.isNegotiable, priceError = null)
    }

    override fun selectCategory(category: Category) {
        _formState.value = _formState.value.copy(category = category, categoryError = null)
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
        Napier.d("EditServiceComponent: Added ${images.size} new images, total new: ${updatedNewImages.size}")
    }

    override fun removeExistingImage(index: Int) {
        val form = _formState.value
        if (index in form.existingImages.indices) {
            val updatedRemovedIndices = form.removedExistingImageIndices.toMutableSet().apply { add(index) }
            _formState.value = _formState.value.copy(
                removedExistingImageIndices = updatedRemovedIndices,
                imagesError = null
            )
            Napier.d("EditServiceComponent: Marked existing image $index for removal")
        }
    }

    override fun removeNewImage(index: Int) {
        val form = _formState.value
        if (index in form.newImages.indices) {
            val updatedNewImages = form.newImages.toMutableList().apply { removeAt(index) }
            _formState.value = _formState.value.copy(newImages = updatedNewImages, imagesError = null)
            Napier.d("EditServiceComponent: Removed new image $index, remaining: ${updatedNewImages.size}")
        }
    }

    override fun updateService() {
        if (!validateForm()) {
            return
        }

        componentScope.launch {
            _state.value = EditServiceState.Updating
            try {
                val form = _formState.value

                val newImageUrls = if (form.newImages.isNotEmpty()) {
                    Napier.d("EditServiceComponent: Uploading ${form.newImages.size} new images...")
                    val imagesToUpload = form.newImages.map { Triple(it.bytes, it.name, it.mimeType) }

                    val uploadResult = apiClient.uploadImages(imagesToUpload, "service")
                    if (uploadResult.isSuccess) {
                        uploadResult.getOrNull()!!
                    } else {
                        val exception = uploadResult.exceptionOrNull()!!
                        _state.value = EditServiceState.Error(
                            exception.message ?: "Не удалось загрузить изображения"
                        )
                        Napier.e("EditServiceComponent: Failed to upload images", exception)
                        return@launch
                    }
                } else {
                    emptyList()
                }

                val remainingExistingImages = form.existingImages.filterIndexed { index, _ ->
                    index !in form.removedExistingImageIndices
                }
                val finalImages = remainingExistingImages + newImageUrls
                Napier.d("EditServiceComponent: Final images: ${finalImages.size}")

                val request = UpdateServiceRequest(
                    title = form.title,
                    description = form.description,
                    price = if (form.isNegotiable) null else form.price,
                    categoryId = form.category!!.id,
                    images = finalImages
                )

                val updateResult = apiClient.updateService(serviceId, request)
                if (updateResult.isSuccess) {
                    val response = updateResult.getOrNull()!!
                    _state.value = EditServiceState.Success(response.service.id)
                    Napier.d("EditServiceComponent: Service updated ${response.service.id}")
                } else {
                    val exception = updateResult.exceptionOrNull()!!
                    _state.value = EditServiceState.Error(exception.message ?: "Не удалось обновить услугу")
                    Napier.e("EditServiceComponent: Failed to update service", exception)
                }
            } catch (e: Exception) {
                _state.value = EditServiceState.Error(e.message ?: "Неизвестная ошибка")
                Napier.e("EditServiceComponent: Exception while updating service", e)
            }
        }
    }

    override fun retry() {
        loadService()
    }

    private fun loadService() {
        componentScope.launch {
            _state.value = EditServiceState.Loading
            try {
                val categoriesResult = categoryRepository.getServiceCategories()
                val serviceResult = serviceRepository.getService(serviceId)

                if (categoriesResult.isSuccess && serviceResult.isSuccess) {
                    val categories = categoriesResult.getOrNull()!!
                    val service = serviceResult.getOrNull()!!

                    _categories.value = categories

                    val category = categories.find { it.id == service.category.id }
                    val isNegotiable = service.price == null

                    _formState.value = EditServiceFormState(
                        title = service.title,
                        description = service.description,
                        price = service.price ?: "",
                        isNegotiable = isNegotiable,
                        category = category,
                        existingImages = service.images
                    )

                    _state.value = EditServiceState.Form
                    Napier.d("EditServiceComponent: Loaded service ${service.id} and ${categories.size} categories")
                } else {
                    val error = categoriesResult.exceptionOrNull() ?: serviceResult.exceptionOrNull()
                    _state.value = EditServiceState.Error(error?.message ?: "Не удалось загрузить данные")
                    Napier.e("EditServiceComponent: Failed to load data", error)
                }
            } catch (e: Exception) {
                _state.value = EditServiceState.Error(e.message ?: "Неизвестная ошибка")
                Napier.e("EditServiceComponent: Exception while loading service", e)
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

        if (!form.isNegotiable && form.price.isBlank()) {
            _formState.value = _formState.value.copy(priceError = "Введите цену или выберите 'Договорная'")
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
