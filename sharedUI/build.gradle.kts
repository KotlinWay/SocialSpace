import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    androidTarget {
        // Use compilerOptions instead of the deprecated kotlinOptions
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
        // Move the Android configuration here
        // No need for a separate android { ... } block
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "sharedUI"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // API Models - общие модели для клиента и сервера
            implementation(project(":api-models"))

            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Ktor Client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.client.logging)

            // Serialization
            implementation(libs.kotlinx.serialization.json)

            // Navigation - Decompose
            api(libs.decompose)
            api(libs.decompose.ext.compose.kmm)
            api(libs.decompose.compose)

            // DI - Koin
            api(libs.koin.core)

            // Image loading - Coil
            implementation(libs.coil)
            implementation(libs.coil.network.ktor)

            // DateTime
            implementation(libs.kotlinx.datetime)

            // Logging
            api(libs.napier)

            // Settings
            implementation(libs.multiplatformSettings)

            // Paging 3
            implementation(libs.paging.common)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlinx.coroutines.android)

            // Activity Compose для ImagePicker
            api(libs.androidx.activityCompose)

            // Paging 3 Compose
            implementation(libs.paging.compose)


            api(libs.koin.android)
            api(libs.koin.compose)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

// Android configuration for KMP library
android {
    namespace = "info.javaway.sc.shared"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
