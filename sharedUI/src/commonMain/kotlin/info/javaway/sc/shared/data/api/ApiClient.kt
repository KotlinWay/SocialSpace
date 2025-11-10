package info.javaway.sc.shared.data.api

import info.javaway.sc.shared.domain.models.*
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.json.Json

/**
 * HTTP клиент для работы с Backend API
 */
class ApiClient(
    private val baseUrl: String = "http://10.0.2.2:8080", // Android Emulator localhost
    private val tokenProvider: () -> String? = { null }
) {

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }

        install(DefaultRequest) {
            url(baseUrl)
            contentType(ContentType.Application.Json)
            headers {
                Napier.e("token: 🦄 ${tokenProvider()}")
                tokenProvider()?.let { token ->
                    append(HttpHeaders.Authorization, "Bearer $token")
                }
            }
        }
    }

    // ==================== AUTH ====================

    suspend fun register(request: RegisterRequest): kotlin.Result<AuthResponse> = handleRequest {
        httpClient.post("/api/auth/register") {
            setBody(request)
        }.body()
    }

    suspend fun login(request: LoginRequest): kotlin.Result<AuthResponse> = handleRequest {
        httpClient.post("/api/auth/login") {
            setBody(request)
        }.body()
    }

    suspend fun getCurrentUser(): kotlin.Result<User> = handleRequest {
        httpClient.get("/api/auth/me").body()
    }

    // ==================== USERS ====================

    suspend fun getUser(userId: Long): kotlin.Result<User> = handleRequest {
        httpClient.get("/api/users/$userId").body()
    }

    suspend fun updateProfile(userId: Long, request: UpdateProfileRequest): kotlin.Result<User> = handleRequest {
        httpClient.put("/api/users/$userId") {
            setBody(request)
        }.body()
    }

    suspend fun deleteAccount(userId: Long): kotlin.Result<SuccessResponse> = handleRequest {
        httpClient.delete("/api/users/$userId").body()
    }

    // ==================== CATEGORIES ====================

    suspend fun getAllCategories(): kotlin.Result<List<Category>> = handleRequest {
        httpClient.get("/api/categories").body()
    }

    suspend fun getProductCategories(): kotlin.Result<List<Category>> = handleRequest {
        httpClient.get("/api/categories/products").body()
    }

    suspend fun getServiceCategories(): kotlin.Result<List<Category>> = handleRequest {
        httpClient.get("/api/categories/services").body()
    }

    // ==================== PRODUCTS ====================

    suspend fun getProducts(
        categoryId: Long? = null,
        status: ProductStatus? = null,
        condition: ProductCondition? = null,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        search: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): kotlin.Result<ProductListResponse> = handleRequest {
        httpClient.get("/api/products") {
            parameter("categoryId", categoryId)
            parameter("status", status?.name)
            parameter("condition", condition?.name)
            parameter("minPrice", minPrice)
            parameter("maxPrice", maxPrice)
            parameter("search", search)
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.body()
    }

    suspend fun getProduct(productId: Long): kotlin.Result<ProductResponse> = handleRequest {
        httpClient.get("/api/products/$productId").body()
    }

    suspend fun getMyProducts(): kotlin.Result<List<ProductResponse>> = handleRequest {
        httpClient.get("/api/products/my").body()
    }

    suspend fun getFavoriteProducts(page: Int = 1, pageSize: Int = 20): kotlin.Result<ProductListResponse> = handleRequest {
        httpClient.get("/api/products/favorites") {
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.body()
    }

    suspend fun addToFavorites(productId: Long): kotlin.Result<SuccessResponse> = handleRequest {
        httpClient.post("/api/products/$productId/favorite").body()
    }

    suspend fun removeFromFavorites(productId: Long): kotlin.Result<SuccessResponse> = handleRequest {
        httpClient.delete("/api/products/$productId/favorite").body()
    }

    // ==================== SERVICES ====================

    suspend fun getServices(
        categoryId: Long? = null,
        status: ServiceStatus? = null,
        search: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): kotlin.Result<ServiceListResponse> = handleRequest {
        httpClient.get("/api/services") {
            parameter("categoryId", categoryId)
            parameter("status", status?.name)
            parameter("search", search)
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.body()
    }

    suspend fun getService(serviceId: Long): kotlin.Result<ServiceResponse> = handleRequest {
        httpClient.get("/api/services/$serviceId").body()
    }

    suspend fun getMyServices(): kotlin.Result<List<ServiceResponse>> = handleRequest {
        httpClient.get("/api/services/my").body()
    }

    // ==================== ERROR HANDLING ====================

    private suspend fun <T> handleRequest(block: suspend () -> T): kotlin.Result<T> {
        return try {
            kotlin.Result.success(block())
        } catch (e: ClientRequestException) {
            // 4xx errors
            println("❌ ClientRequestException: ${e.response.status}")
            println("   URL: ${e.response.call.request.url}")
            println("   Headers: ${e.response.call.request.headers.entries()}")
            try {
                val errorResponse: ErrorResponse = e.response.body()
                println("   Error body: $errorResponse")
                kotlin.Result.failure(Exception(errorResponse.message))
            } catch (parseError: Exception) {
                println("   Failed to parse error body: ${parseError.message}")
                kotlin.Result.failure(Exception("Ошибка клиента: ${e.response.status.description}"))
            }
        } catch (e: ServerResponseException) {
            // 5xx errors
            println("❌ ServerResponseException: ${e.response.status}")
            println("   URL: ${e.response.call.request.url}")
            kotlin.Result.failure(Exception("Ошибка сервера: ${e.response.status.description}"))
        } catch (e: Exception) {
            // Network or other errors
            println("❌ Exception: ${e.message}")
            e.printStackTrace()
            kotlin.Result.failure(e)
        }
    }

    fun close() {
        httpClient.close()
    }
}
