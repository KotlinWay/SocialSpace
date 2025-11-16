package info.javaway.sc.backend.plugins

import info.javaway.sc.backend.routes.authRoutes
import info.javaway.sc.backend.routes.categoryRoutes
import info.javaway.sc.backend.routes.fileRoutes
import info.javaway.sc.backend.routes.productRoutes
import info.javaway.sc.backend.routes.spaceRoutes
import info.javaway.sc.backend.routes.serviceRoutes
import info.javaway.sc.backend.routes.userRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("SocialSpace API - Server is running!")
        }

        get("/health") {
            call.respondText("OK")
        }

        // API Routes
        route("/api") {
            // Аутентификация (регистрация, вход, получение текущего пользователя)
            authRoutes()

            // Пользователи (профили, обновление, удаление)
            userRoutes()

            // Категории (товары и услуги)
            categoryRoutes()

            // Товары (CRUD, избранное, поиск)
            productRoutes()

            // Услуги (CRUD, поиск)
            serviceRoutes()

            // Пространства (список, создание, вступление)
            spaceRoutes()

            // Загрузка файлов (изображения для товаров, услуг, аватары)
            fileRoutes()
        }
    }
}
