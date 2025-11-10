package info.javaway.sc.backend.routes

import info.javaway.sc.api.models.ErrorResponse
import info.javaway.sc.api.models.FileUploadResponse
import info.javaway.sc.backend.repository.UserRepository
import info.javaway.sc.backend.services.FileService
import info.javaway.sc.backend.services.FileType
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Маршруты для работы с файлами (загрузка изображений)
 */
fun Route.fileRoutes(
    userRepository: UserRepository = UserRepository()
) {

    /**
     * POST /api/upload
     * Загрузка изображения (требует JWT авторизации)
     *
     * Query параметры:
     * - type: String - тип файла (avatar, product, service)
     *
     * Multipart/form-data:
     * - file: File - файл изображения
     *
     * Ответ:
     * - 200 OK: FileUploadResponse с URL загруженного файла
     * - 400 Bad Request: ошибка валидации
     * - 401 Unauthorized: не авторизован
     */
    authenticate("auth-jwt") {
        post("/upload") {
            try {
                // Проверяем JWT токен
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asLong()

                if (userId == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("UNAUTHORIZED", "Требуется авторизация")
                    )
                    return@post
                }

                // Получаем тип файла из query параметра
                val typeStr = call.request.queryParameters["type"]
                if (typeStr == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("MISSING_TYPE", "Не указан тип файла (type=avatar|product|service)")
                    )
                    return@post
                }

                // Парсим тип файла
                val fileType = when (typeStr.lowercase()) {
                    "avatar" -> FileType.AVATAR
                    "product" -> FileType.PRODUCT
                    "service" -> FileType.SERVICE
                    else -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_TYPE", "Неверный тип файла. Разрешены: avatar, product, service")
                        )
                        return@post
                    }
                }

                // Обрабатываем multipart запрос
                val multipart = call.receiveMultipart()
                var fileBytes: ByteArray? = null
                var fileName: String? = null
                var contentType: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            fileName = part.originalFileName
                            contentType = part.contentType?.toString()
                            fileBytes = part.streamProvider().readBytes()
                        }
                        else -> part.dispose()
                    }
                }

                // Проверяем, что файл был загружен
                if (fileBytes == null || fileName == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("NO_FILE", "Файл не был загружен")
                    )
                    return@post
                }

                // Валидация изображения
                val validationResult = FileService.validateImage(fileBytes!!, fileName!!, contentType)
                if (!validationResult.isValid) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("VALIDATION_ERROR", validationResult.errorMessage ?: "Ошибка валидации файла")
                    )
                    return@post
                }

                // Сохранение файла
                val uploadResult = FileService.saveFile(fileBytes!!, fileName!!, fileType)

                if (uploadResult.success) {
                    call.respond(
                        HttpStatusCode.OK,
                        FileUploadResponse(
                            success = true,
                            url = uploadResult.url,
                            fileName = uploadResult.fileName,
                            message = "Файл успешно загружен"
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("UPLOAD_ERROR", uploadResult.error ?: "Ошибка при загрузке файла")
                    )
                }

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", "Внутренняя ошибка сервера: ${e.message}")
                )
            }
        }
    }

    /**
     * POST /api/users/{id}/avatar
     * Загрузка аватара пользователя (требует JWT авторизации)
     * Только владелец профиля может загрузить свой аватар
     *
     * Path параметры:
     * - id: Long - ID пользователя
     *
     * Multipart/form-data:
     * - file: File - файл изображения аватара
     *
     * Ответ:
     * - 200 OK: FileUploadResponse с URL загруженного аватара
     * - 400 Bad Request: ошибка валидации
     * - 401 Unauthorized: не авторизован
     * - 403 Forbidden: нет прав для обновления этого профиля
     * - 404 Not Found: пользователь не найден
     */
    authenticate("auth-jwt") {
        post("/users/{id}/avatar") {
            try {
                // Проверяем JWT токен
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asLong()

                if (userId == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("UNAUTHORIZED", "Требуется авторизация")
                    )
                    return@post
                }

                // Получаем ID пользователя из пути
                val targetUserId = call.parameters["id"]?.toLongOrNull()
                if (targetUserId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("INVALID_ID", "Неверный ID пользователя")
                    )
                    return@post
                }

                // Проверяем, что пользователь обновляет свой профиль
                if (userId != targetUserId) {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponse("FORBIDDEN", "Вы можете обновить только свой аватар")
                    )
                    return@post
                }

                // Проверяем, что пользователь существует
                val user = userRepository.findById(targetUserId)
                if (user == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("USER_NOT_FOUND", "Пользователь не найден")
                    )
                    return@post
                }

                // Обрабатываем multipart запрос
                val multipart = call.receiveMultipart()
                var fileBytes: ByteArray? = null
                var fileName: String? = null
                var contentType: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            fileName = part.originalFileName
                            contentType = part.contentType?.toString()
                            fileBytes = part.streamProvider().readBytes()
                        }
                        else -> part.dispose()
                    }
                }

                // Проверяем, что файл был загружен
                if (fileBytes == null || fileName == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("NO_FILE", "Файл не был загружен")
                    )
                    return@post
                }

                // Валидация изображения
                val validationResult = FileService.validateImage(fileBytes!!, fileName!!, contentType)
                if (!validationResult.isValid) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("VALIDATION_ERROR", validationResult.errorMessage ?: "Ошибка валидации файла")
                    )
                    return@post
                }

                // Сохранение файла
                val uploadResult = FileService.saveFile(fileBytes!!, fileName!!, FileType.AVATAR)

                if (!uploadResult.success) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("UPLOAD_ERROR", uploadResult.error ?: "Ошибка при загрузке файла")
                    )
                    return@post
                }

                // Удаляем старый аватар, если он был
                val avatar = user.avatar
                if (!avatar.isNullOrBlank()) {
                    FileService.deleteFile(avatar)
                }

                // Обновляем аватар в профиле пользователя
                val updatedUser = userRepository.updateAvatar(targetUserId, uploadResult.url!!)

                if (updatedUser != null) {
                    call.respond(
                        HttpStatusCode.OK,
                        FileUploadResponse(
                            success = true,
                            url = uploadResult.url,
                            fileName = uploadResult.fileName,
                            message = "Аватар успешно обновлен"
                        )
                    )
                } else {
                    // Если не удалось обновить в БД, удаляем загруженный файл
                    FileService.deleteFile(uploadResult.url)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("UPDATE_ERROR", "Ошибка при обновлении аватара в профиле")
                    )
                }

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", "Внутренняя ошибка сервера: ${e.message}")
                )
            }
        }
    }
}
