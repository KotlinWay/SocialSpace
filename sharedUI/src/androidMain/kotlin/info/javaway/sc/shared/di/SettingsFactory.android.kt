package info.javaway.sc.shared.di

import com.russhwolf.settings.Settings

/**
 * Android реализация Settings через SharedPreferences
 * Использует Settings() по умолчанию, который автоматически
 * создает SharedPreferences с именем по умолчанию
 */
actual fun createSettings(): Settings {
    return Settings()
}
