package app.notekea.core.presentation.images

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap

/** [bytes] as a picture, decoded on [dispatcher]; null when Skia cannot read them (a broken or renamed file). */
suspend fun decodedImage(
    bytes: ByteArray,
    dispatcher: CoroutineDispatcher
): ImageBitmap? = withContext(context = dispatcher) {
    try {
        bytes.decodeToImageBitmap()
    } catch (_: IllegalArgumentException) {
        null
    }
}
