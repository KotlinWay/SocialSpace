package info.javaway.sc.shared.domain.models

import kotlinx.serialization.Serializable

/**
 * Ответ с ошибкой от API
 */
@Serializable
data class ErrorResponse(
    val code: String,
    val message: String
)

/**
 * Успешный ответ от API
 */
@Serializable
data class SuccessResponse(
    val message: String
)

/**
 * Результат операции (Success/Error)
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: String? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}
