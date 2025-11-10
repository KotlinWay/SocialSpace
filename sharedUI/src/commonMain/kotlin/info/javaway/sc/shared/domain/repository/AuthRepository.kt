package info.javaway.sc.shared.domain.repository

import info.javaway.sc.api.models.*

/**
 * Репозиторий для работы с аутентификацией
 */
interface AuthRepository {
    suspend fun register(request: RegisterRequest): kotlin.Result<AuthResponse>
    suspend fun login(request: LoginRequest): kotlin.Result<AuthResponse>
    suspend fun getCurrentUser(): kotlin.Result<User>
    suspend fun logout()
    fun isLoggedIn(): Boolean
    fun getToken(): String?
    fun getUserId(): Long?
}
