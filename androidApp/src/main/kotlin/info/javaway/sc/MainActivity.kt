package info.javaway.sc

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.arkivanov.decompose.defaultComponentContext
import info.javaway.sc.shared.App
import info.javaway.sc.shared.data.local.ThemeManager
import info.javaway.sc.shared.data.local.TokenManager
import info.javaway.sc.shared.di.appModules
import info.javaway.sc.shared.navigation.RootComponent
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Application класс для инициализации Koin
 */
class SocialSpaceApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Инициализируем Koin
        startKoin {
            androidContext(this@SocialSpaceApp)
            modules(appModules)
        }
    }
}

class MainActivity : ComponentActivity() {

    // Получаем TokenManager из Koin
    private val tokenManager: TokenManager by inject()
    private val themeManager: ThemeManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Napier.base(DebugAntilog())
        // Создаем ComponentContext для Decompose
        val componentContext = defaultComponentContext()

        // Создаем RootComponent с TokenManager из DI
        val rootComponent = RootComponent(
            componentContext = componentContext,
            tokenManager = tokenManager
        )

        setContent {
            App(
                rootComponent = rootComponent,
                themeManager = themeManager
            )
        }
    }
}
