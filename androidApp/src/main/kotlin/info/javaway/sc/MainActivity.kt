package info.javaway.sc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arkivanov.decompose.defaultComponentContext
import info.javaway.sc.shared.App
import info.javaway.sc.shared.data.local.TokenManager
import info.javaway.sc.shared.navigation.RootComponent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Создаем ComponentContext для Decompose
        val componentContext = defaultComponentContext()

        // Создаем TokenManager для проверки токена
        val tokenManager = TokenManager(Settings())

        // Создаем RootComponent
        val rootComponent = RootComponent(
            componentContext = componentContext,
            tokenManager = tokenManager
        )

        setContent {
            App(rootComponent = rootComponent)
        }
    }
}
