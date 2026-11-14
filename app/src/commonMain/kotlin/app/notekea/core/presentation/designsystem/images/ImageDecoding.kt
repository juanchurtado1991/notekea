package app.notekea.core.presentation.designsystem.images

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap

/**
 * [bytes] as a picture, decoded on [dispatcher]; null when Skia cannot read them (a broken or renamed file). A picture
 * wider than [maxWidth] is kept at that width: a camera's photo held a hundred megabytes of memory to be drawn a
 * few hundred points wide (the code review of 2026-11-13).
 */
suspend fun decodedImage(
    bytes: ByteArray,
    dispatcher: CoroutineDispatcher,
    maxWidth: Int? = null
): ImageBitmap? = withContext(context = dispatcher) {
    try {
        val whole = bytes.decodeToImageBitmap()
        val isSmall = maxWidth == null || whole.width <= maxWidth

        if (isSmall) {
            whole
        } else {
            narrowed(
                image = whole,
                width = maxWidth
            )
        }
    } catch (_: IllegalArgumentException) {
        null
    }
}

private fun narrowed(
    image: ImageBitmap,
    width: Int
): ImageBitmap {
    val height =
        (image.height.toLong() * width / image.width).toInt().coerceAtLeast(minimumValue = 1)

    val narrow = ImageBitmap(
        width = width,
        height = height
    )

    Canvas(image = narrow).drawImageRect(
        image = image,
        dstSize = IntSize(
            width = width,
            height = height
        ),
        paint = Paint().apply { filterQuality = FilterQuality.High }
    )

    return narrow
}
