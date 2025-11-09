package info.javaway.sc.backend.services

import java.io.File
import java.util.UUID

/**
 * Сервис для работы с файлами (загрузка, валидация, хранение)
 */
object FileService {

    // Директории для хранения файлов
    private const val UPLOAD_DIR = "uploads"
    private const val AVATARS_DIR = "$UPLOAD_DIR/avatars"
    private const val PRODUCTS_DIR = "$UPLOAD_DIR/products"
    private const val SERVICES_DIR = "$UPLOAD_DIR/services"

    // Разрешенные типы файлов (MIME types)
    private val ALLOWED_IMAGE_TYPES = setOf(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/webp"
    )

    // Разрешенные расширения файлов
    private val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")

    // Максимальный размер файла: 5 MB
    private const val MAX_FILE_SIZE = 5 * 1024 * 1024L // 5 MB в байтах

    init {
        // Создаем директории при инициализации, если их нет
        createDirectoriesIfNotExist()
    }

    /**
     * Создает директории для хранения файлов, если они не существуют
     */
    private fun createDirectoriesIfNotExist() {
        listOf(AVATARS_DIR, PRODUCTS_DIR, SERVICES_DIR).forEach { dir ->
            val directory = File(dir)
            if (!directory.exists()) {
                directory.mkdirs()
            }
        }
    }

    /**
     * Валидация изображения
     * @param fileBytes байты файла
     * @param fileName имя файла
     * @param contentType MIME тип файла
     * @return ValidationResult с результатом валидации
     */
    fun validateImage(
        fileBytes: ByteArray,
        fileName: String,
        contentType: String?
    ): ValidationResult {
        // Проверка размера файла
        if (fileBytes.isEmpty()) {
            return ValidationResult(false, "Файл пустой")
        }

        if (fileBytes.size > MAX_FILE_SIZE) {
            val sizeMB = String.format("%.2f", fileBytes.size / (1024.0 * 1024.0))
            return ValidationResult(false, "Размер файла ($sizeMB MB) превышает максимально допустимый (5 MB)")
        }

        // Проверка типа файла по MIME type
        if (contentType != null && contentType !in ALLOWED_IMAGE_TYPES) {
            return ValidationResult(
                false,
                "Неподдерживаемый тип файла. Разрешены: JPG, PNG, WEBP"
            )
        }

        // Проверка расширения файла
        val extension = fileName.substringAfterLast('.', "").lowercase()
        if (extension !in ALLOWED_EXTENSIONS) {
            return ValidationResult(
                false,
                "Неподдерживаемое расширение файла. Разрешены: jpg, jpeg, png, webp"
            )
        }

        return ValidationResult(true)
    }

    /**
     * Сохранение файла в директорию
     * @param fileBytes байты файла
     * @param originalFileName оригинальное имя файла
     * @param fileType тип файла (avatar, product, service)
     * @return FileUploadResult с URL файла или ошибкой
     */
    fun saveFile(
        fileBytes: ByteArray,
        originalFileName: String,
        fileType: FileType
    ): FileUploadResult {
        // Генерируем уникальное имя файла
        val extension = originalFileName.substringAfterLast('.', "").lowercase()
        val uniqueFileName = "${UUID.randomUUID()}.$extension"

        // Определяем директорию в зависимости от типа файла
        val directory = when (fileType) {
            FileType.AVATAR -> AVATARS_DIR
            FileType.PRODUCT -> PRODUCTS_DIR
            FileType.SERVICE -> SERVICES_DIR
        }

        // Создаем полный путь к файлу
        val file = File("$directory/$uniqueFileName")

        return try {
            // Сохраняем файл
            file.writeBytes(fileBytes)

            // Формируем URL для доступа к файлу
            val fileUrl = "/$directory/$uniqueFileName"

            FileUploadResult(
                success = true,
                url = fileUrl,
                fileName = uniqueFileName
            )
        } catch (e: Exception) {
            FileUploadResult(
                success = false,
                error = "Ошибка при сохранении файла: ${e.message}"
            )
        }
    }

    /**
     * Удаление файла
     * @param fileUrl URL файла (например, "/uploads/products/uuid.jpg")
     * @return true если файл успешно удален, false если не удалось удалить
     */
    fun deleteFile(fileUrl: String): Boolean {
        return try {
            // Убираем начальный слеш, если есть
            val filePath = fileUrl.removePrefix("/")
            val file = File(filePath)

            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Удаление нескольких файлов
     * @param fileUrls список URL файлов
     */
    fun deleteFiles(fileUrls: List<String>) {
        fileUrls.forEach { deleteFile(it) }
    }
}

/**
 * Тип файла для определения директории хранения
 */
enum class FileType {
    AVATAR,   // Аватары пользователей
    PRODUCT,  // Фотографии товаров
    SERVICE   // Фотографии услуг
}

/**
 * Результат загрузки файла
 */
data class FileUploadResult(
    val success: Boolean,
    val url: String? = null,
    val fileName: String? = null,
    val error: String? = null
)
