package info.javaway.sc.shared.data.repository

import info.javaway.sc.shared.data.api.ApiClient
import info.javaway.sc.shared.data.local.TokenManager
import info.javaway.sc.shared.domain.models.*
import info.javaway.sc.shared.domain.repository.AuthRepository

/**
 * Реализация репозитория аутентификации
 */
class AuthRepositoryImpl(
    private val apiClient: ApiClient,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return when (val result = apiClient.register(request)) {
            is Result.Success -> {
                tokenManager.saveToken(result.data.token)
                tokenManager.saveUserId(result.data.user.id)
                result
            }
            is Result.Error -> result
            is Result.Loading -> result
        }
    }

    override suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return when (val result = apiClient.login(request)) {
            is Result.Success -> {
                tokenManager.saveToken(result.data.token)
                tokenManager.saveUserId(result.data.user.id)
                result
            }
            is Result.Error -> result
            is Result.Loading -> result
        }
    }

    override suspend fun getCurrentUser(): Result<User> {
        return apiClient.getCurrentUser()
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
