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
