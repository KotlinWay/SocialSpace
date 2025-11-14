package info.javaway.sc.shared.navigation

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import info.javaway.sc.shared.presentation.screens.auth.LoginScreen
import info.javaway.sc.shared.presentation.screens.auth.RegisterScreen

/**
 * Composable для отображения навигации между экранами
 */
@Composable
fun RootContent(component: RootComponent) {
    Children(
        stack = component.stack,
        animation = stackAnimation(fade())
    ) {
        when (val child = it.instance) {
            is RootComponent.Child.Login -> LoginScreen(
                component = child.component,
                onLoginSuccess = child.onLoginSuccess,
                onNavigateToRegister = child.onNavigateToRegister
            )
            is RootComponent.Child.Register -> RegisterScreen(
                component = child.component,
                onRegisterSuccess = child.onRegisterSuccess,
                onNavigateToLogin = child.onNavigateToLogin
            )
            is RootComponent.Child.Main -> MainContent(
                component = child.component
            )
        }
    }
}
