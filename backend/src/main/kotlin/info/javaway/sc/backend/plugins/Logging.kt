package info.javaway.sc.backend.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import org.slf4j.event.Level

fun Application.configureLogging() {
    install(CallLogging) {
        level = Level.INFO

        // Логируем каждый запрос с заголовками
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val uri = call.request.uri
            val headers = call.request.headers.entries()
                .joinToString(", ") { (key, values) ->
                    "$key: ${values.joinToString("; ")}"
                }

            buildString {
                append("📡 $httpMethod $uri")
                append(" | Status: $status")
                append("\n   Headers: [$headers]")
            }
        }
    }
}
