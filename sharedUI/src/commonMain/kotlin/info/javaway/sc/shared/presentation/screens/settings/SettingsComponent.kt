package info.javaway.sc.shared.presentation.screens.settings

import com.arkivanov.decompose.ComponentContext
import info.javaway.sc.shared.data.local.ThemeManager
import info.javaway.sc.shared.domain.models.ThemeMode
import info.javaway.sc.shared.presentation.core.BaseComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface SettingsComponent {
    val state: StateFlow<SettingsUiState>
    fun selectTheme(mode: ThemeMode)
}

class DefaultSettingsComponent(
    componentContext: ComponentContext,
    private val themeManager: ThemeManager
) : BaseComponent(componentContext), SettingsComponent {

    private val _state = MutableStateFlow(SettingsUiState(themeManager.getThemeMode()))
    override val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        componentScope.launch {
            themeManager.themeModeFlow.collect { mode ->
                _state.value = SettingsUiState(mode)
            }
        }
    }

    override fun selectTheme(mode: ThemeMode) {
        themeManager.setThemeMode(mode)
    }
}

data class SettingsUiState(val themeMode: ThemeMode)
