package app.notekea.core.presentation.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

/**
 * Draws [bitmap] at [size], which a Markdown renderer reads as the image's own size to place it inline (an image in a
 * paragraph or a table cell, `![[photo.png|80]]` 80 wide); it keeps the image's proportions when [size] does.
 */
class ScaledBitmapPainter(
    private val bitmap: ImageBitmap,
    private val size: Size
) : Painter() {
    override val intrinsicSize: Size get() = size

    override fun DrawScope.onDraw() {
        drawImage(
            image = bitmap,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(width = bitmap.width, height = bitmap.height),
            dstSize = IntSize(width = this.size.width.roundToInt(), height = this.size.height.roundToInt())
        )
    }
}
