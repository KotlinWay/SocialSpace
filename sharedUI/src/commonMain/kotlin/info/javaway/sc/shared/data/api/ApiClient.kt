package info.javaway.sc.shared.data.api

import info.javaway.sc.api.models.*
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
                encodeDefaults = true // ✅ Всегда сериализовать поля со значениями по умолчанию (включая null для nullable полей)
            })
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Napier.e("Logger Ktor => 🦄 $message")
                }
            }
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

    suspend fun register(request: RegisterRequest): Result<AuthResponse> = handleRequest {
        httpClient.post("/api/auth/register") {
            setBody(request)
        }.body()
    }

    suspend fun login(request: LoginRequest): Result<AuthResponse> = handleRequest {
        httpClient.post("/api/auth/login") {
            setBody(request)
        }.body()
    }

    suspend fun getCurrentUser(): Result<User> = handleRequest {
        httpClient.get("/api/auth/me").body()
    }

    // ==================== USERS ====================

    suspend fun getUser(userId: Long): Result<User> = handleRequest {
        httpClient.get("/api/users/$userId").body()
    }

    suspend fun updateProfile(userId: Long, request: UpdateProfileRequest): Result<User> = handleRequest {
        httpClient.put("/api/users/$userId") {
            setBody(request)
        }.body()
    }

    suspend fun deleteAccount(userId: Long): Result<SuccessResponse> = handleRequest {
        httpClient.delete("/api/users/$userId").body()
    }

    // ==================== CATEGORIES ====================

    suspend fun getAllCategories(): Result<List<Category>> = handleRequest {
        httpClient.get("/api/categories").body()
    }

    suspend fun getProductCategories(): Result<List<Category>> = handleRequest {
        httpClient.get("/api/categories/products").body()
    }

    suspend fun getServiceCategories(): Result<List<Category>> = handleRequest {
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
    ): Result<ProductListResponse> = handleRequest {
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

    suspend fun getProduct(productId: Long): Result<ProductResponse> = handleRequest {
        httpClient.get("/api/products/$productId").body()
    }

    suspend fun getMyProducts(): Result<List<ProductResponse>> = handleRequest {
        httpClient.get("/api/products/my").body()
    }

    suspend fun getFavoriteProducts(page: Int = 1, pageSize: Int = 20): Result<ProductListResponse> = handleRequest {
        httpClient.get("/api/products/favorites") {
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.body()
    }

    suspend fun addToFavorites(productId: Long): Result<SuccessResponse> = handleRequest {
        httpClient.post("/api/products/$productId/favorite").body()
    }

    suspend fun removeFromFavorites(productId: Long): Result<SuccessResponse> = handleRequest {
        httpClient.delete("/api/products/$productId/favorite").body()
    }

    // ==================== SERVICES ====================

    suspend fun getServices(
        categoryId: Long? = null,
        status: ServiceStatus? = null,
        search: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): Result<ServiceListResponse> = handleRequest {
        httpClient.get("/api/services") {
            parameter("categoryId", categoryId)
            parameter("status", status?.name)
            parameter("search", search)
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.body()
    }

    suspend fun getService(serviceId: Long): Result<ServiceResponse> = handleRequest {
        httpClient.get("/api/services/$serviceId").body()
    }

    suspend fun getMyServices(): Result<List<ServiceResponse>> = handleRequest {
        httpClient.get("/api/services/my").body()
    }

    // ==================== ERROR HANDLING ====================

    private suspend fun <T> handleRequest(block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: ClientRequestException) {
            // 4xx errors
            println("❌ ClientRequestException: ${e.response.status}")
            println("   URL: ${e.response.call.request.url}")
            println("   Headers: ${e.response.call.request.headers.entries()}")
            try {
                val errorResponse: ErrorResponse = e.response.body()
                println("   Error body: $errorResponse")
                Result.failure(Exception(errorResponse.message))
            } catch (parseError: Exception) {
                println("   Failed to parse error body: ${parseError.message}")
                Result.failure(Exception("Ошибка клиента: ${e.response.status.description}"))
            }
        } catch (e: ServerResponseException) {
            // 5xx errors
            println("❌ ServerResponseException: ${e.response.status}")
            println("   URL: ${e.response.call.request.url}")
            Result.failure(Exception("Ошибка сервера: ${e.response.status.description}"))
        } catch (e: Exception) {
            // Network or other errors
            println("❌ Exception: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun close() {
        httpClient.close()
    }
}
