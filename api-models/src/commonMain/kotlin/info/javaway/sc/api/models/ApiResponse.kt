package info.javaway.sc.api.models

import kotlinx.serialization.Serializable

/**
 * Ответ с ошибкой
 */
@Serializable
data class ErrorResponse(
    val error: String,
    val message: String
)

/**
 * Успешный ответ
 */
@Serializable
data class SuccessResponse(
    val success: Boolean,
    val message: String? = null
)

/**
 * Ответ после загрузки файла
 */
@Serializable
data class FileUploadResponse(
    val success: Boolean,
    val url: String? = null,
    val fileName: String? = null,
    val message: String? = null
)
