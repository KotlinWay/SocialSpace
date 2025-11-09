package info.javaway.sc.backend.plugins

import info.javaway.sc.backend.routes.authRoutes
import info.javaway.sc.backend.routes.categoryRoutes
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

            // TODO: Add product routes
            // TODO: Add service routes
        }
    }
}
