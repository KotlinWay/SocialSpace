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
import info.javaway.sc.shared.presentation.screens.home.HomeScreen
import info.javaway.sc.shared.presentation.screens.products.ProductListScreen

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
                        ProductListScreen()
                    }
                    is MainComponent.Child.Services -> {
                        // TODO: Заменить на ServiceListScreen
                        PlaceholderScreen("Услуги")
                    }
                    is MainComponent.Child.Profile -> {
                        // Переиспользуем HomeScreen для профиля (временно)
                        HomeScreen(onLogout = child.onLogout)
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
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
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
