package info.javaway.sc.shared

import androidx.compose.runtime.Composable
import info.javaway.sc.shared.di.appModules
import info.javaway.sc.shared.presentation.screens.auth.LoginScreen
import info.javaway.sc.shared.presentation.theme.SocialSpaceTheme
import org.koin.compose.KoinApplication

@Composable
fun App() {
    KoinApplication(application = {
        modules(appModules)
    }) {
        SocialSpaceTheme {
            LoginScreen(
                onLoginSuccess = {
                    // TODO: Навигация на главный экран
                },
                onNavigateToRegister = {
                    // TODO: Навигация на экран регистрации
                }
            )
        }
    }
}
