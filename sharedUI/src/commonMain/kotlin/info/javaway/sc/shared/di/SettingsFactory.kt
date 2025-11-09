package info.javaway.sc.shared.di

import com.russhwolf.settings.Settings

/**
 * Фабрика для создания Settings (multiplatform-settings)
 * Каждая платформа предоставляет свою реализацию
 */
expect fun createSettings(): Settings
