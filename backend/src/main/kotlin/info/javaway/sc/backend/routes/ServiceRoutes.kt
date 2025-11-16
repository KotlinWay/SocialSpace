package info.javaway.sc.backend.routes

import info.javaway.sc.api.models.*
import info.javaway.sc.backend.repository.CategoryRepository
import info.javaway.sc.backend.repository.ServiceRepository
import info.javaway.sc.backend.repository.UserRepository
import info.javaway.sc.backend.services.SpaceService
import info.javaway.sc.backend.services.SpaceService.SpaceException
import info.javaway.sc.backend.utils.SpaceDefaults
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.math.ceil

/**
 * Маршруты для работы с услугами
 */
fun Route.serviceRoutes(
    serviceRepository: ServiceRepository = ServiceRepository(),
    userRepository: UserRepository = UserRepository(),
    categoryRepository: CategoryRepository = CategoryRepository(),
    spaceService: SpaceService = SpaceService()
) {
    suspend fun ApplicationCall.resolveSpaceIdOrRespond(): Long? {
        val param = request.queryParameters["spaceId"]
        if (param.isNullOrBlank()) {
            return SpaceDefaults.DEFAULT_SPACE_ID
        }
        return param.toLongOrNull() ?: run {
            respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("INVALID_SPACE_ID", "Некорректный spaceId")
            )
            null
        }
    }

    route("/services") {

        /**
         * GET /api/services
         * Получить список услуг с фильтрацией и пагинацией
         *
         * Query параметры:
         * - categoryId: Long? - фильтр по категории
         * - status: String? - фильтр по статусу (ACTIVE, INACTIVE)
         * - search: String? - поиск по названию и описанию
         * - page: Int? - номер страницы (по умолчанию 1)
         * - pageSize: Int? - размер страницы (по умолчанию 20, максимум 100)
         */
        get {
            val spaceId = call.resolveSpaceIdOrRespond() ?: return@get

            try {
                val categoryId = call.request.queryParameters["categoryId"]?.toLongOrNull()
                val statusStr = call.request.queryParameters["status"]
                val searchQuery = call.request.queryParameters["search"]
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull()?.coerceIn(1, 100) ?: 20

                if (page < 1) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("INVALID_PAGE", "Номер страницы должен быть больше 0")
                    )
                    return@get
                }

                val status = statusStr?.let { runCatching { ServiceStatus.valueOf(it) }.getOrNull() }

                val principal = call.principal<JWTPrincipal>()
                val currentUserId = principal?.payload?.getClaim("userId")?.asLong()
                currentUserId?.let {
                    try {
                        spaceService.ensureMembership(spaceId, it)
                    } catch (e: SpaceException) {
                        call.respond(e.status, ErrorResponse(e.code, e.message))
                        return@get
                    }
                }

                val offset = ((page - 1) * pageSize).toLong()

                val services = serviceRepository.getAllServices(
                    spaceId = spaceId,
                    categoryId = categoryId,
                    status = status,
                    searchQuery = searchQuery,
                    limit = pageSize,
                    offset = offset
                )

                val total = serviceRepository.countServices(
                    spaceId = spaceId,
                    categoryId = categoryId,
                    status = status,
                    searchQuery = searchQuery
                )

                val totalPages = ceil(total.toDouble() / pageSize).toInt()

                // Маппим Service -> ServiceListItem с дополнительной информацией
                val serviceListItems = services.mapNotNull { service ->
                    val user = userRepository.findById(service.userId) ?: return@mapNotNull null
                    val category = categoryRepository.findById(service.categoryId) ?: return@mapNotNull null

                    ServiceListItem(
                        id = service.id,
                        spaceId = service.spaceId,
                        title = service.title,
                        description = service.description,
                        price = service.price,
                        images = service.images,
                        status = service.status,
                        views = service.views,
                        createdAt = service.createdAt,
                        updatedAt = service.updatedAt,
                        user = UserPublicInfo(
                            id = user.id,
                            name = user.name,
                            avatar = user.avatar,
                            phone = user.phone,
                            rating = user.rating,
                            isVerified = user.isVerified
                        ),
                        category = CategoryInfo(
                            id = category.id,
                            name = category.name,
                            icon = category.icon
                        )
                    )
                }

                val response = ServiceListResponse(
                    services = serviceListItems,
                    total = total,
                    page = page,
                    pageSize = pageSize,
                    totalPages = totalPages
                )

                call.respond(HttpStatusCode.OK, response)
            } catch (e: Exception) {
                call.application.log.error("Get services error", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", "Ошибка при получении списка услуг")
                )
            }
        }

        /**
         * GET /api/services/{id}
         * Получить детальную информацию об услуге
         */
        get("/{id}") {
            try {
                val serviceId = call.parameters["id"]?.toLongOrNull()

                if (serviceId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("INVALID_ID", "Неверный ID услуги")
                    )
                    return@get
                }

                val service = serviceRepository.findById(serviceId)
                if (service == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("SERVICE_NOT_FOUND", "Услуга не найдена")
                    )
                    return@get
                }

                val principal = call.principal<JWTPrincipal>()
                val currentUserId = principal?.payload?.getClaim("userId")?.asLong()
                currentUserId?.let {
                    try {
                        spaceService.ensureMembership(service.spaceId, it)
                    } catch (e: SpaceException) {
                        call.respond(e.status, ErrorResponse(e.code, e.message))
                        return@get
                    }
                }

                // Увеличиваем счетчик просмотров
                serviceRepository.incrementViews(serviceId)

                // Получаем информацию о пользователе
                val user = userRepository.findById(service.userId)
                if (user == null) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("USER_NOT_FOUND", "Пользователь услуги не найден")
                    )
                    return@get
                }

                // Получаем информацию о категории
                val category = categoryRepository.findById(service.categoryId)
                if (category == null) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("CATEGORY_NOT_FOUND", "Категория услуги не найдена")
                    )
                    return@get
                }

                val response = ServiceResponse(
                    service = service,
                    user = UserPublicInfo(
                        id = user.id,
                        name = user.name,
                        avatar = user.avatar,
                        phone = user.phone,
                        rating = user.rating,
                        isVerified = user.isVerified
                    ),
                    category = CategoryInfo(
                        id = category.id,
                        name = category.name,
                        icon = category.icon
                    )
                )

                call.respond(HttpStatusCode.OK, response)
            } catch (e: Exception) {
                call.application.log.error("Get service details error", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SERVER_ERROR", "Ошибка при получении услуги")
                )
            }
        }

        /**
         * Защищенные маршруты (требуют аутентификации)
         */
        authenticate("auth-jwt") {

            /**
             * GET /api/services/my
             * Получить список услуг текущего пользователя
             */
            get("/my") {
                val spaceId = call.resolveSpaceIdOrRespond() ?: return@get

                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()

                    if (userId == null) {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse("UNAUTHORIZED", "Требуется авторизация")
                        )
                        return@get
                    }

                    try {
                        spaceService.ensureMembership(spaceId, userId)
                    } catch (e: SpaceException) {
                        call.respond(e.status, ErrorResponse(e.code, e.message))
                        return@get
                    }

                    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                    val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull()?.coerceIn(1, 100) ?: 20
                    val offset = ((page - 1) * pageSize).toLong()

                    val services = serviceRepository.findByUserId(
                        userId = userId,
                        spaceId = spaceId,
                        limit = pageSize,
                        offset = offset
                    )

                    val serviceListItems = services.mapNotNull { service ->
                        val user = userRepository.findById(service.userId) ?: return@mapNotNull null
                        val category = categoryRepository.findById(service.categoryId) ?: return@mapNotNull null

                        ServiceListItem(
                            id = service.id,
                            spaceId = service.spaceId,
                            title = service.title,
                            description = service.description,
                            price = service.price,
                            images = service.images,
                            status = service.status,
                            views = service.views,
                            createdAt = service.createdAt,
                            updatedAt = service.updatedAt,
                            user = UserPublicInfo(
                                id = user.id,
                                name = user.name,
                                avatar = user.avatar,
                                phone = user.phone,
                                rating = user.rating,
                                isVerified = user.isVerified
                            ),
                            category = CategoryInfo(
                                id = category.id,
                                name = category.name,
                                icon = category.icon
                            )
                        )
                    }

                    call.respond(HttpStatusCode.OK, serviceListItems)
                } catch (e: Exception) {
                    call.application.log.error("Get my services error", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("SERVER_ERROR", "Ошибка при получении услуг пользователя")
                    )
                }
            }

            /**
             * POST /api/services
             * Создать новую услугу
             */
            post {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()

                    if (userId == null) {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ErrorResponse("UNAUTHORIZED", "Требуется авторизация")
                        )
                        return@post
                    }

                    val request = call.receive<CreateServiceRequest>()

                    // Валидация
                    if (request.title.isBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_TITLE", "Название не может быть пустым")
                        )
                        return@post
                    }

                    if (request.title.length > 200) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_TITLE", "Название не может быть длиннее 200 символов")
                        )
                        return@post
                    }

                    if (request.description.isBlank()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_DESCRIPTION", "Описание не может быть пустым")
                        )
                        return@post
                    }

                    if (request.images.isEmpty()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_IMAGES", "Необходимо добавить хотя бы одно изображение")
                        )
                        return@post
                    }

                    if (request.images.size > 5) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_IMAGES", "Максимум 5 изображений")
                        )
                        return@post
                    }

                    val category = categoryRepository.findById(request.categoryId)
                    if (category == null || category.type != CategoryType.SERVICE) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_CATEGORY", "Неверная категория услуги")
                        )
                        return@post
                    }

                    val user = userRepository.findById(userId)
                        ?: return@post call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse("USER_NOT_FOUND", "Пользователь не найден")
                        )

                    val requestedSpaceId = request.spaceId
                    if (requestedSpaceId != null && requestedSpaceId <= 0) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_SPACE_ID", "spaceId должен быть положительным числом")
                        )
                        return@post
                    }

                    val targetSpaceId = requestedSpaceId ?: user.defaultSpaceId ?: SpaceDefaults.DEFAULT_SPACE_ID

                    try {
                        spaceService.ensureMembership(targetSpaceId, userId)
                    } catch (e: SpaceException) {
                        call.respond(e.status, ErrorResponse(e.code, e.message))
                        return@post
                    }

                    val service = serviceRepository.createService(
                        userId = userId,
                        title = request.title,
                        description = request.description,
                        categoryId = request.categoryId,
                        price = request.price,
                        images = request.images,
                        spaceId = targetSpaceId
                    )

                    if (service != null) {
                        val response = ServiceResponse(
                            service = service,
                            user = UserPublicInfo(
                                id = user.id,
                                name = user.name,
                                avatar = user.avatar,
                                phone = user.phone,
                                rating = user.rating,
                                isVerified = user.isVerified
                            ),
                            category = CategoryInfo(
                                id = category.id,
                                name = category.name,
                                icon = category.icon
                            )
                        )

                        call.respond(HttpStatusCode.Created, response)
                    } else {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse("CREATE_ERROR", "Ошибка при создании услуги")
                        )
                    }
                } catch (e: Exception) {
                    call.application.log.error("Create service error", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("SERVER_ERROR", "Ошибка при создании услуги")
                    )
                }
            }

            /**
             * PUT /api/services/{id}
             * Обновить услугу (только свою)
             */
            put("/{id}") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()
                    val serviceId = call.parameters["id"]?.toLongOrNull()

                    if (userId == null || serviceId == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_REQUEST", "Неверный запрос")
                        )
                        return@put
                    }

                    val existingService = serviceRepository.findById(serviceId)
                    if (existingService == null) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("SERVICE_NOT_FOUND", "Услуга не найдена")
                        )
                        return@put
                    }

                    if (existingService.userId != userId) {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse("FORBIDDEN", "Нет прав для редактирования этой услуги")
                        )
                        return@put
                    }

                    try {
                        spaceService.ensureMembership(existingService.spaceId, userId)
                    } catch (e: SpaceException) {
                        call.respond(e.status, ErrorResponse(e.code, e.message))
                        return@put
                    }

                    val request = call.receive<UpdateServiceRequest>()

                    // Валидация
                    request.title?.let {
                        if (it.isBlank() || it.length > 200) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("INVALID_TITLE", "Название должно быть от 1 до 200 символов")
                            )
                            return@put
                        }
                    }

                    request.description?.let {
                        if (it.isBlank()) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("INVALID_DESCRIPTION", "Описание не может быть пустым")
                            )
                            return@put
                        }
                    }

                    request.images?.let {
                        if (it.isEmpty() || it.size > 5) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("INVALID_IMAGES", "Количество изображений должно быть от 1 до 5")
                            )
                            return@put
                        }
                    }

                    request.categoryId?.let {
                        val category = categoryRepository.findById(it)
                        if (category == null || category.type != CategoryType.SERVICE) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorResponse("INVALID_CATEGORY", "Неверная категория")
                            )
                            return@put
                        }
                    }

                    if (request.spaceId != null && request.spaceId != existingService.spaceId) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("SPACE_CHANGE_NOT_SUPPORTED", "Перемещение услуги между пространствами пока не поддерживается")
                        )
                        return@put
                    }

                    val updatedService = serviceRepository.updateService(
                        serviceId = serviceId,
                        title = request.title,
                        description = request.description,
                        categoryId = request.categoryId,
                        price = request.price,
                        status = request.status,
                        images = request.images
                    )

                    if (updatedService != null) {
                        // Получаем информацию о пользователе
                        val user = userRepository.findById(updatedService.userId)
                        if (user == null) {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                ErrorResponse("USER_NOT_FOUND", "Пользователь не найден")
                            )
                            return@put
                        }

                        // Получаем информацию о категории
                        val categoryInfo = categoryRepository.findById(updatedService.categoryId)
                        if (categoryInfo == null) {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                ErrorResponse("CATEGORY_NOT_FOUND", "Категория не найдена")
                            )
                            return@put
                        }

                        val response = ServiceResponse(
                            service = updatedService,
                            user = UserPublicInfo(
                                id = user.id,
                                name = user.name,
                                avatar = user.avatar,
                                phone = user.phone,
                                rating = user.rating,
                                isVerified = user.isVerified
                            ),
                            category = CategoryInfo(
                                id = categoryInfo.id,
                                name = categoryInfo.name,
                                icon = categoryInfo.icon
                            )
                        )

                        call.respond(HttpStatusCode.OK, response)
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("SERVICE_NOT_FOUND", "Услуга не найдена")
                        )
                    }
                } catch (e: Exception) {
                    call.application.log.error("Update service error", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("SERVER_ERROR", "Ошибка при обновлении услуги")
                    )
                }
            }

            /**
             * DELETE /api/services/{id}
             * Удалить услугу (только свою)
             */
            delete("/{id}") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()
                    val serviceId = call.parameters["id"]?.toLongOrNull()

                    if (userId == null || serviceId == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("INVALID_REQUEST", "Неверный запрос")
                        )
                        return@delete
                    }

                    val service = serviceRepository.findById(serviceId)
                    if (service == null) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("SERVICE_NOT_FOUND", "Услуга не найдена")
                        )
                        return@delete
                    }

                    if (service.userId != userId) {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse("FORBIDDEN", "Нет прав для удаления этой услуги")
                        )
                        return@delete
                    }

                    try {
                        spaceService.ensureMembership(service.spaceId, userId)
                    } catch (e: SpaceException) {
                        call.respond(e.status, ErrorResponse(e.code, e.message))
                        return@delete
                    }

                    val deleted = serviceRepository.deleteService(serviceId)
                    if (deleted) {
                        call.respond(
                            HttpStatusCode.OK,
                            SuccessResponse(true, "Услуга успешно удалена")
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("SERVICE_NOT_FOUND", "Услуга не найдена")
                        )
                    }
                } catch (e: Exception) {
                    call.application.log.error("Delete service error", e)
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("SERVER_ERROR", "Ошибка при удалении услуги")
                    )
                }
            }
        }
    }
}
