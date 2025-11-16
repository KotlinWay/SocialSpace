package info.javaway.sc.shared.data.repository

import info.javaway.sc.shared.data.api.ApiClient
import info.javaway.sc.shared.data.local.SpaceManager
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
    private val tokenManager: TokenManager,
    private val spaceManager: SpaceManager
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

        return apiClient.register(request).fold(
            onSuccess = { authResponse ->
                tokenManager.saveToken(authResponse.token)
                tokenManager.saveUserId(authResponse.user.id)
                handleSpaceSelection(authResponse.user.defaultSpaceId)
                kotlin.Result.success(authResponse.toDomain())
            },
            onFailure = { exception ->
                kotlin.Result.failure(exception)
            }
        )
    }

    override suspend fun login(phone: String, password: String): kotlin.Result<AuthResponse> {
        val request = ApiLoginRequest(phone = phone, password = password)

        return apiClient.login(request).fold(
            onSuccess = { authResponse ->
                tokenManager.saveToken(authResponse.token)
                tokenManager.saveUserId(authResponse.user.id)
                handleSpaceSelection(authResponse.user.defaultSpaceId)
                kotlin.Result.success(authResponse.toDomain())
            },
            onFailure = { exception ->
                kotlin.Result.failure(exception)
            }
        )
    }

    override suspend fun getCurrentUser(): kotlin.Result<User> {
        return apiClient.getCurrentUser().fold(
            onSuccess = { user ->
                kotlin.Result.success(user.toDomain())
            },
            onFailure = { exception ->
                kotlin.Result.failure(exception)
            }
        )
    }

    override suspend fun logout() {
        tokenManager.clear()
        spaceManager.clearSpace()
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
    private fun handleSpaceSelection(defaultSpaceId: Long?) {
        if (defaultSpaceId != null) {
            spaceManager.selectSpace(defaultSpaceId)
        } else {
            spaceManager.clearSpace()
        }
    }
}
