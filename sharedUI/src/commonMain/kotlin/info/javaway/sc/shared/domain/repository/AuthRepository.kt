package info.javaway.sc.shared.domain.repository

import info.javaway.sc.shared.domain.models.AuthResponse
import info.javaway.sc.shared.domain.models.User

/**
 * Репозиторий для работы с аутентификацией
 * Возвращает Domain модели
 */
interface AuthRepository {
    /**
     * Регистрация нового пользователя
     */
    suspend fun register(phone: String, email: String?, name: String, password: String): kotlin.Result<AuthResponse>

    /**
     * Вход в систему
     */
    suspend fun login(phone: String, password: String): kotlin.Result<AuthResponse>

    /**
     * Получить текущего пользователя
     */
    suspend fun getCurrentUser(): kotlin.Result<User>

    /**
     * Выход из системы
     */
    suspend fun logout()

    /**
     * Проверка авторизации
     */
    fun isLoggedIn(): Boolean

    /**
     * Получить токен
     */
    fun getToken(): String?

    /**
     * Получить ID текущего пользователя
     */
    fun getUserId(): Long?
}
