package info.javaway.sc.backend.routes

import info.javaway.sc.backend.models.*
import info.javaway.sc.backend.services.AuthResult
import info.javaway.sc.backend.services.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Маршруты для аутентификации
 */
fun Route.authRoutes(authService: AuthService = AuthService()) {

    route("/auth") {

        /**
         * POST /api/auth/register
         * Регистрация нового пользователя
         */
        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()

                when (val result = authService.register(request)) {
                    is AuthResult.Success -> {
                        call.respond(HttpStatusCode.Created, result.data)
                    }
                    is AuthResult.Error -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("REGISTRATION_ERROR", result.message)
                        )
                    }
                }
            } catch (e: Exception) {
                call.application.log.error("Registration error", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", "Ошибка при регистрации")
                )
            }
        }

        /**
         * POST /api/auth/login
         * Вход пользователя
         */
        post("/login") {
            try {
                val request = call.receive<LoginRequest>()

                when (val result = authService.login(request)) {
                    is AuthResult.Success -> {
                        call.respond(HttpStatusCode.OK, result.data)
                    }
                    is AuthResult.Error -> {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse("LOGIN_ERROR", result.message)
                        )
                    }
                }
            } catch (e: Exception) {
                call.application.log.error("Login error", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", "Ошибка при входе")
                )
            }
        }

        /**
         * GET /api/auth/me
         * Получить данные текущего пользователя (требует аутентификации)
         */
        authenticate("auth-jwt") {
            get("/me") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()

                    if (userId == null) {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse("UNAUTHORIZED", "Неверный токен")
                        )
                        return@get
                    }

                    val user = authService.getUserById(userId)
                    if (user == null) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("USER_NOT_FOUND", "Пользователь не найден")
                        )
                        return@get
                    }

                    call.respond(HttpStatusCode.OK, user)
                } catch (e: Exception) {
                    call.application.log.error("Get current user error", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("SERVER_ERROR", "Ошибка при получении данных пользователя")
                    )
                }
            }
        }
    }
}
