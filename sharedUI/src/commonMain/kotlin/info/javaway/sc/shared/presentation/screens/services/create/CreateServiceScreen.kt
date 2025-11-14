package info.javaway.sc.shared.presentation.screens.services.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import info.javaway.sc.shared.domain.models.Category
import info.javaway.sc.shared.domain.models.CategoryType
import info.javaway.sc.shared.presentation.components.CategorySelectorField
import info.javaway.sc.shared.utils.SelectedImage
import info.javaway.sc.shared.utils.rememberImagePickerLauncher

/**
 * Экран создания услуги
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateServiceScreen(
    component: CreateServiceComponent,
    onBack: () -> Unit,
    onSuccess: (Long) -> Unit
) {
    val state by component.state.collectAsState()
    val formState by component.formState.collectAsState()
    val categories by component.categories.collectAsState()

    // Image picker launcher
    val imagePickerLauncher = rememberImagePickerLauncher(maxImages = 5) { images ->
        component.selectImages(images)
    }

    // Обработка успешного создания
    LaunchedEffect(state) {
        if (state is CreateServiceState.Success) {
            val serviceId = (state as CreateServiceState.Success).serviceId
            onSuccess(serviceId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новая услуга") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        when (state) {
            is CreateServiceState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is CreateServiceState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = (state as CreateServiceState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(onClick = { component.retry() }) {
                            Text("Повторить")
                        }
                    }
                }
            }

            is CreateServiceState.Form, is CreateServiceState.Creating -> {
                CreateServiceForm(
                    formState = formState,
                    categories = categories,
                    isCreating = state is CreateServiceState.Creating,
                    onTitleChange = component::updateTitle,
                    onDescriptionChange = component::updateDescription,
                    onPriceChange = component::updatePrice,
                    onNegotiablePriceToggle = component::toggleNegotiablePrice,
                    onCategorySelect = component::selectCategory,
                    onAddImages = { imagePickerLauncher() },
                    onRemoveImage = component::removeImage,
                    onCreate = component::createService,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is CreateServiceState.Success -> {
                // Success обрабатывается в LaunchedEffect выше
                // Показываем индикатор загрузки пока идет навигация
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

/**
 * Форма создания услуги
 */
@Composable
private fun CreateServiceForm(
    formState: ServiceFormState,
    categories: List<Category>,
    isCreating: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onNegotiablePriceToggle: (Boolean) -> Unit,
    onCategorySelect: (Category) -> Unit,
    onAddImages: () -> Unit,
    onRemoveImage: (Int) -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Название
        OutlinedTextField(
            value = formState.title,
            onValueChange = onTitleChange,
            label = { Text("Название *") },
            placeholder = { Text("Введите название услуги") },
            isError = formState.titleError != null,
            supportingText = formState.titleError?.let { { Text(it) } },
            enabled = !isCreating,
            modifier = Modifier.fillMaxWidth()
        )

        // Описание
        OutlinedTextField(
            value = formState.description,
            onValueChange = onDescriptionChange,
            label = { Text("Описание *") },
            placeholder = { Text("Опишите услугу, условия, опыт работы") },
            isError = formState.descriptionError != null,
            supportingText = formState.descriptionError?.let { { Text(it) } },
            enabled = !isCreating,
            minLines = 4,
            maxLines = 8,
            modifier = Modifier.fillMaxWidth()
        )

        // Цена и чекбокс "Договорная"
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = formState.price,
                onValueChange = onPriceChange,
                label = { Text("Цена, ₽") },
                placeholder = { Text("Введите стоимость или оставьте пустым") },
                isError = formState.priceError != null,
                supportingText = formState.priceError?.let { { Text(it) } },
                enabled = !isCreating && !formState.isNegotiable,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = formState.isNegotiable,
                    onCheckedChange = onNegotiablePriceToggle,
                    enabled = !isCreating
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Договорная цена",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Категория
        CategorySelectorField(
            selectedCategory = formState.category,
            categories = categories,
            categoryType = CategoryType.SERVICE,
            onCategorySelected = onCategorySelect,
            enabled = !isCreating
        )
        if (formState.categoryError != null) {
            Text(
                text = formState.categoryError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Изображения
        ImageSelector(
            selectedImages = formState.selectedImages,
            imagesError = formState.imagesError,
            onAddImages = onAddImages,
            onRemoveImage = onRemoveImage,
            enabled = !isCreating
        )

        // Кнопка создания
        Button(
            onClick = onCreate,
            enabled = !isCreating,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (isCreating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isCreating) "Создание..." else "Создать услугу")
        }

        // Отступ снизу для удобства прокрутки
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Компонент выбора изображений
 */
@Composable
private fun ImageSelector(
    selectedImages: List<SelectedImage>,
    imagesError: String?,
    onAddImages: () -> Unit,
    onRemoveImage: (Int) -> Unit,
    enabled: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Фотографии * (${selectedImages.size}/5)",
                style = MaterialTheme.typography.bodyLarge,
                color = if (imagesError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            if (selectedImages.size < 5 && enabled) {
                TextButton(onClick = onAddImages) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Добавить")
                }
            }
        }

        if (imagesError != null) {
            Text(
                text = imagesError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Галерея выбранных изображений
        if (selectedImages.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                selectedImages.forEachIndexed { index, image ->
                    ImagePreviewCard(
                        image = image,
                        onRemove = { onRemoveImage(index) },
                        enabled = enabled
                    )
                }
            }
        } else {
            // Placeholder когда нет изображений
            if (enabled) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    onClick = onAddImages
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Нажмите, чтобы добавить фото",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Добавьте фото примеров работ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Карточка предпросмотра изображения
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImagePreviewCard(
    image: SelectedImage,
    onRemove: () -> Unit,
    enabled: Boolean
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Информация об изображении
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = image.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1
                )
                Text(
                    text = "${image.bytes.size / 1024} KB • ${image.mimeType}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Кнопка удаления
            if (enabled) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Удалить"
                    )
                }
            }
        }
    }
}
