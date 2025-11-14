package info.javaway.sc.shared.presentation.screens.products

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import info.javaway.sc.api.models.ProductCondition
import info.javaway.sc.shared.domain.models.CategoryType
import info.javaway.sc.shared.presentation.components.CategorySelectorField
import info.javaway.sc.shared.utils.rememberImagePickerLauncher

/**
 * Экран создания товара
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProductScreen(
    viewModel: CreateProductViewModel,
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

    // Обработка успешного создания
    LaunchedEffect(state) {
        if (state is CreateProductState.Success) {
            val productId = (state as CreateProductState.Success).productId
            onSuccess(productId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новый товар") },
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
            is CreateProductState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is CreateProductState.Error -> {
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
                            text = (state as CreateProductState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(onClick = { viewModel.retry() }) {
                            Text("Повторить")
                        }
                    }
                }
            }

            is CreateProductState.Form, is CreateProductState.Creating -> {
                CreateProductForm(
                    formState = formState,
                    categories = categories,
                    isCreating = state is CreateProductState.Creating,
                    onTitleChange = viewModel::updateTitle,
                    onDescriptionChange = viewModel::updateDescription,
                    onPriceChange = viewModel::updatePrice,
                    onCategorySelect = viewModel::selectCategory,
                    onConditionSelect = viewModel::selectCondition,
                    onAddImages = { imagePickerLauncher() },
                    onRemoveImage = viewModel::removeImage,
                    onCreate = viewModel::createProduct,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is CreateProductState.Success -> {
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
 * Форма создания товара
 */
@Composable
private fun CreateProductForm(
    formState: ProductFormState,
    categories: List<info.javaway.sc.shared.domain.models.Category>,
    isCreating: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onCategorySelect: (info.javaway.sc.shared.domain.models.Category) -> Unit,
    onConditionSelect: (ProductCondition) -> Unit,
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
            placeholder = { Text("Введите название товара") },
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
            placeholder = { Text("Опишите товар") },
            isError = formState.descriptionError != null,
            supportingText = formState.descriptionError?.let { { Text(it) } },
            enabled = !isCreating,
            minLines = 4,
            maxLines = 8,
            modifier = Modifier.fillMaxWidth()
        )

        // Цена
        OutlinedTextField(
            value = formState.price,
            onValueChange = onPriceChange,
            label = { Text("Цена, ₽ *") },
            placeholder = { Text("0") },
            isError = formState.priceError != null,
            supportingText = formState.priceError?.let { { Text(it) } },
            enabled = !isCreating,
            modifier = Modifier.fillMaxWidth()
        )

        // Категория
        CategorySelectorField(
            selectedCategory = formState.category,
            categories = categories,
            categoryType = CategoryType.PRODUCT,
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

        // Состояние (NEW/USED)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Состояние *",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = formState.condition == ProductCondition.NEW,
                    onClick = { onConditionSelect(ProductCondition.NEW) },
                    label = { Text("Новое") },
                    enabled = !isCreating,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = formState.condition == ProductCondition.USED,
                    onClick = { onConditionSelect(ProductCondition.USED) },
                    label = { Text("Б/У") },
                    enabled = !isCreating,
                    modifier = Modifier.weight(1f)
                )
            }
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
            Text(if (isCreating) "Создание..." else "Создать товар")
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
    selectedImages: List<info.javaway.sc.shared.utils.SelectedImage>,
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
                                text = "Добавьте фото",
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
 * TODO: Добавить preview изображения через Coil или Skia после настройки зависимостей
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImagePreviewCard(
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
