package info.javaway.sc.backend.plugins

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

        // API Routes will be added here
        route("/api") {
            // TODO: Add auth routes
            // TODO: Add user routes
            // TODO: Add product routes
            // TODO: Add service routes
            // TODO: Add category routes
        }
    }
}
