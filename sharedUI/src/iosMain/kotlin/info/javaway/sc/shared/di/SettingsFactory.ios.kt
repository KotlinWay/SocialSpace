package info.javaway.sc.shared.di

import com.russhwolf.settings.Settings

/**
 * iOS реализация Settings через NSUserDefaults
 * Использует Settings() по умолчанию, который автоматически
 * использует NSUserDefaults.standard
 */
actual fun createSettings(): Settings {
    return Settings()
}
