package app.notekea.core.presentation.images

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * This picture at the smallest size that still covers [width] by [height] pixels while keeping its proportions, as a
 * cropped card shows it (`ROADMAP.md` 1.18): a 12-megapixel photo held for a card takes a few megabytes instead of
 * about fifty. A picture already that small is returned as it is; none is ever enlarged.
 */
fun ImageBitmap.scaledToCover(
    width: Int,
    height: Int
): ImageBitmap {
    val scale = max(width.toFloat() / this.width, height.toFloat() / this.height)
    if (scale >= 1f) return this
    val size = IntSize(
        width = (this.width * scale).roundToInt().coerceAtLeast(minimumValue = 1),
        height = (this.height * scale).roundToInt().coerceAtLeast(minimumValue = 1)
    )
    val scaled = ImageBitmap(width = size.width, height = size.height)
    Canvas(image = scaled).drawImageRect(
        image = this,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(width = this.width, height = this.height),
        dstOffset = IntOffset.Zero,
        dstSize = size,
        paint = Paint().apply { filterQuality = FilterQuality.High }
    )
    return scaled
}
