package info.javaway.sc.shared.utils

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.github.aakira.napier.Napier
import java.io.ByteArrayOutputStream

/**
 * Android реализация выбора изображений из галереи
 */
@Composable
actual fun rememberImagePickerLauncher(
    maxImages: Int,
    onResult: (List<SelectedImage>) -> Unit
): () -> Unit {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = maxImages)
    ) { uris: List<Uri> ->
        Napier.d("ImagePicker: Selected ${uris.size} images")

        if (uris.isEmpty()) {
            Napier.d("ImagePicker: No images selected")
            onResult(emptyList())
            return@rememberLauncherForActivityResult
        }

        try {
            val selectedImages = uris.mapNotNull { uri ->
                try {
                    val bytes = uri.toByteArray(context)
                    val name = uri.getFileName(context)
                    val mimeType = uri.getMimeType(context)

                    SelectedImage(
                        bytes = bytes,
                        name = name,
                        mimeType = mimeType
                    )
                } catch (e: Exception) {
                    Napier.e("ImagePicker: Failed to read image from uri: $uri", e)
                    null
                }
            }

            Napier.d("ImagePicker: Successfully loaded ${selectedImages.size} images")
            onResult(selectedImages)
        } catch (e: Exception) {
            Napier.e("ImagePicker: Failed to process images", e)
            onResult(emptyList())
        }
    }

    return {
        launcher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
}

/**
 * Конвертирует Uri в ByteArray
 */
private fun Uri.toByteArray(context: Context): ByteArray {
    val inputStream = context.contentResolver.openInputStream(this)
        ?: throw IllegalStateException("Cannot open input stream for uri: $this")

    return inputStream.use { input ->
        val buffer = ByteArrayOutputStream()
        val data = ByteArray(4096)
        var count: Int

        while (input.read(data).also { count = it } != -1) {
            buffer.write(data, 0, count)
        }

        buffer.toByteArray()
    }
}

/**
 * Получает имя файла из Uri
 */
private fun Uri.getFileName(context: Context): String {
    val cursor = context.contentResolver.query(this, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val displayNameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (displayNameIndex != -1) {
                return it.getString(displayNameIndex)
            }
        }
    }
    // Fallback: используем последний сегмент пути
    return lastPathSegment ?: "image_${System.currentTimeMillis()}.jpg"
}

/**
 * Получает MIME type из Uri
 */
private fun Uri.getMimeType(context: Context): String {
    return context.contentResolver.getType(this) ?: "image/jpeg"
}
