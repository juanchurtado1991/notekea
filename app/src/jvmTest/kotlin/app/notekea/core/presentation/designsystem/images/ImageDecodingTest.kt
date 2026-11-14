package app.notekea.core.presentation.designsystem.images

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals

/** The code review of 2026-11-13: a camera's photo was kept whole in memory to be drawn a few hundred points wide. */
class ImageDecodingTest {
    private fun png(
        width: Int,
        height: Int
    ): ByteArray = ByteArrayOutputStream().also {
        ImageIO.write(BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "png", it)
    }.toByteArray()

    @Test
    fun `a picture wider than asked is kept at that width with its shape, a narrower one as it is`() =
        runBlocking {
            val sizes = listOf(WIDE to LIMIT, NARROW to LIMIT, WIDE to null).map { (width, most) ->
                val image = decodedImage(
                    bytes = png(
                        width = width,
                        height = width / 2
                    ),
                    dispatcher = Dispatchers.Default,
                    maxWidth = most
                )

                image?.width to image?.height
            }

            assertEquals(
                expected = listOf(LIMIT to LIMIT / 2, NARROW to NARROW / 2, WIDE to WIDE / 2),
                actual = sizes
            )
        }

    private companion object {
        const val WIDE = 3_000
        const val NARROW = 400
        const val LIMIT = 1_000
    }
}
