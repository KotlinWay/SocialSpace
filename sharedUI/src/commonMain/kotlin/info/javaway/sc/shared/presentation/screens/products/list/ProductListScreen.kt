package info.javaway.sc.shared.presentation.screens.products.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import info.javaway.sc.shared.domain.models.Product
import info.javaway.sc.shared.presentation.components.ProductCard

/**
 * Экран списка товаров с Paging 3
 */
@Composable
fun ProductListScreen(
    component: ProductListComponent,
    onProductClick: (Long) -> Unit,
    onCreateProduct: () -> Unit
) {
    val lazyPagingItems: LazyPagingItems<Product> = component.productsFlow.collectAsLazyPagingItems()

    ProductListContent(
        lazyPagingItems = lazyPagingItems,
        onProductClick = onProductClick,
        onCreateProduct = onCreateProduct
    )
}

/**
 * Контент списка товаров с обработкой состояний Paging 3
 */
@Composable
private fun ProductListContent(
    lazyPagingItems: LazyPagingItems<Product>,
    onProductClick: (Long) -> Unit,
    onCreateProduct: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateProduct) {
                Icon(Icons.Default.Add, contentDescription = "Создать товар")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        // Обработка состояний загрузки
        when {
            // Первая загрузка (loading)
            lazyPagingItems.loadState.refresh is LoadState.Loading -> {
                LoadingState()
            }

            // Ошибка при первой загрузке
            lazyPagingItems.loadState.refresh is LoadState.Error -> {
                val error = lazyPagingItems.loadState.refresh as LoadState.Error
                ErrorState(
                    message = error.error.message ?: "Ошибка загрузки товаров",
                    onRetry = { lazyPagingItems.retry() }
                )
            }

            // Пустой список
            lazyPagingItems.itemCount == 0 -> {
                EmptyState(
                    onRefresh = { lazyPagingItems.refresh() }
                )
            }

            // Список товаров
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Товары
                    items(
                        count = lazyPagingItems.itemCount,
                        key = { index ->
                            lazyPagingItems[index]?.id ?: index
                        }
                    ) { index ->
                        val product = lazyPagingItems[index]
                        if (product != null) {
                            ProductCard(
                                product = product,
                                onClick = { onProductClick(product.id) }
                            )
                        }
                    }

                    // Loading indicator для пагинации (append)
                    if (lazyPagingItems.loadState.append is LoadState.Loading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    // Ошибка при загрузке следующей страницы
                    if (lazyPagingItems.loadState.append is LoadState.Error) {
                        item {
                            val error = lazyPagingItems.loadState.append as LoadState.Error
                            AppendErrorItem(
                                message = error.error.message ?: "Ошибка загрузки",
                                onRetry = { lazyPagingItems.retry() }
                            )
                        }
                    }
                }
            }
        }
        }
    }
}

/**
 * Состояние загрузки (первая загрузка)
 */
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Состояние ошибки
 */
@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Ошибка загрузки",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Button(onClick = onRetry) {
                Text("Повторить")
            }
        }
    }
}

/**
 * Пустое состояние
 */
@Composable
private fun EmptyState(
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Товаров пока нет",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Будьте первым, кто разместит объявление!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

/**
 * Ошибка при загрузке следующей страницы (append)
 */
@Composable
private fun AppendErrorItem(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) {
                Text("Повторить")
            }
        }
    }
}
