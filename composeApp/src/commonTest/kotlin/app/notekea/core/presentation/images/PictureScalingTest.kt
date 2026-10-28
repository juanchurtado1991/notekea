package app.notekea.core.presentation.images

import androidx.compose.ui.graphics.ImageBitmap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class PictureScalingTest {
    @Test
    fun `a large picture shrinks to the smallest size that still covers the card, keeping its proportions`() {
        val photo = ImageBitmap(width = 4_000, height = 3_000)

        val cover = photo.scaledToCover(width = 400, height = 200)

        assertEquals(expected = 400 to 300, actual = cover.width to cover.height)
    }

    @Test
    fun `a picture already small enough is kept as it is, never enlarged`() {
        val icon = ImageBitmap(width = 120, height = 90)

        assertSame(expected = icon, actual = icon.scaledToCover(width = 400, height = 300))
    }
}
