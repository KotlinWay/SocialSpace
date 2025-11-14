package info.javaway.sc.shared.presentation.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Экран регистрации нового пользователя
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    component: RegisterComponent,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state by component.state.collectAsState()
    val phone by component.phone.collectAsState()
    val name by component.name.collectAsState()
    val email by component.email.collectAsState()
    val password by component.password.collectAsState()
    val confirmPassword by component.confirmPassword.collectAsState()

    LaunchedEffect(state) {
        if (state is RegisterState.Success) {
            onRegisterSuccess()
        }
    }

    val isLoading = state is RegisterState.Loading
    val errorMessage = (state as? RegisterState.Error)?.message

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Регистрация") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Приветствие
            Text(
                text = "Создайте аккаунт",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Заполните данные для регистрации",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Поле телефона
            OutlinedTextField(
                value = phone,
                onValueChange = { component.onPhoneChange(it) },
                label = { Text("Номер телефона *") },
                placeholder = { Text("+79991234567") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле имени
            OutlinedTextField(
                value = name,
                onValueChange = { component.onNameChange(it) },
                label = { Text("Имя *") },
                placeholder = { Text("Иван Иванов") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле email (опционально)
            OutlinedTextField(
                value = email,
                onValueChange = { component.onEmailChange(it) },
                label = { Text("Email (опционально)") },
                placeholder = { Text("example@mail.ru") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле пароля
            OutlinedTextField(
                value = password,
                onValueChange = { component.onPasswordChange(it) },
                label = { Text("Пароль *") },
                placeholder = { Text("Минимум 6 символов") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле подтверждения пароля
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { component.onConfirmPasswordChange(it) },
                label = { Text("Подтверждение пароля *") },
                placeholder = { Text("Повторите пароль") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Кнопка регистрации
            Button(
                onClick = { component.register() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Зарегистрироваться")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Кнопка перехода на вход
            TextButton(
                onClick = onNavigateToLogin,
                enabled = !isLoading
            ) {
                Text("Уже есть аккаунт? Войти")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Подсказка об обязательных полях
            Text(
                text = "* - обязательные поля",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
