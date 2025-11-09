package info.javaway.sc.shared

import androidx.compose.runtime.Composable
import info.javaway.sc.shared.navigation.RootComponent
import info.javaway.sc.shared.navigation.RootContent
import info.javaway.sc.shared.presentation.theme.SocialSpaceTheme

@Composable
fun App(rootComponent: RootComponent) {
    SocialSpaceTheme {
        RootContent(component = rootComponent)
    }
}
