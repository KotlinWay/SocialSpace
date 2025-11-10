package info.javaway.sc.shared.data.repository

import info.javaway.sc.shared.data.api.ApiClient
import info.javaway.sc.shared.data.local.TokenManager
import info.javaway.sc.api.models.*
import info.javaway.sc.shared.domain.repository.AuthRepository

/**
 * Реализация репозитория аутентификации
 */
class AuthRepositoryImpl(
    private val apiClient: ApiClient,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun register(request: RegisterRequest): kotlin.Result<AuthResponse> {
        return apiClient.register(request)
            .onSuccess { response ->
                tokenManager.saveToken(response.token)
                tokenManager.saveUserId(response.user.id)
            }
    }

    override suspend fun login(request: LoginRequest): kotlin.Result<AuthResponse> {
        return apiClient.login(request)
            .onSuccess { response ->
                tokenManager.saveToken(response.token)
                tokenManager.saveUserId(response.user.id)
            }
    }

    override suspend fun getCurrentUser(): kotlin.Result<User> {
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
