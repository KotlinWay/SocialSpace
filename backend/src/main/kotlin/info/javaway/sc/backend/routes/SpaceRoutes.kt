package info.javaway.sc.backend.routes

import info.javaway.sc.api.models.CreateSpaceRequest
import info.javaway.sc.api.models.ErrorResponse
import info.javaway.sc.api.models.JoinSpaceRequest
import info.javaway.sc.api.models.SpaceListResponse
import info.javaway.sc.api.models.SpaceMemberResponse
import info.javaway.sc.api.models.SpaceResponse
import info.javaway.sc.api.models.SpaceType
import info.javaway.sc.api.models.UpdateSpaceRequest
import info.javaway.sc.backend.services.SpaceService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

/**
 * Маршруты для работы с пространствами (Spaces).
 */
fun Route.spaceRoutes(
    spaceService: SpaceService = SpaceService()
) {
    route("/spaces") {

        get {
            val typeParam = call.request.queryParameters["type"]
            val type = typeParam?.let {
                runCatching { SpaceType.valueOf(it.uppercase()) }.getOrNull()
            }
            val search = call.request.queryParameters["search"]
            val page = call.request.queryParameters["page"]?.toIntOrNull()?.takeIf { it > 0 } ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull()?.coerceIn(1, 100) ?: 20

            val principal = call.principal<JWTPrincipal>()
            val currentUserId = principal?.payload?.getClaim("userId")?.asLong()

            try {
                val response: SpaceListResponse = spaceService.listSpaces(
                    currentUserId = currentUserId,
                    type = type,
                    search = search,
                    page = page,
                    pageSize = pageSize
                )
                call.respond(HttpStatusCode.OK, response)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SPACE_LIST_ERROR", e.message ?: "Ошибка при получении пространств")
                )
            }
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_ID", "Неверный ID пространства"))
                return@get
            }

            val principal = call.principal<JWTPrincipal>()
            val currentUserId = principal?.payload?.getClaim("userId")?.asLong()

            try {
                val response: SpaceResponse = spaceService.getSpace(id, currentUserId)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: SpaceService.SpaceException) {
                call.respond(e.status, ErrorResponse(e.code, e.message))
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("SPACE_DETAIL_ERROR", e.message ?: "Ошибка при получении информации о пространстве")
                )
            }
        }

        authenticate("auth-jwt") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val ownerId = principal?.payload?.getClaim("userId")?.asLong()
                if (ownerId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("UNAUTHORIZED", "Требуется авторизация"))
                    return@post
                }

                try {
                    val request = call.receive<CreateSpaceRequest>()
                    val response = spaceService.createSpace(ownerId, request)
                    call.respond(HttpStatusCode.Created, response)
                } catch (e: SpaceService.SpaceException) {
                    call.respond(e.status, ErrorResponse(e.code, e.message))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("SPACE_CREATE_ERROR", e.message ?: "Ошибка при создании пространства")
                    )
                }
            }

            put("/{id}") {
                val id = call.parameters["id"]?.toLongOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_ID", "Неверный ID пространства"))
                    return@put
                }

                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asLong()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("UNAUTHORIZED", "Требуется авторизация"))
                    return@put
                }

                try {
                    val request = call.receive<UpdateSpaceRequest>()
                    val response = spaceService.updateSpace(id, userId, request)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: SpaceService.SpaceException) {
                    call.respond(e.status, ErrorResponse(e.code, e.message))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("SPACE_UPDATE_ERROR", e.message ?: "Ошибка при обновлении пространства")
                    )
                }
            }

            post("/{id}/join") {
                val id = call.parameters["id"]?.toLongOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_ID", "Неверный ID пространства"))
                    return@post
                }

                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asLong()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("UNAUTHORIZED", "Требуется авторизация"))
                    return@post
                }

                try {
                    val request = call.receive<JoinSpaceRequest>()
                    val response = spaceService.joinSpace(id, userId, request)
                    call.respond(HttpStatusCode.OK, response)
                } catch (e: SpaceService.SpaceException) {
                    call.respond(e.status, ErrorResponse(e.code, e.message))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("SPACE_JOIN_ERROR", e.message ?: "Ошибка при вступлении в пространство")
                    )
                }
            }

            get("/{id}/members") {
                val id = call.parameters["id"]?.toLongOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("INVALID_ID", "Неверный ID пространства"))
                    return@get
                }

                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asLong()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("UNAUTHORIZED", "Требуется авторизация"))
                    return@get
                }

                try {
                    spaceService.ensureMembership(id, userId)
                    val members: List<SpaceMemberResponse> = spaceService.listMembers(id)
                    call.respond(HttpStatusCode.OK, members)
                } catch (e: SpaceService.SpaceException) {
                    call.respond(e.status, ErrorResponse(e.code, e.message))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("SPACE_MEMBERS_ERROR", e.message ?: "Ошибка при получении участников пространства")
                    )
                }
            }
        }
    }
}
