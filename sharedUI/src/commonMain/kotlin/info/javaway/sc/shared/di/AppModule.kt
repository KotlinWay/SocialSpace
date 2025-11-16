package info.javaway.sc.shared.di

import info.javaway.sc.shared.data.api.ApiClient
import info.javaway.sc.shared.data.local.SpaceManager
import info.javaway.sc.shared.data.local.ThemeManager
import info.javaway.sc.shared.data.local.TokenManager
import info.javaway.sc.shared.data.repository.AuthRepositoryImpl
import info.javaway.sc.shared.data.repository.CategoryRepositoryImpl
import info.javaway.sc.shared.data.repository.ProductRepositoryImpl
import info.javaway.sc.shared.data.repository.ServiceRepositoryImpl
import info.javaway.sc.shared.data.repository.SpaceRepositoryImpl
import info.javaway.sc.shared.domain.repository.AuthRepository
import info.javaway.sc.shared.domain.repository.CategoryRepository
import info.javaway.sc.shared.domain.repository.ProductRepository
import info.javaway.sc.shared.domain.repository.SpaceRepository
import info.javaway.sc.shared.domain.repository.ServiceRepository
import info.javaway.sc.shared.utils.PhoneDialer
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin модули для Dependency Injection
 */

val dataModule = module {
    // Settings для хранения данных (platform-specific)
    single { createSettings() }

    // TokenManager для работы с JWT токеном
    single { TokenManager(get()) }

    // SpaceManager для хранения выбранного пространства
    single { SpaceManager(get()) }

    // ThemeManager для хранения пользовательской темы
    single { ThemeManager(get()) }

    // ApiClient для работы с Backend API
    single {
        ApiClient(
            baseUrl = "http://10.0.2.2:8080", // Android Emulator localhost
            tokenProvider = { get<TokenManager>().getToken() },
            spaceProvider = { get<SpaceManager>().getCurrentSpaceId() }
        )
    }
}

/**
 * Platform-specific module (expect/actual pattern)
 * Provides platform-specific dependencies like PhoneDialer
 */
expect val platformModule: org.koin.core.module.Module

val repositoryModule = module {
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
    singleOf(::CategoryRepositoryImpl) bind CategoryRepository::class
    singleOf(::ProductRepositoryImpl) bind ProductRepository::class
    singleOf(::ServiceRepositoryImpl) bind ServiceRepository::class
    singleOf(::SpaceRepositoryImpl) bind SpaceRepository::class
}

// Объединенный список всех модулей
val appModules = listOf(
    dataModule,
    repositoryModule,
    platformModule
)
