package info.javaway.sc.shared

import androidx.compose.runtime.Composable
import info.javaway.sc.shared.di.appModules
import info.javaway.sc.shared.navigation.RootComponent
import info.javaway.sc.shared.navigation.RootContent
import info.javaway.sc.shared.presentation.theme.SocialSpaceTheme
import org.koin.compose.KoinApplication

@Composable
fun App(rootComponent: RootComponent) {
    KoinApplication(application = {
        modules(appModules)
    }) {
        SocialSpaceTheme {
            RootContent(component = rootComponent)
        }
    }
}
