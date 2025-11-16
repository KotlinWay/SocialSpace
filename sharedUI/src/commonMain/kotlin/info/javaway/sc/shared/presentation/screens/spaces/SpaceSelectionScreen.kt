package info.javaway.sc.shared.presentation.screens.spaces

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import info.javaway.sc.shared.domain.models.Space
import info.javaway.sc.shared.domain.models.SpaceType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpaceSelectionScreen(
    component: SpaceSelectionComponent,
    onSpaceSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by component.state.collectAsState()
    val effect by component.effect.collectAsState()

    LaunchedEffect(effect) {
        if (effect is SpaceSelectionEffect.SpaceSelected) {
            component.consumeEffect()
            onSpaceSelected()
        }
    }

    var pendingPrivateJoin by remember { mutableStateOf<Space?>(null) }
    var inviteInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Выбор пространства") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Загружаем доступные пространства...")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                item {
                    if (uiState.error != null) {
                        ErrorCard(message = uiState.error.orEmpty(), onRetry = component::refresh)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Text(
                        text = "Выберите ЖК или создайте новое пространство.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (uiState.spaces.isEmpty()) {
                    item {
                        Text(
                            text = "Пока нет публичных пространств. Создайте новое или присоединитесь по коду.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(uiState.spaces, key = { it.id }) { space ->
                        SpaceCard(
                            space = space,
                            isJoining = uiState.isJoining,
                            onJoin = {
                                if (space.type == SpaceType.PUBLIC) {
                                    component.joinSpace(space)
                                } else {
                                    pendingPrivateJoin = space
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    CreateSpaceCard(
                        isCreating = uiState.isCreating,
                        onCreate = { name, slug, description, type, inviteCode ->
                            component.createSpace(name, slug, description, type, inviteCode)
                        }
                    )
                }
            }
        }
    }

    if (pendingPrivateJoin != null) {
        AlertDialog(
            onDismissRequest = {
                pendingPrivateJoin = null
                inviteInput = ""
            },
            title = { Text("Введите код приглашения") },
            text = {
                Column {
                    Text("Пространство: ${pendingPrivateJoin?.name}")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inviteInput,
                        onValueChange = { inviteInput = it },
                        label = { Text("Код приглашения") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingPrivateJoin?.let { space ->
                            component.joinSpace(space, inviteInput.ifBlank { null })
                        }
                        pendingPrivateJoin = null
                        inviteInput = ""
                    },
                    enabled = inviteInput.isNotBlank()
                ) {
                    Text("Присоединиться")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingPrivateJoin = null
                    inviteInput = ""
                }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRetry) {
                Text("Повторить")
            }
        }
    }
}

@Composable
private fun SpaceCard(
    space: Space,
    isJoining: Boolean,
    onJoin: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(space.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = space.description ?: "Нет описания",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Участников: ${space.membersCount}", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = if (space.type == SpaceType.PUBLIC) "Публичное" else "Приватное",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onJoin,
                enabled = !isJoining,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (space.type == SpaceType.PUBLIC) "Присоединиться" else "Ввести код")
            }
        }
    }
}

@Composable
private fun CreateSpaceCard(
    isCreating: Boolean,
    onCreate: (String, String, String?, SpaceType, String?) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var slug by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var inviteCode by rememberSaveable { mutableStateOf("") }
    var selectedType by rememberSaveable { mutableStateOf(SpaceType.PUBLIC) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Создать новое пространство", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = slug,
                onValueChange = { slug = it.lowercase() },
                label = { Text("Slug (уникальный идентификатор)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Тип пространства", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            SingleChoiceSegmentedButtonRow {
                SpaceType.values().forEach { type ->
                    SegmentedButton(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(if (type == SpaceType.PUBLIC) "Публичное" else "Приватное") },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            if (selectedType == SpaceType.PRIVATE) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it },
                    label = { Text("Код приглашения") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onCreate(
                        name.trim(),
                        slug.trim(),
                        description.ifBlank { null },
                        selectedType,
                        if (inviteCode.isBlank()) null else inviteCode.trim()
                    )
                },
                enabled = !isCreating && name.isNotBlank() && slug.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Создать")
                }
            }
        }
    }
}
