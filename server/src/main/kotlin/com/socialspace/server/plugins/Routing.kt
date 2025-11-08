package com.socialspace.server.plugins

import com.socialspace.server.routes.userRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Welcome to SocialSpace API!")
        }

        get("/health") {
            call.respondText("OK")
        }

        // User routes
        userRoutes()
    }
}
