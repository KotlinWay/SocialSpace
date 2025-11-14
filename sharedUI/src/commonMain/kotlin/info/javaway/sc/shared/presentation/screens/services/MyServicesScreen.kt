package info.javaway.sc.shared.presentation.screens.services

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import info.javaway.sc.shared.domain.models.Service
import info.javaway.sc.shared.presentation.components.ServiceCard
import org.koin.compose.koinInject

/**
 * Экран "Мои услуги"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyServicesScreen(
    onBack: () -> Unit,
    onServiceClick: (Long) -> Unit,
    onEditService: (Long) -> Unit,
    onCreateService: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyServicesViewModel = koinInject()
) {
    // Очистка ViewModel при уничтожении экрана
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onCleared()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Мои услуги") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateService,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Создать объявление"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = viewModel.state) {
                is MyServicesState.Loading -> {
                    LoadingContent()
                }
                is MyServicesState.Success -> {
                    MyServicesContent(
                        services = state.services,
                        onServiceClick = onServiceClick,
                        onEditService = onEditService,
                        onDeleteService = { serviceId ->
                            viewModel.deleteService(serviceId)
                        }
                    )
                }
                is MyServicesState.Empty -> {
                    EmptyContent(onCreateService = onCreateService)
                }
                is MyServicesState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.loadMyServices() }
                    )
                }
            }
        }
    }
}

/**
 * Контент списка услуг
 */
@Composable
private fun MyServicesContent(
    services: List<Service>,
    onServiceClick: (Long) -> Unit,
    onEditService: (Long) -> Unit,
    onDeleteService: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = services,
            key = { it.id }
        ) { service ->
            MyServiceCard(
                service = service,
                onServiceClick = { onServiceClick(service.id) },
                onEditClick = { onEditService(service.id) },
                onDeleteClick = { onDeleteService(service.id) }
            )
        }
    }
}

/**
 * Карточка своей услуги с кнопками управления
 */
@Composable
private fun MyServiceCard(
    service: Service,
    onServiceClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Используем существующий ServiceCard для отображения услуги
            ServiceCard(
                service = service,
                onClick = onServiceClick,
                modifier = Modifier.fillMaxWidth()
            )

            // Кнопки управления
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                // Кнопка "Редактировать"
                OutlinedButton(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Редактировать")
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Кнопка "Удалить"
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Удалить")
                }
            }
        }
    }

    // Диалог подтверждения удаления
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            serviceTitle = service.title,
            onConfirm = {
                showDeleteDialog = false
                onDeleteClick()
            },
            onDismiss = {
                showDeleteDialog = false
            }
        )
    }
}

/**
 * Диалог подтверждения удаления
 */
@Composable
private fun DeleteConfirmationDialog(
    serviceTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Удалить услугу?")
        },
        text = {
            Text("Вы уверены, что хотите удалить услугу \"$serviceTitle\"? Это действие нельзя отменить.")
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Удалить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

/**
 * Контент пустого списка
 */
@Composable
private fun EmptyContent(
    onCreateService: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "🔧",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "У вас пока нет услуг",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Создайте первое объявление об услуге, чтобы начать работать",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onCreateService) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Создать услугу")
            }
        }
    }
}

/**
 * Индикатор загрузки
 */
@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Загрузка услуг...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Контент ошибки
 */
@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Ошибка",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Повторить")
            }
        }
    }
}
