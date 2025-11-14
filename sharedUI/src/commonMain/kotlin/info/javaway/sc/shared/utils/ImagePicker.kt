package info.javaway.sc.shared.utils

import androidx.compose.runtime.Composable

/**
 * Выбранное изображение
 */
data class SelectedImage(
    val bytes: ByteArray,
    val name: String,
    val mimeType: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SelectedImage

        if (!bytes.contentEquals(other.bytes)) return false
        if (name != other.name) return false
        if (mimeType != other.mimeType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + mimeType.hashCode()
        return result
    }
}

/**
 * Launcher для выбора изображений из галереи (до 5 штук)
 *
 * @param maxImages Максимальное количество изображений (по умолчанию 5)
 * @param onResult Callback с выбранными изображениями
 * @return Lambda для запуска выбора изображений
 */
@Composable
expect fun rememberImagePickerLauncher(
    maxImages: Int = 5,
    onResult: (List<SelectedImage>) -> Unit
): () -> Unit
