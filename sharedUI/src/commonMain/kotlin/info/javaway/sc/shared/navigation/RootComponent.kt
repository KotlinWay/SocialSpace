package info.javaway.sc.shared.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import info.javaway.sc.shared.data.local.TokenManager
import kotlinx.serialization.Serializable

/**
 * Root компонент для навигации (Decompose)
 */
class RootComponent(
    componentContext: ComponentContext,
    private val tokenManager: TokenManager
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    val stack: Value<ChildStack<*, Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = if (tokenManager.getToken() != null) {
                Config.Main
            } else {
                Config.Login
            },
            handleBackButton = true,
            childFactory = ::child,
        )

    private fun child(config: Config, componentContext: ComponentContext): Child =
        when (config) {
            is Config.Login -> Child.Login(
                onNavigateToRegister = { navigation.push(Config.Register) },
                onLoginSuccess = { navigation.replaceCurrent(Config.Main) }
            )
            is Config.Register -> Child.Register(
                onNavigateToLogin = { navigation.pop() },
                onRegisterSuccess = { navigation.replaceCurrent(Config.Main) }
            )
            is Config.Main -> Child.Main(
                component = MainComponent(
                    componentContext = componentContext,
                    onLogout = {
                        tokenManager.clear()
                        navigation.replaceCurrent(Config.Login)
                    }
                )
            )
        }

    sealed class Child {
        data class Login(
            val onNavigateToRegister: () -> Unit,
            val onLoginSuccess: () -> Unit
        ) : Child()

        data class Register(
            val onNavigateToLogin: () -> Unit,
            val onRegisterSuccess: () -> Unit
        ) : Child()

        data class Main(
            val component: MainComponent
        ) : Child()
    }

    @Serializable
    sealed interface Config {
        @Serializable
        data object Login : Config

        @Serializable
        data object Register : Config

        @Serializable
        data object Main : Config
    }
}
