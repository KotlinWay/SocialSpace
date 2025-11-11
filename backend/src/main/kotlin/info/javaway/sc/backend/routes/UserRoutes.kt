package info.javaway.sc.backend.routes

import info.javaway.sc.api.models.*
import info.javaway.sc.backend.repository.UserRepository
import info.javaway.sc.backend.services.AuthService
import info.javaway.sc.backend.services.UpdateResult
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Маршруты для работы с пользователями
 */
fun Route.userRoutes(
    authService: AuthService = AuthService(),
    userRepository: UserRepository = UserRepository()
) {

    route("/users") {

        /**
         * GET /api/users/{id}
         * Получить публичный профиль пользователя
         */
        get("/{id}") {
            try {
                val userId = call.parameters["id"]?.toLongOrNull()

                if (userId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("INVALID_ID", "Неверный ID пользователя")
                    )
                    return@get
                }

                val user = userRepository.findById(userId)
                if (user == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("USER_NOT_FOUND", "Пользователь не найден")
                    )
                    return@get
                }

                // Возвращаем публичный профиль (без конфиденциальной информации)
                call.respond(HttpStatusCode.OK, user.toPublicProfile())
            } catch (e: Exception) {
                call.application.log.error("Get user error", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", "Ошибка при получении профиля")
                )
            }
        }

        /**
         * Защищенные маршруты (требуют аутентификации)
         */
        authenticate("auth-jwt") {

            /**
             * PUT /api/users/{id}
             * Обновить профиль пользователя
             */
            put("/{id}") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val currentUserId = principal?.payload?.getClaim("userId")?.asLong()
                    val targetUserId = call.parameters["id"]?.toLongOrNull()

                    if (currentUserId == null || targetUserId == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_REQUEST", "Неверный запрос")
                        )
                        return@put
                    }

                    // Пользователь может редактировать только свой профиль
                    if (currentUserId != targetUserId) {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse("FORBIDDEN", "Нет прав для редактирования этого профиля")
                        )
                        return@put
                    }

                    val request = call.receive<UpdateProfileRequest>()

                    when (val result = authService.updateProfile(currentUserId, request)) {
                        is UpdateResult.Success -> {
                            call.respond(HttpStatusCode.OK, result.user)
                        }
                        is UpdateResult.Error -> {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("UPDATE_ERROR", result.message)
                            )
                        }
                    }
                } catch (e: Exception) {
                    call.application.log.error("Update profile error", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("SERVER_ERROR", "Ошибка при обновлении профиля")
                    )
                }
            }

            /**
             * POST /api/users/{id}/avatar
             * Загрузить аватар пользователя
             * TODO: Реализовать после создания FileService
             */
            post("/{id}/avatar") {
                call.respond(
                    HttpStatusCode.NotImplemented,
                    ErrorResponse("NOT_IMPLEMENTED", "Загрузка аватара будет реализована позже")
                )
            }

            /**
             * DELETE /api/users/{id}
             * Удалить пользователя (только свой аккаунт)
             */
            delete("/{id}") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val currentUserId = principal?.payload?.getClaim("userId")?.asLong()
                    val targetUserId = call.parameters["id"]?.toLongOrNull()

                    if (currentUserId == null || targetUserId == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_REQUEST", "Неверный запрос")
                        )
                        return@delete
                    }

                    // Пользователь может удалить только свой аккаунт
                    if (currentUserId != targetUserId) {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse("FORBIDDEN", "Нет прав для удаления этого аккаунта")
                        )
                        return@delete
                    }

                    val deleted = userRepository.deleteUser(currentUserId)
                    if (deleted) {
                        call.respond(
                            HttpStatusCode.OK,
                            SuccessResponse(true, "Аккаунт успешно удален")
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("USER_NOT_FOUND", "Пользователь не найден")
                        )
                    }
                } catch (e: Exception) {
                    call.application.log.error("Delete user error", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("SERVER_ERROR", "Ошибка при удалении аккаунта")
                    )
                }
            }
        }
    }
}
