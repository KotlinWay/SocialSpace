package info.javaway.sc.shared.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
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
            is Config.Products -> Child.Products(
                onProductClick = { productId ->
                    navigation.push(Config.ProductDetail(productId))
                },
                onCreateProduct = {
                    navigation.push(Config.CreateProduct)
                }
            )
            is Config.ProductDetail -> Child.ProductDetail(
                productId = config.productId,
                onBack = { navigation.pop() }
            )
            is Config.CreateProduct -> Child.CreateProduct(
                onBack = { navigation.pop() },
                onSuccess = { productId ->
                    // После создания товара возвращаемся на список и открываем детали
                    navigation.pop()
                    navigation.push(Config.ProductDetail(productId))
                }
            )
            is Config.Services -> Child.Services(
                onServiceClick = { serviceId ->
                    navigation.push(Config.ServiceDetail(serviceId))
                },
                onCreateService = {
                    navigation.push(Config.CreateService)
                }
            )
            is Config.ServiceDetail -> Child.ServiceDetail(
                serviceId = config.serviceId,
                onBack = { navigation.pop() }
            )
            is Config.CreateService -> Child.CreateService(
                onBack = { navigation.pop() },
                onSuccess = { serviceId ->
                    // После создания услуги возвращаемся на список и открываем детали
                    navigation.pop()
                    navigation.push(Config.ServiceDetail(serviceId))
                }
            )
            is Config.Profile -> Child.Profile(
                onLogout = onLogout,
                onMyProductsClick = {
                    navigation.push(Config.MyProducts)
                }
            )
            is Config.MyProducts -> Child.MyProducts(
                onBack = { navigation.pop() },
                onProductClick = { productId ->
                    navigation.push(Config.ProductDetail(productId))
                },
                onEditProduct = { productId ->
                    // TODO: Этап 13.3 - EditProductScreen
                    println("Edit product clicked: $productId")
                },
                onCreateProduct = {
                    navigation.push(Config.CreateProduct)
                }
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
        data class Products(
            val onProductClick: (Long) -> Unit,
            val onCreateProduct: () -> Unit
        ) : Child()

        data class ProductDetail(
            val productId: Long,
            val onBack: () -> Unit
        ) : Child()

        data class CreateProduct(
            val onBack: () -> Unit,
            val onSuccess: (Long) -> Unit
        ) : Child()

        data class Services(
            val onServiceClick: (Long) -> Unit,
            val onCreateService: () -> Unit
        ) : Child()

        data class ServiceDetail(
            val serviceId: Long,
            val onBack: () -> Unit
        ) : Child()

        data class CreateService(
            val onBack: () -> Unit,
            val onSuccess: (Long) -> Unit
        ) : Child()

        data class Profile(
            val onLogout: () -> Unit,
            val onMyProductsClick: () -> Unit
        ) : Child()

        data class MyProducts(
            val onBack: () -> Unit,
            val onProductClick: (Long) -> Unit,
            val onEditProduct: (Long) -> Unit,
            val onCreateProduct: () -> Unit
        ) : Child()
    }

    @Serializable
    sealed interface Config {
        @Serializable
        data object Products : Config

        @Serializable
        data class ProductDetail(val productId: Long) : Config

        @Serializable
        data object CreateProduct : Config

        @Serializable
        data object Services : Config

        @Serializable
        data class ServiceDetail(val serviceId: Long) : Config

        @Serializable
        data object CreateService : Config

        @Serializable
        data object Profile : Config

        @Serializable
        data object MyProducts : Config
    }
}
