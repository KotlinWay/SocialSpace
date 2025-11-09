package info.javaway.sc.backend.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureHTTP() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost() // TODO: Change this in production
    }

    // Настройка раздачи статических файлов (загруженные изображения)
    routing {
        // Раздача файлов из директории uploads/
        static("/uploads") {
            staticRootFolder = File("uploads")
            files(".")
        }
    }
}
