plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    // JVM target для backend
    jvm {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    // Android target для sharedUI
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    // iOS targets для будущей поддержки
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "apiModels"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Только kotlinx.serialization для сериализации моделей
            implementation(libs.kotlinx.serialization.json)
            // DateTime для работы с датами
            implementation(libs.kotlinx.datetime)
        }

        jvmMain.dependencies {
            // Зависимости специфичные для JVM (если потребуются)
        }

        androidMain.dependencies {
            // Зависимости специфичные для Android (если потребуются)
        }

        iosMain.dependencies {
            // Зависимости специфичные для iOS (если потребуются)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
