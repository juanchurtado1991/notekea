package app.notekea.core.presentation.components

import androidx.compose.ui.geometry.Size

/**
 * The size an attached image is shown at: its own size (one image pixel per dp), [requestedWidth] when the note asks
 * for one (`|300`), never wider than [maxWidth], and always with its own proportions. Every value is in the same unit.
 */
fun attachmentImageSize(
    imageWidth: Int,
    imageHeight: Int,
    maxWidth: Float,
    requestedWidth: Float? = null
): Size {
    if (imageWidth <= 0 || imageHeight <= 0) return Size.Zero
    val width = minOf(requestedWidth ?: imageWidth.toFloat(), maxWidth)
    return Size(width = width, height = width * imageHeight / imageWidth)
}
