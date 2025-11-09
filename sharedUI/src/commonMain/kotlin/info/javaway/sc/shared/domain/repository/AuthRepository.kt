package info.javaway.sc.shared.domain.repository

import info.javaway.sc.shared.domain.models.*

/**
 * Репозиторий для работы с аутентификацией
 */
interface AuthRepository {
    suspend fun register(request: RegisterRequest): Result<AuthResponse>
    suspend fun login(request: LoginRequest): Result<AuthResponse>
    suspend fun getCurrentUser(): Result<User>
    suspend fun logout()
    fun isLoggedIn(): Boolean
    fun getToken(): String?
    fun getUserId(): Long?
}
