package info.javaway.sc.shared.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import info.javaway.sc.api.models.Category
import info.javaway.sc.api.models.CategoryType

/**
 * Селектор категории с диалогом
 *
 * @param selectedCategory Выбранная категория (может быть null)
 * @param categories Список доступных категорий
 * @param categoryType Тип категорий (PRODUCT или SERVICE)
 * @param onCategorySelected Callback при выборе категории
 * @param modifier Modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectorField(
    selectedCategory: Category?,
    categories: List<Category>,
    categoryType: CategoryType,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }

    // Фильтруем категории по типу
    val filteredCategories = remember(categories, categoryType) {
        categories.filter { it.type == categoryType }
    }

    OutlinedTextField(
        value = selectedCategory?.name ?: "",
        onValueChange = { /* Read-only */ },
        label = { Text("Категория") },
        placeholder = { Text("Выберите категорию") },
        readOnly = true,
        enabled = enabled,
        leadingIcon = {
            if (selectedCategory?.icon != null) {
                Text(
                    text = selectedCategory.icon,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        trailingIcon = {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ArrowDropDown,
                contentDescription = "Выбрать категорию"
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { showDialog = true },
        colors = OutlinedTextFieldDefaults.colors()
    )

    if (showDialog) {
        CategorySelectorDialog(
            categories = filteredCategories,
            selectedCategory = selectedCategory,
            onCategorySelected = { category ->
                onCategorySelected(category)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

/**
 * Диалог выбора категории
 */
@Composable
private fun CategorySelectorDialog(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Выберите категорию") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    CategoryItem(
                        category = category,
                        isSelected = category.id == selectedCategory?.id,
                        onClick = { onCategorySelected(category) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

/**
 * Элемент категории в списке
 */
@Composable
private fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка категории
            if (category.icon != null) {
                Text(
                    text = category.icon,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            // Название категории
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Галочка для выбранной категории
            if (isSelected) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Check,
                    contentDescription = "Выбрано",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
