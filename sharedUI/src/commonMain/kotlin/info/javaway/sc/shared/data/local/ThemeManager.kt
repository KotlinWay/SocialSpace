package info.javaway.sc.shared.data.local

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import info.javaway.sc.shared.domain.models.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Хранит выбранный пользователем режим темы и уведомляет подписчиков об изменениях.
 */
class ThemeManager(private val settings: Settings) {

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
    }

    private val _themeMode = MutableStateFlow(readPersistedMode())
    val themeModeFlow: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun getThemeMode(): ThemeMode = _themeMode.value

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        settings[KEY_THEME_MODE] = mode.name
    }

    private fun readPersistedMode(): ThemeMode {
        val stored = settings.getStringOrNull(KEY_THEME_MODE) ?: return ThemeMode.SYSTEM
        return runCatching { ThemeMode.valueOf(stored) }.getOrDefault(ThemeMode.SYSTEM)
    }
}
