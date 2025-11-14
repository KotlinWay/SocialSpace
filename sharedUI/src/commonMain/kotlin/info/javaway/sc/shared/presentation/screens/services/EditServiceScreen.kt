package info.javaway.sc.shared.presentation.screens.services

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import info.javaway.sc.shared.domain.models.CategoryType
import info.javaway.sc.shared.presentation.components.CategorySelectorField
import info.javaway.sc.shared.utils.rememberImagePickerLauncher

/**
 * Экран редактирования услуги
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditServiceScreen(
    viewModel: EditServiceViewModel,
    onBack: () -> Unit,
    onSuccess: (Long) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val categories by viewModel.categories.collectAsState()

    // Image picker launcher
    val imagePickerLauncher = rememberImagePickerLauncher(maxImages = 5) { images ->
        viewModel.selectImages(images)
    }

    // Обработка успешного обновления
    LaunchedEffect(state) {
        if (state is EditServiceState.Success) {
            val serviceId = (state as EditServiceState.Success).serviceId
            onSuccess(serviceId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактирование услуги") },
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
            is EditServiceState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is EditServiceState.Error -> {
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
                            text = (state as EditServiceState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(onClick = { viewModel.retry() }) {
                            Text("Повторить")
                        }
                    }
                }
            }

            is EditServiceState.Form, is EditServiceState.Updating -> {
                EditServiceForm(
                    formState = formState,
                    categories = categories,
                    isUpdating = state is EditServiceState.Updating,
                    onTitleChange = viewModel::updateTitle,
                    onDescriptionChange = viewModel::updateDescription,
                    onPriceChange = viewModel::updatePrice,
                    onToggleNegotiable = viewModel::toggleNegotiablePrice,
                    onCategorySelect = viewModel::selectCategory,
                    onAddImages = { imagePickerLauncher() },
                    onRemoveExistingImage = viewModel::removeExistingImage,
                    onRemoveNewImage = viewModel::removeNewImage,
                    onUpdate = viewModel::updateService,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is EditServiceState.Success -> {
                // Success обрабатывается в LaunchedEffect выше
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
 * Форма редактирования услуги
 */
@Composable
private fun EditServiceForm(
    formState: EditServiceFormState,
    categories: List<info.javaway.sc.shared.domain.models.Category>,
    isUpdating: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onToggleNegotiable: () -> Unit,
    onCategorySelect: (info.javaway.sc.shared.domain.models.Category) -> Unit,
    onAddImages: () -> Unit,
    onRemoveExistingImage: (Int) -> Unit,
    onRemoveNewImage: (Int) -> Unit,
    onUpdate: () -> Unit,
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
            label = { Text("Название услуги *") },
            placeholder = { Text("Введите название услуги") },
            isError = formState.titleError != null,
            supportingText = formState.titleError?.let { { Text(it) } },
            enabled = !isUpdating,
            modifier = Modifier.fillMaxWidth()
        )

        // Описание
        OutlinedTextField(
            value = formState.description,
            onValueChange = onDescriptionChange,
            label = { Text("Описание *") },
            placeholder = { Text("Опишите услугу") },
            isError = formState.descriptionError != null,
            supportingText = formState.descriptionError?.let { { Text(it) } },
            enabled = !isUpdating,
            minLines = 4,
            maxLines = 8,
            modifier = Modifier.fillMaxWidth()
        )

        // Цена и Договорная
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = formState.price,
                onValueChange = onPriceChange,
                label = { Text("Цена, ₽") },
                placeholder = { Text("0") },
                isError = formState.priceError != null,
                supportingText = formState.priceError?.let { { Text(it) } },
                enabled = !isUpdating && !formState.isNegotiable,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = formState.isNegotiable,
                    onCheckedChange = { onToggleNegotiable() },
                    enabled = !isUpdating
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Договорная цена",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Категория
        CategorySelectorField(
            selectedCategory = formState.category,
            categories = categories,
            categoryType = CategoryType.SERVICE,
            onCategorySelected = onCategorySelect,
            enabled = !isUpdating
        )
        if (formState.categoryError != null) {
            Text(
                text = formState.categoryError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Изображения (существующие + новые)
        EditImageSelector(
            existingImages = formState.existingImages,
            newImages = formState.newImages,
            removedExistingImageIndices = formState.removedExistingImageIndices,
            imagesError = formState.imagesError,
            onAddImages = onAddImages,
            onRemoveExistingImage = onRemoveExistingImage,
            onRemoveNewImage = onRemoveNewImage,
            enabled = !isUpdating
        )

        // Кнопка обновления
        Button(
            onClick = onUpdate,
            enabled = !isUpdating,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (isUpdating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isUpdating) "Сохранение..." else "Сохранить изменения")
        }

        // Отступ снизу для удобства прокрутки
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Компонент выбора изображений для редактирования
 * Показывает существующие изображения + новые
 */
@Composable
private fun EditImageSelector(
    existingImages: List<String>,
    newImages: List<info.javaway.sc.shared.utils.SelectedImage>,
    removedExistingImageIndices: Set<Int>,
    imagesError: String?,
    onAddImages: () -> Unit,
    onRemoveExistingImage: (Int) -> Unit,
    onRemoveNewImage: (Int) -> Unit,
    enabled: Boolean
) {
    val totalImages = existingImages.size - removedExistingImageIndices.size + newImages.size

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Фотографии примеров работ * ($totalImages/5)",
                style = MaterialTheme.typography.bodyLarge,
                color = if (imagesError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            if (totalImages < 5 && enabled) {
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

        // Существующие изображения
        if (existingImages.isNotEmpty()) {
            Text(
                text = "Текущие фотографии",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                existingImages.forEachIndexed { index, imageUrl ->
                    if (index !in removedExistingImageIndices) {
                        ExistingImageCard(
                            imageUrl = imageUrl,
                            onRemove = { onRemoveExistingImage(index) },
                            enabled = enabled
                        )
                    }
                }
            }
        }

        // Новые изображения
        if (newImages.isNotEmpty()) {
            Text(
                text = "Новые фотографии",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                newImages.forEachIndexed { index, image ->
                    NewImagePreviewCard(
                        image = image,
                        onRemove = { onRemoveNewImage(index) },
                        enabled = enabled
                    )
                }
            }
        }

        // Placeholder если нет изображений
        if (totalImages == 0 && enabled) {
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
        }
    }
}

/**
 * Карточка существующего изображения (с Coil)
 */
@Composable
private fun ExistingImageCard(
    imageUrl: String,
    onRemove: () -> Unit,
    enabled: Boolean
) {
    val context = LocalPlatformContext.current

    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Превью изображения через Coil
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                imageLoader = ImageLoader.Builder(context)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp),
                contentScale = ContentScale.Crop
            )

            // URL изображения
            Text(
                text = imageUrl.substringAfterLast('/'),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 2
            )

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

/**
 * Карточка нового изображения (локальное)
 */
@Composable
private fun NewImagePreviewCard(
    image: info.javaway.sc.shared.utils.SelectedImage,
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
