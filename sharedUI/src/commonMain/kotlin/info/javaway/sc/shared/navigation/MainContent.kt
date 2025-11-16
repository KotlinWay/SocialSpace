package info.javaway.sc.shared.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import info.javaway.sc.shared.presentation.screens.products.create.CreateProductScreen
import info.javaway.sc.shared.presentation.screens.products.edit.EditProductScreen
import info.javaway.sc.shared.presentation.screens.profile.products.MyProductsScreen
import info.javaway.sc.shared.presentation.screens.products.detail.ProductDetailScreen
import info.javaway.sc.shared.presentation.screens.products.list.ProductListScreen
import info.javaway.sc.shared.presentation.screens.profile.ProfileScreen
import info.javaway.sc.shared.presentation.screens.services.create.CreateServiceScreen
import info.javaway.sc.shared.presentation.screens.services.detail.ServiceDetailScreen
import info.javaway.sc.shared.presentation.screens.services.list.ServiceListScreen
import info.javaway.sc.shared.presentation.screens.services.EditServiceScreen
import info.javaway.sc.shared.presentation.screens.services.MyServicesScreen
import info.javaway.sc.shared.utils.PhoneDialer
import info.javaway.sc.shared.presentation.screens.settings.SettingsScreen
import org.koin.compose.koinInject

/**
 * Main контент с Bottom Navigation
 */
@Composable
fun MainContent(
    component: MainComponent,
    modifier: Modifier = Modifier
) {
    val stack by component.stack.subscribeAsState()
    val activeChild = stack.active.instance

    // Inject PhoneDialer from Koin
    val phoneDialer: PhoneDialer = koinInject()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                BottomNavItem.entries.forEach { item ->
                    val isSelected = when (item) {
                        BottomNavItem.PRODUCTS -> activeChild is MainComponent.Child.Products
                        BottomNavItem.SERVICES -> activeChild is MainComponent.Child.Services
                        BottomNavItem.PROFILE -> activeChild is MainComponent.Child.Profile
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            when (item) {
                                BottomNavItem.PRODUCTS -> component.onProductsTabClicked()
                                BottomNavItem.SERVICES -> component.onServicesTabClicked()
                                BottomNavItem.PROFILE -> component.onProfileTabClicked()
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Children(
                stack = component.stack
            ) {
                when (val child = it.instance) {
                    is MainComponent.Child.Products -> {
                        ProductListScreen(
                            component = child.component,
                            onProductClick = child.onProductClick,
                            onCreateProduct = child.onCreateProduct
                        )
                    }
                    is MainComponent.Child.ProductDetail -> {
                        ProductDetailScreen(
                            component = child.component,
                            onBack = child.onBack,
                            onCallSeller = { phone ->
                                phoneDialer.dial(phone)
                            },
                            onEditProduct = child.onEditProduct
                        )
                    }
                    is MainComponent.Child.CreateProduct -> {
                        CreateProductScreen(
                            component = child.component,
                            onBack = child.onBack,
                            onSuccess = child.onSuccess
                        )
                    }
                    is MainComponent.Child.Services -> {
                        ServiceListScreen(
                            component = child.component,
                            onServiceClick = child.onServiceClick,
                            onCreateService = child.onCreateService
                        )
                    }
                    is MainComponent.Child.ServiceDetail -> {
                        ServiceDetailScreen(
                            component = child.component,
                            onBack = child.onBack,
                            onCallProvider = { phone ->
                                phoneDialer.dial(phone)
                            },
                            onEditService = child.onEditService
                        )
                    }
                    is MainComponent.Child.CreateService -> {
                        CreateServiceScreen(
                            component = child.component,
                            onBack = child.onBack,
                            onSuccess = child.onSuccess
                        )
                    }
                    is MainComponent.Child.Profile -> {
                        ProfileScreen(
                            component = child.component,
                            onLogout = child.onLogout,
                            onMyProductsClick = child.onMyProductsClick,
                            onMyServicesClick = child.onMyServicesClick,
                            onSwitchSpace = child.onSwitchSpace,
                            onOpenSettings = child.onOpenSettings
                        )
                    }
                    is MainComponent.Child.MyProducts -> {
                        MyProductsScreen(
                            component = child.component,
                            onBack = child.onBack,
                            onProductClick = child.onProductClick,
                            onEditProduct = child.onEditProduct,
                            onCreateProduct = child.onCreateProduct
                        )
                    }
                    is MainComponent.Child.EditProduct -> {
                        EditProductScreen(
                            component = child.component,
                            onBack = child.onBack,
                            onSuccess = child.onSuccess
                        )
                    }
                    is MainComponent.Child.MyServices -> {
                        MyServicesScreen(
                            component = child.component,
                            onBack = child.onBack,
                            onServiceClick = child.onServiceClick,
                            onEditService = child.onEditService,
                            onCreateService = child.onCreateService
                        )
                    }
                    is MainComponent.Child.EditService -> {
                        EditServiceScreen(
                            component = child.component,
                            onBack = child.onBack,
                            onSuccess = child.onSuccess
                        )
                    }
                    is MainComponent.Child.Settings -> {
                        SettingsScreen(
                            component = child.component,
                            onBack = child.onBack
                        )
                    }
                }
            }
        }
    }
}

/**
 * Элементы Bottom Navigation
 */
private enum class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    PRODUCTS(
        title = "Товары",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    SERVICES(
        title = "Услуги",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    ),
    PROFILE(
        title = "Профиль",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
}
