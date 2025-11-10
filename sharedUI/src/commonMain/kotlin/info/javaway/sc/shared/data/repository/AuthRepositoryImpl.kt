package info.javaway.sc.shared.data.repository

import info.javaway.sc.shared.data.api.ApiClient
import info.javaway.sc.shared.data.local.TokenManager
import info.javaway.sc.shared.data.mappers.toDomain
import info.javaway.sc.shared.domain.models.AuthResponse
import info.javaway.sc.shared.domain.models.User
import info.javaway.sc.shared.domain.repository.AuthRepository
import info.javaway.sc.api.models.RegisterRequest as ApiRegisterRequest
import info.javaway.sc.api.models.LoginRequest as ApiLoginRequest

/**
 * Реализация репозитория аутентификации
 * Преобразует DTO в Domain модели
 */
class AuthRepositoryImpl(
    private val apiClient: ApiClient,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun register(
        phone: String,
        email: String?,
        name: String,
        password: String
    ): kotlin.Result<AuthResponse> {
        val request = ApiRegisterRequest(
            phone = phone,
            email = email,
            name = name,
            password = password
        )

        val result = apiClient.register(request)
        return when (result) {
            is Result.Success -> {
                tokenManager.saveToken(result.data.token)
                tokenManager.saveUserId(result.data.user.id)
                Result.Success(result.data.toDomain())
            }
            is Result.Error -> result
        }
    }

    override suspend fun login(phone: String, password: String): kotlin.Result<AuthResponse> {
        val request = ApiLoginRequest(phone = phone, password = password)

        val result = apiClient.login(request)
        return when (result) {
            is Result.Success -> {
                tokenManager.saveToken(result.data.token)
                tokenManager.saveUserId(result.data.user.id)
                Result.Success(result.data.toDomain())
            }
            is Result.Error -> result
        }
    }

    override suspend fun getCurrentUser(): kotlin.Result<User> {
        val result = apiClient.getCurrentUser()
        return when (result) {
            is Result.Success -> Result.Success(result.data.toDomain())
            is Result.Error -> result
        }
    }

    override suspend fun logout() {
        tokenManager.clear()
    }

    override fun isLoggedIn(): Boolean {
        return tokenManager.hasToken()
    }

    override fun getToken(): String? {
        return tokenManager.getToken()
    }

    override fun getUserId(): Long? {
        return tokenManager.getUserId()
    }
}
