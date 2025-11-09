package info.javaway.sc.shared.data.local

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

/**
 * Менеджер для работы с JWT токеном
 */
class TokenManager(private val settings: Settings) {

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
    }

    /**
     * Сохранить токен
     */
    fun saveToken(token: String) {
        settings[KEY_AUTH_TOKEN] = token
    }

    /**
     * Получить токен
     */
    fun getToken(): String? {
        return settings.getStringOrNull(KEY_AUTH_TOKEN)
    }

    /**
     * Проверить наличие токена
     */
    fun hasToken(): Boolean {
        return getToken() != null
    }

    /**
     * Сохранить ID пользователя
     */
    fun saveUserId(userId: Long) {
        settings[KEY_USER_ID] = userId
    }

    /**
     * Получить ID пользователя
     */
    fun getUserId(): Long? {
        return settings.getLongOrNull(KEY_USER_ID)
    }

    /**
     * Удалить токен и данные пользователя (выход)
     */
    fun clear() {
        settings.remove(KEY_AUTH_TOKEN)
        settings.remove(KEY_USER_ID)
    }
}
