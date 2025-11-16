package info.javaway.sc.shared.di

import info.javaway.sc.shared.utils.PhoneDialer
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android implementation of platformModule.
 * Provides Android-specific dependencies.
 */
actual val platformModule = module {
    // PhoneDialer requires Android Context
    single { PhoneDialer(androidContext()) }
}
