package info.javaway.sc.shared.di

import info.javaway.sc.shared.data.api.ApiClient
import info.javaway.sc.shared.data.local.TokenManager
import info.javaway.sc.shared.data.repository.AuthRepositoryImpl
import info.javaway.sc.shared.data.repository.CategoryRepositoryImpl
import info.javaway.sc.shared.data.repository.ProductRepositoryImpl
import info.javaway.sc.shared.data.repository.ServiceRepositoryImpl
import info.javaway.sc.shared.domain.repository.AuthRepository
import info.javaway.sc.shared.domain.repository.CategoryRepository
import info.javaway.sc.shared.domain.repository.ProductRepository
import info.javaway.sc.shared.domain.repository.ServiceRepository
import info.javaway.sc.shared.presentation.screens.auth.LoginViewModel
import info.javaway.sc.shared.presentation.screens.auth.RegisterViewModel
import info.javaway.sc.shared.presentation.screens.home.HomeViewModel
import info.javaway.sc.shared.presentation.screens.products.CreateProductViewModel
import info.javaway.sc.shared.presentation.screens.products.MyProductsViewModel
import info.javaway.sc.shared.presentation.screens.products.ProductDetailViewModel
import info.javaway.sc.shared.presentation.screens.products.ProductListViewModel
import info.javaway.sc.shared.presentation.screens.profile.ProfileViewModel
import info.javaway.sc.shared.presentation.screens.services.CreateServiceViewModel
import info.javaway.sc.shared.presentation.screens.services.ServiceDetailViewModel
import info.javaway.sc.shared.presentation.screens.services.ServiceListViewModel
import org.koin.core.module.dsl.factoryOf
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

    // ApiClient для работы с Backend API
    single {
        ApiClient(
            baseUrl = "http://10.0.2.2:8080", // Android Emulator localhost
            tokenProvider = { get<TokenManager>().getToken() }
        )
    }
}

val repositoryModule = module {
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
    singleOf(::CategoryRepositoryImpl) bind CategoryRepository::class
    singleOf(::ProductRepositoryImpl) bind ProductRepository::class
    singleOf(::ServiceRepositoryImpl) bind ServiceRepository::class
}

val viewModelModule = module {
    factoryOf(::LoginViewModel)
    factoryOf(::RegisterViewModel)
    factoryOf(::HomeViewModel)
    factoryOf(::ProductListViewModel)
    factoryOf(::ServiceListViewModel)
    factoryOf(::ProfileViewModel)

    // CreateProductViewModel для создания товара
    factoryOf(::CreateProductViewModel)

    // MyProductsViewModel для списка своих товаров
    factoryOf(::MyProductsViewModel)

    // CreateServiceViewModel для создания услуги
    factoryOf(::CreateServiceViewModel)

    // ProductDetailViewModel с параметром productId
    factory { (productId: Long) ->
        ProductDetailViewModel(
            productRepository = get(),
            authRepository = get(),
            productId = productId
        )
    }

    // ServiceDetailViewModel с параметром serviceId
    factory { (serviceId: Long) ->
        ServiceDetailViewModel(
            serviceRepository = get(),
            authRepository = get(),
            serviceId = serviceId
        )
    }
}

// Объединенный список всех модулей
val appModules = listOf(
    dataModule,
    repositoryModule,
    viewModelModule
)
