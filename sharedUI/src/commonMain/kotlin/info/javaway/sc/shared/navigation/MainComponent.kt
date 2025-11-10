package info.javaway.sc.shared.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable

/**
 * Main компонент с Bottom Navigation (Товары, Услуги, Профиль)
 */
class MainComponent(
    componentContext: ComponentContext,
    private val onLogout: () -> Unit
) : ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    val stack: Value<ChildStack<*, Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Products,
            handleBackButton = true,
            childFactory = ::child,
        )

    private fun child(config: Config, componentContext: ComponentContext): Child =
        when (config) {
            is Config.Products -> Child.Products
            is Config.Services -> Child.Services
            is Config.Profile -> Child.Profile(
                onLogout = onLogout
            )
        }

    // Навигация между вкладками
    fun onProductsTabClicked() {
        navigation.bringToFront(Config.Products)
    }

    fun onServicesTabClicked() {
        navigation.bringToFront(Config.Services)
    }

    fun onProfileTabClicked() {
        navigation.bringToFront(Config.Profile)
    }

    sealed class Child {
        data object Products : Child()
        data object Services : Child()
        data class Profile(
            val onLogout: () -> Unit
        ) : Child()
    }

    @Serializable
    sealed interface Config {
        @Serializable
        data object Products : Config

        @Serializable
        data object Services : Config

        @Serializable
        data object Profile : Config
    }
}
