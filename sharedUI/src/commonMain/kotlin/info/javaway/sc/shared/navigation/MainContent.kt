package info.javaway.sc.shared.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import info.javaway.sc.shared.presentation.screens.products.CreateProductScreen
import info.javaway.sc.shared.presentation.screens.products.ProductDetailScreen
import info.javaway.sc.shared.presentation.screens.products.ProductListScreen
import info.javaway.sc.shared.presentation.screens.profile.ProfileScreen
import info.javaway.sc.shared.presentation.screens.services.ServiceDetailScreen
import info.javaway.sc.shared.presentation.screens.services.ServiceListScreen
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

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
                            onProductClick = child.onProductClick,
                            onCreateProduct = child.onCreateProduct
                        )
                    }
                    is MainComponent.Child.ProductDetail -> {
                        ProductDetailScreen(
                            productId = child.productId,
                            onBack = child.onBack,
                            onCallSeller = { phone ->
                                // TODO: Реализовать в Android через Intent.ACTION_DIAL
                                println("Call seller: $phone")
                            }
                        )
                    }
                    is MainComponent.Child.CreateProduct -> {
                        val viewModel = koinInject<info.javaway.sc.shared.presentation.screens.products.CreateProductViewModel>()
                        CreateProductScreen(
                            viewModel = viewModel,
                            onBack = child.onBack,
                            onSuccess = child.onSuccess
                        )
                    }
                    is MainComponent.Child.Services -> {
                        ServiceListScreen(
                            onServiceClick = child.onServiceClick,
                            onCreateService = child.onCreateService
                        )
                    }
                    is MainComponent.Child.ServiceDetail -> {
                        ServiceDetailScreen(
                            serviceId = child.serviceId,
                            onBack = child.onBack,
                            onCallProvider = { phone ->
                                // TODO: Реализовать в Android через Intent.ACTION_DIAL
                                println("Call provider: $phone")
                            }
                        )
                    }
                    is MainComponent.Child.CreateService -> {
                        // TODO: CreateServiceScreen после реализации CreateServiceViewModel
                        PlaceholderScreen("Создание услуги (в разработке)")
                    }
                    is MainComponent.Child.Profile -> {
                        ProfileScreen(onLogout = child.onLogout)
                    }
                }
            }
        }
    }
}

/**
 * Временный экран-заглушка
 */
@Composable
private fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                text = "Экран: $title",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "(В разработке)",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
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
