package info.javaway.sc.backend.routes

import info.javaway.sc.api.models.CategoryType
import info.javaway.sc.api.models.ErrorResponse
import info.javaway.sc.backend.repository.CategoryRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Маршруты для работы с категориями
 */
fun Route.categoryRoutes(categoryRepository: CategoryRepository = CategoryRepository()) {

    route("/categories") {

        /**
         * GET /api/categories
         * Получить все категории
         */
        get {
            try {
                val categories = categoryRepository.getAllCategories()
                call.respond(HttpStatusCode.OK, categories)
            } catch (e: Exception) {
                call.application.log.error("Error fetching all categories", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", "Ошибка при получении категорий")
                )
            }
        }

        /**
         * GET /api/categories/products
         * Получить категории товаров
         */
        get("/products") {
            try {
                val categories = categoryRepository.getCategoriesByType(CategoryType.PRODUCT)
                call.respond(HttpStatusCode.OK, categories)
            } catch (e: Exception) {
                call.application.log.error("Error fetching product categories", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", "Ошибка при получении категорий товаров")
                )
            }
        }

        /**
         * GET /api/categories/services
         * Получить категории услуг
         */
        get("/services") {
            try {
                val categories = categoryRepository.getCategoriesByType(CategoryType.SERVICE)
                call.respond(HttpStatusCode.OK, categories)
            } catch (e: Exception) {
                call.application.log.error("Error fetching service categories", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", "Ошибка при получении категорий услуг")
                )
            }
        }

        /**
         * GET /api/categories/{id}
         * Получить категорию по ID
         */
        get("/{id}") {
            try {
                val categoryId = call.parameters["id"]?.toLongOrNull()

                if (categoryId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("INVALID_ID", "Некорректный ID категории")
                    )
                    return@get
                }

                val category = categoryRepository.findById(categoryId)

                if (category == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("CATEGORY_NOT_FOUND", "Категория не найдена")
                    )
                    return@get
                }

                call.respond(HttpStatusCode.OK, category)
            } catch (e: Exception) {
                call.application.log.error("Error fetching category by ID", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", "Ошибка при получении категории")
                )
            }
        }
    }
}
