package app.notekea.editor.data

import kotlinx.coroutines.test.runTest
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals

/** A video's poster is never a black frame when the video has another (the developer's smoke of 2026-11-13). */
class VideoPosterTest {
    private val black = frame(color = Color.BLACK)
    private val street = frame(color = Color(120, 140, 90))

    @Test
    fun `the first frame that is not black is the poster, and the seconds after it are not read`() =
        runTest {
            val read = mutableListOf<Int>()

            val poster = VideoPoster(
                frameAt = { seconds ->
                    read += seconds
                    if (seconds < FADE_END) black else street
                }
            ).pick()

            assertEquals(
                expected = listOf<Any?>(true, listOf(0, 1, 3)),
                actual = listOf(poster.contentEquals(street), read)
            )
        }

    @Test
    fun `a video black all along, or shorter than the seconds read, keeps its first frame, and none gives none`() =
        runTest {
            val allBlack = VideoPoster(frameAt = { black }).pick()

            val short = VideoPoster(frameAt = { seconds ->
                black.takeIf {
                    seconds == 0
                }
            }).pick()

            val none = VideoPoster(frameAt = { null }).pick()

            assertEquals(
                expected = listOf<Any?>(true, true, null),
                actual = listOf(allBlack.contentEquals(black), short.contentEquals(black), none)
            )
        }

    private fun frame(color: Color): ByteArray {
        val image = BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB)

        image
            .createGraphics()
            .apply {
                this.color = color
                fillRect(0, 0, SIZE, SIZE)
                dispose()
            }

        return ByteArrayOutputStream().use { bytes ->
            ImageIO.write(image, "png", bytes)
            bytes.toByteArray()
        }
    }

    private companion object {
        const val SIZE = 48
        const val FADE_END = 3
    }
}
