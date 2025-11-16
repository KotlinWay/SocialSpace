package info.javaway.sc.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import info.javaway.sc.shared.data.local.ThemeManager
import info.javaway.sc.shared.navigation.RootComponent
import info.javaway.sc.shared.navigation.RootContent
import info.javaway.sc.shared.presentation.theme.SocialSpaceTheme

@Composable
fun App(
    rootComponent: RootComponent,
    themeManager: ThemeManager
) {
    val themeMode by themeManager.themeModeFlow.collectAsState()

    SocialSpaceTheme(themeMode = themeMode) {
        RootContent(component = rootComponent)
    }
}
