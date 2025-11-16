package info.javaway.sc.shared.presentation.screens.services.create

import com.arkivanov.decompose.ComponentContext
import info.javaway.sc.api.models.CreateServiceRequest
import info.javaway.sc.shared.data.api.ApiClient
import info.javaway.sc.shared.data.local.SpaceManager
import info.javaway.sc.shared.domain.models.Category
import info.javaway.sc.shared.domain.repository.CategoryRepository
import info.javaway.sc.shared.presentation.core.BaseComponent
import info.javaway.sc.shared.utils.SelectedImage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface CreateServiceComponent {
    val state: StateFlow<CreateServiceState>
    val formState: StateFlow<ServiceFormState>
    val categories: StateFlow<List<Category>>

    fun updateTitle(title: String)
    fun updateDescription(description: String)
    fun updatePrice(price: String)
    fun toggleNegotiablePrice(isNegotiable: Boolean)
    fun selectCategory(category: Category)
    fun selectImages(images: List<SelectedImage>)
    fun removeImage(index: Int)
    fun createService()
    fun retry()
}

class DefaultCreateServiceComponent(
    componentContext: ComponentContext,
    private val apiClient: ApiClient,
    private val categoryRepository: CategoryRepository,
    private val spaceManager: SpaceManager
) : BaseComponent(componentContext), CreateServiceComponent {

    private val _state = MutableStateFlow<CreateServiceState>(CreateServiceState.Loading)
    override val state: StateFlow<CreateServiceState> = _state.asStateFlow()

    private val _formState = MutableStateFlow(ServiceFormState())
    override val formState: StateFlow<ServiceFormState> = _formState.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    override val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    init {
        loadCategories()
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

    override fun toggleNegotiablePrice(isNegotiable: Boolean) {
        _formState.value = _formState.value.copy(
            isNegotiable = isNegotiable,
            price = if (isNegotiable) "" else _formState.value.price,
            priceError = null
        )
    }

    override fun selectCategory(category: Category) {
        _formState.value = _formState.value.copy(category = category, categoryError = null)
    }

    override fun selectImages(images: List<SelectedImage>) {
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
        Napier.d("CreateServiceComponent: Added ${images.size} images, total: ${currentImages.size}")
    }

    override fun removeImage(index: Int) {
        val currentImages = _formState.value.selectedImages.toMutableList()
        if (index in currentImages.indices) {
            currentImages.removeAt(index)
            _formState.value = _formState.value.copy(
                selectedImages = currentImages,
                imagesError = null
            )
            Napier.d("CreateServiceComponent: Removed image at index $index, remaining: ${currentImages.size}")
        }
    }

    override fun createService() {
        if (!validateForm()) {
            return
        }

        componentScope.launch {
            _state.value = CreateServiceState.Creating
            try {
                val spaceId = spaceManager.getCurrentSpaceId()
                if (spaceId == null) {
                    _state.value = CreateServiceState.Error("Сначала выберите пространство")
                    return@launch
                }

                val form = _formState.value

                Napier.d("CreateServiceComponent: Uploading ${form.selectedImages.size} images...")
                val imagesToUpload = form.selectedImages.map { image ->
                    Triple(image.bytes, image.name, image.mimeType)
                }

                val uploadResult = apiClient.uploadImages(imagesToUpload, "service")
                if (uploadResult.isSuccess) {
                    val imageUrls = uploadResult.getOrNull()!!
                    Napier.d("CreateServiceComponent: Uploaded images: $imageUrls")

                    val request = CreateServiceRequest(
                        title = form.title,
                        description = form.description,
                        price = if (form.isNegotiable) null else form.price.ifBlank { null },
                        categoryId = form.category!!.id,
                        images = imageUrls,
                        spaceId = spaceId
                    )

                    val createResult = apiClient.createService(request)
                    if (createResult.isSuccess) {
                        val response = createResult.getOrNull()!!
                        _state.value = CreateServiceState.Success(response.service.id)
                        Napier.d("CreateServiceComponent: Service created with id: ${response.service.id}")
                    } else {
                        val exception = createResult.exceptionOrNull()!!
                        _state.value = CreateServiceState.Error(
                            exception.message ?: "Не удалось создать услугу"
                        )
                        Napier.e("CreateServiceComponent: Failed to create service", exception)
                    }
                } else {
                    val exception = uploadResult.exceptionOrNull()!!
                    _state.value = CreateServiceState.Error(
                        exception.message ?: "Не удалось загрузить изображения"
                    )
                    Napier.e("CreateServiceComponent: Failed to upload images", exception)
                }
            } catch (e: Exception) {
                _state.value = CreateServiceState.Error(e.message ?: "Неизвестная ошибка")
                Napier.e("CreateServiceComponent: Exception while creating service", e)
            }
        }
    }

    override fun retry() {
        loadCategories()
    }

    private fun loadCategories() {
        componentScope.launch {
            _state.value = CreateServiceState.Loading
            try {
                val result = categoryRepository.getServiceCategories()
                if (result.isSuccess) {
                    val categories = result.getOrNull()!!
                    _categories.value = categories
                    _state.value = CreateServiceState.Form
                    Napier.d("CreateServiceComponent: Loaded ${categories.size} categories")
                } else {
                    val exception = result.exceptionOrNull()!!
                    _state.value = CreateServiceState.Error(
                        exception.message ?: "Не удалось загрузить категории"
                    )
                    Napier.e("CreateServiceComponent: Failed to load categories", exception)
                }
            } catch (e: Exception) {
                _state.value = CreateServiceState.Error(e.message ?: "Неизвестная ошибка")
                Napier.e("CreateServiceComponent: Exception while loading categories", e)
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

        if (form.category == null) {
            _formState.value = _formState.value.copy(categoryError = "Выберите категорию")
            hasErrors = true
        }

        if (form.selectedImages.isEmpty()) {
            _formState.value = _formState.value.copy(imagesError = "Добавьте хотя бы одно изображение")
            hasErrors = true
        } else if (form.selectedImages.size > 5) {
            _formState.value = _formState.value.copy(imagesError = "Можно загрузить максимум 5 изображений")
            hasErrors = true
        }

        return !hasErrors
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
