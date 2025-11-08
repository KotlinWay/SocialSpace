package com.socialspace.server.routes

import com.socialspace.server.models.CreateUserDTO
import com.socialspace.server.models.UpdateUserDTO
import com.socialspace.server.repositories.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

/**
 * Настройка маршрутов для работы с пользователями
 */
fun Route.userRoutes() {
    val userRepository = UserRepository()

    route("/api/users") {

        // GET /api/users - Получить всех пользователей
        get {
            try {
                val users = userRepository.findAll()
                call.respond(HttpStatusCode.OK, users)
            } catch (e: Exception) {
                call.application.log.error("Error fetching users", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to fetch users")
                )
            }
        }

        // GET /api/users/{id} - Получить пользователя по ID
        get("/{id}") {
            try {
                val id = call.parameters["id"] ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Missing user ID")
                    )
                    return@get
                }

                val userId = try {
                    UUID.fromString(id)
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid user ID format")
                    )
                    return@get
                }

                val user = userRepository.findById(userId)
                if (user != null) {
                    call.respond(HttpStatusCode.OK, user)
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "User not found")
                    )
                }
            } catch (e: Exception) {
                call.application.log.error("Error fetching user", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to fetch user")
                )
            }
        }

        // POST /api/users - Создать нового пользователя
        post {
            try {
                val createUserDTO = call.receive<CreateUserDTO>()

                // Валидация входных данных
                val validationErrors = validateCreateUser(createUserDTO)
                if (validationErrors.isNotEmpty()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("errors" to validationErrors)
                    )
                    return@post
                }

                // Проверка на дубликаты
                if (userRepository.existsByPhone(createUserDTO.phone)) {
                    call.respond(
                        HttpStatusCode.Conflict,
                        mapOf("error" to "User with this phone already exists")
                    )
                    return@post
                }

                if (createUserDTO.email != null && userRepository.existsByEmail(createUserDTO.email)) {
                    call.respond(
                        HttpStatusCode.Conflict,
                        mapOf("error" to "User with this email already exists")
                    )
                    return@post
                }

                val user = userRepository.create(createUserDTO)
                if (user != null) {
                    call.respond(HttpStatusCode.Created, user)
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to create user")
                    )
                }
            } catch (e: ContentTransformationException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid request body")
                )
            } catch (e: Exception) {
                call.application.log.error("Error creating user", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to create user")
                )
            }
        }

        // PUT /api/users/{id} - Обновить пользователя
        put("/{id}") {
            try {
                val id = call.parameters["id"] ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Missing user ID")
                    )
                    return@put
                }

                val userId = try {
                    UUID.fromString(id)
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid user ID format")
                    )
                    return@put
                }

                val updateUserDTO = call.receive<UpdateUserDTO>()

                // Валидация входных данных
                val validationErrors = validateUpdateUser(updateUserDTO)
                if (validationErrors.isNotEmpty()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("errors" to validationErrors)
                    )
                    return@put
                }

                // Проверяем, существует ли пользователь
                val existingUser = userRepository.findById(userId)
                if (existingUser == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "User not found")
                    )
                    return@put
                }

                // Проверка на дубликат email, если он изменяется
                if (updateUserDTO.email != null &&
                    updateUserDTO.email != existingUser.email &&
                    userRepository.existsByEmail(updateUserDTO.email)) {
                    call.respond(
                        HttpStatusCode.Conflict,
                        mapOf("error" to "User with this email already exists")
                    )
                    return@put
                }

                val updated = userRepository.update(userId, updateUserDTO)
                if (updated) {
                    val updatedUser = userRepository.findById(userId)
                    call.respond(HttpStatusCode.OK, updatedUser!!)
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to update user")
                    )
                }
            } catch (e: ContentTransformationException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid request body")
                )
            } catch (e: Exception) {
                call.application.log.error("Error updating user", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to update user")
                )
            }
        }

        // DELETE /api/users/{id} - Удалить пользователя
        delete("/{id}") {
            try {
                val id = call.parameters["id"] ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Missing user ID")
                    )
                    return@delete
                }

                val userId = try {
                    UUID.fromString(id)
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid user ID format")
                    )
                    return@delete
                }

                val deleted = userRepository.delete(userId)
                if (deleted) {
                    call.respond(
                        HttpStatusCode.OK,
                        mapOf("message" to "User deleted successfully")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "User not found")
                    )
                }
            } catch (e: Exception) {
                call.application.log.error("Error deleting user", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Failed to delete user")
                )
            }
        }
    }
}

/**
 * Валидация данных при создании пользователя
 */
private fun validateCreateUser(dto: CreateUserDTO): List<String> {
    val errors = mutableListOf<String>()

    if (dto.phone.isBlank()) {
        errors.add("Phone is required")
    } else if (!isValidPhone(dto.phone)) {
        errors.add("Phone format is invalid")
    }

    if (dto.name.isBlank()) {
        errors.add("Name is required")
    } else if (dto.name.length < 2) {
        errors.add("Name must be at least 2 characters")
    } else if (dto.name.length > 255) {
        errors.add("Name must not exceed 255 characters")
    }

    if (dto.email != null && dto.email.isNotBlank() && !isValidEmail(dto.email)) {
        errors.add("Email format is invalid")
    }

    if (dto.password.isBlank()) {
        errors.add("Password is required")
    } else if (dto.password.length < 6) {
        errors.add("Password must be at least 6 characters")
    }

    return errors
}

/**
 * Валидация данных при обновлении пользователя
 */
private fun validateUpdateUser(dto: UpdateUserDTO): List<String> {
    val errors = mutableListOf<String>()

    if (dto.name != null) {
        if (dto.name.isBlank()) {
            errors.add("Name cannot be empty")
        } else if (dto.name.length < 2) {
            errors.add("Name must be at least 2 characters")
        } else if (dto.name.length > 255) {
            errors.add("Name must not exceed 255 characters")
        }
    }

    if (dto.email != null && dto.email.isNotBlank() && !isValidEmail(dto.email)) {
        errors.add("Email format is invalid")
    }

    if (dto.bio != null && dto.bio.length > 1000) {
        errors.add("Bio must not exceed 1000 characters")
    }

    return errors
}

/**
 * Простая валидация формата телефона
 * Принимает телефоны в формате +7XXXXXXXXXX или 8XXXXXXXXXX
 */
private fun isValidPhone(phone: String): Boolean {
    val phoneRegex = Regex("^(\\+7|8)\\d{10}$")
    return phoneRegex.matches(phone)
}

/**
 * Простая валидация формата email
 */
private fun isValidEmail(email: String): Boolean {
    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    return emailRegex.matches(email)
}
