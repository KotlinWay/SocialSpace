package info.javaway.sc.shared.presentation.core

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Базовый класс для Decompose-компонентов, который создает coroutineScope,
 * автоматически отменяемый при уничтожении компонента.
 */
abstract class BaseComponent(
    componentContext: ComponentContext
) : ComponentContext by componentContext {

    protected val componentScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        lifecycle.doOnDestroy { componentScope.cancel() }
    }
}
