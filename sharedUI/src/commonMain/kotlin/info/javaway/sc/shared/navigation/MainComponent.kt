package info.javaway.sc.shared.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import info.javaway.sc.shared.data.api.ApiClient
import info.javaway.sc.shared.data.local.SpaceManager
import info.javaway.sc.shared.data.local.ThemeManager
import info.javaway.sc.shared.domain.repository.AuthRepository
import info.javaway.sc.shared.domain.repository.CategoryRepository
import info.javaway.sc.shared.domain.repository.ProductRepository
import info.javaway.sc.shared.domain.repository.ServiceRepository
import info.javaway.sc.shared.presentation.screens.products.create.CreateProductComponent
import info.javaway.sc.shared.presentation.screens.products.create.DefaultCreateProductComponent
import info.javaway.sc.shared.presentation.screens.products.detail.DefaultProductDetailComponent
import info.javaway.sc.shared.presentation.screens.products.detail.ProductDetailComponent
import info.javaway.sc.shared.presentation.screens.products.edit.DefaultEditProductComponent
import info.javaway.sc.shared.presentation.screens.products.edit.EditProductComponent
import info.javaway.sc.shared.presentation.screens.products.list.DefaultProductListComponent
import info.javaway.sc.shared.presentation.screens.products.list.ProductListComponent
import info.javaway.sc.shared.presentation.screens.profile.DefaultProfileComponent
import info.javaway.sc.shared.presentation.screens.profile.ProfileComponent
import info.javaway.sc.shared.presentation.screens.profile.products.DefaultMyProductsComponent
import info.javaway.sc.shared.presentation.screens.profile.products.MyProductsComponent
import info.javaway.sc.shared.presentation.screens.services.DefaultMyServicesComponent
import info.javaway.sc.shared.presentation.screens.services.EditServiceComponent
import info.javaway.sc.shared.presentation.screens.services.MyServicesComponent
import info.javaway.sc.shared.presentation.screens.services.create.CreateServiceComponent
import info.javaway.sc.shared.presentation.screens.services.create.DefaultCreateServiceComponent
import info.javaway.sc.shared.presentation.screens.services.detail.DefaultServiceDetailComponent
import info.javaway.sc.shared.presentation.screens.services.detail.ServiceDetailComponent
import info.javaway.sc.shared.presentation.screens.services.list.DefaultServiceListComponent
import info.javaway.sc.shared.presentation.screens.services.list.ServiceListComponent
import info.javaway.sc.shared.presentation.screens.services.DefaultEditServiceComponent
import info.javaway.sc.shared.presentation.screens.settings.DefaultSettingsComponent
import info.javaway.sc.shared.presentation.screens.settings.SettingsComponent
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Main компонент с Bottom Navigation (Товары, Услуги, Профиль)
 */
class MainComponent(
    componentContext: ComponentContext,
    private val onLogout: () -> Unit,
    private val onSwitchSpace: () -> Unit
) : ComponentContext by componentContext, KoinComponent {

    private val productRepository: ProductRepository by inject()
    private val serviceRepository: ServiceRepository by inject()
    private val categoryRepository: CategoryRepository by inject()
    private val authRepository: AuthRepository by inject()
    private val apiClient: ApiClient by inject()
    private val spaceManager: SpaceManager by inject()
    private val themeManager: ThemeManager by inject()

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
                component = DefaultProductListComponent(
                    componentContext = componentContext,
                    productRepository = productRepository
                ),
                onProductClick = { productId ->
                    navigation.pushNew(Config.ProductDetail(productId))
                },
                onCreateProduct = {
                    navigation.pushNew(Config.CreateProduct)
                }
            )
            is Config.ProductDetail -> Child.ProductDetail(
                component = DefaultProductDetailComponent(
                    componentContext = componentContext,
                    productRepository = productRepository,
                    authRepository = authRepository,
                    productId = config.productId
                ),
                onBack = { navigation.pop() },
                onEditProduct = { productId ->
                    navigation.pushNew(Config.EditProduct(productId))
                }
            )
            is Config.CreateProduct -> Child.CreateProduct(
                component = DefaultCreateProductComponent(
                    componentContext = componentContext,
                    apiClient = apiClient,
                    categoryRepository = categoryRepository,
                    spaceManager = spaceManager
                ),
                onBack = { navigation.pop() },
                onSuccess = { productId ->
                    navigation.pop()
                    navigation.pushNew(Config.ProductDetail(productId))
                }
            )
            is Config.Services -> Child.Services(
                component = DefaultServiceListComponent(
                    componentContext = componentContext,
                    serviceRepository = serviceRepository
                ),
                onServiceClick = { serviceId ->
                    navigation.pushNew(Config.ServiceDetail(serviceId))
                },
                onCreateService = {
                    navigation.pushNew(Config.CreateService)
                }
            )
            is Config.ServiceDetail -> Child.ServiceDetail(
                component = DefaultServiceDetailComponent(
                    componentContext = componentContext,
                    serviceRepository = serviceRepository,
                    authRepository = authRepository,
                    serviceId = config.serviceId
                ),
                onBack = { navigation.pop() },
                onEditService = { serviceId ->
                    navigation.pushNew(Config.EditService(serviceId))
                }
            )
            is Config.CreateService -> Child.CreateService(
                component = DefaultCreateServiceComponent(
                    componentContext = componentContext,
                    apiClient = apiClient,
                    categoryRepository = categoryRepository,
                    spaceManager = spaceManager
                ),
                onBack = { navigation.pop() },
                onSuccess = { serviceId ->
                    navigation.pop()
                    navigation.pushNew(Config.ServiceDetail(serviceId))
                }
            )
            is Config.Profile -> Child.Profile(
                component = DefaultProfileComponent(
                    componentContext = componentContext,
                    authRepository = authRepository
                ),
                onLogout = onLogout,
                onMyProductsClick = {
                    navigation.pushNew(Config.MyProducts)
                },
                onMyServicesClick = {
                    navigation.pushNew(Config.MyServices)
                },
                onSwitchSpace = onSwitchSpace,
                onOpenSettings = {
                    navigation.pushNew(Config.Settings)
                }
            )
            is Config.MyProducts -> Child.MyProducts(
                component = DefaultMyProductsComponent(
                    componentContext = componentContext,
                    productRepository = productRepository
                ),
                onBack = { navigation.pop() },
                onProductClick = { productId ->
                    navigation.pushNew(Config.ProductDetail(productId))
                },
                onEditProduct = { productId ->
                    navigation.pushNew(Config.EditProduct(productId))
                },
                onCreateProduct = {
                    navigation.pushNew(Config.CreateProduct)
                }
            )
            is Config.EditProduct -> Child.EditProduct(
                component = DefaultEditProductComponent(
                    componentContext = componentContext,
                    productId = config.productId,
                    productRepository = productRepository,
                    apiClient = apiClient,
                    categoryRepository = categoryRepository
                ),
                onBack = { navigation.pop() },
                onSuccess = { productId ->
                    navigation.pop()
                    navigation.pushNew(Config.ProductDetail(productId))
                }
            )
            is Config.MyServices -> Child.MyServices(
                component = DefaultMyServicesComponent(
                    componentContext = componentContext,
                    serviceRepository = serviceRepository
                ),
                onBack = { navigation.pop() },
                onServiceClick = { serviceId ->
                    navigation.pushNew(Config.ServiceDetail(serviceId))
                },
                onEditService = { serviceId ->
                    navigation.pushNew(Config.EditService(serviceId))
                },
                onCreateService = {
                    navigation.pushNew(Config.CreateService)
                }
            )
            is Config.EditService -> Child.EditService(
                component = DefaultEditServiceComponent(
                    componentContext = componentContext,
                    serviceId = config.serviceId,
                    serviceRepository = serviceRepository,
                    apiClient = apiClient,
                    categoryRepository = categoryRepository
                ),
                onBack = { navigation.pop() },
                onSuccess = { serviceId ->
                    navigation.pop()
                    navigation.pushNew(Config.ServiceDetail(serviceId))
                }
            )
            is Config.Settings -> Child.Settings(
                component = DefaultSettingsComponent(
                    componentContext = componentContext,
                    themeManager = themeManager
                ),
                onBack = { navigation.pop() }
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
            val component: ProductListComponent,
            val onProductClick: (Long) -> Unit,
            val onCreateProduct: () -> Unit
        ) : Child()

        data class ProductDetail(
            val component: ProductDetailComponent,
            val onBack: () -> Unit,
            val onEditProduct: (Long) -> Unit
        ) : Child()

        data class CreateProduct(
            val component: CreateProductComponent,
            val onBack: () -> Unit,
            val onSuccess: (Long) -> Unit
        ) : Child()

        data class Services(
            val component: ServiceListComponent,
            val onServiceClick: (Long) -> Unit,
            val onCreateService: () -> Unit
        ) : Child()

        data class ServiceDetail(
            val component: ServiceDetailComponent,
            val onBack: () -> Unit,
            val onEditService: (Long) -> Unit
        ) : Child()

        data class CreateService(
            val component: CreateServiceComponent,
            val onBack: () -> Unit,
            val onSuccess: (Long) -> Unit
        ) : Child()

        data class Profile(
            val component: ProfileComponent,
            val onLogout: () -> Unit,
            val onMyProductsClick: () -> Unit,
            val onMyServicesClick: () -> Unit,
            val onSwitchSpace: () -> Unit,
            val onOpenSettings: () -> Unit
        ) : Child()

        data class MyProducts(
            val component: MyProductsComponent,
            val onBack: () -> Unit,
            val onProductClick: (Long) -> Unit,
            val onEditProduct: (Long) -> Unit,
            val onCreateProduct: () -> Unit
        ) : Child()

        data class EditProduct(
            val component: EditProductComponent,
            val onBack: () -> Unit,
            val onSuccess: (Long) -> Unit
        ) : Child()

        data class MyServices(
            val component: MyServicesComponent,
            val onBack: () -> Unit,
            val onServiceClick: (Long) -> Unit,
            val onEditService: (Long) -> Unit,
            val onCreateService: () -> Unit
        ) : Child()

        data class EditService(
            val component: EditServiceComponent,
            val onBack: () -> Unit,
            val onSuccess: (Long) -> Unit
        ) : Child()

        data class Settings(
            val component: SettingsComponent,
            val onBack: () -> Unit
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

        @Serializable
        data class EditProduct(val productId: Long) : Config

        @Serializable
        data object MyServices : Config

        @Serializable
        data class EditService(val serviceId: Long) : Config

        @Serializable
        data object Settings : Config
    }
}
