package app.notekea.editor.data

import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

/**
 * Picks a video's poster (the developer's request of 2026-11-13: a video always shows a picture, and never a black
 * one when it has another): its frame at 0 s, or the first later one of [TIMES] that is not dark; when every frame is
 * dark, the first. [frameAt] gives the frame at a second as a PNG, null past the video's end.
 */
internal class VideoPoster(private val frameAt: suspend (seconds: Int) -> ByteArray?) {
    suspend fun pick(): ByteArray? {
        var first: ByteArray? = null

        for (seconds in TIMES) {
            val frame = frameAt(seconds) ?: break
            if (!isDark(png = frame)) return frame
            first = first ?: frame
        }

        return first
    }

    companion object {
        /** The seconds looked at, in order: a video that fades in from black shows something by then. */
        val TIMES = listOf(0, 1, 3, 6, 12, 30)

        // Of 255: under it a frame is black to the eye (a fade's first frames, a recording's dark start).
        private const val DARK = 24
        private const val STEPS = 24
        private const val RED_SHIFT = 16
        private const val GREEN_SHIFT = 8
        private const val CHANNEL = 0xFF
        private const val RED_WEIGHT = 299
        private const val GREEN_WEIGHT = 587
        private const val BLUE_WEIGHT = 114
        private const val WEIGHTS = 1000

        /** Whether the picture [png] is black to the eye: its brightness, sampled over a grid, is under [DARK]. */
        fun isDark(png: ByteArray): Boolean {
            val image = ImageIO.read(ByteArrayInputStream(png)) ?: return true
            var sum = 0L
            var count = 0

            for (column in 0 until STEPS) {
                for (row in 0 until STEPS) {
                    val pixel =
                        image.getRGB(column * image.width / STEPS, row * image.height / STEPS)

                    val red = pixel shr RED_SHIFT and CHANNEL
                    val green = pixel shr GREEN_SHIFT and CHANNEL
                    val blue = pixel and CHANNEL
                    sum += (red * RED_WEIGHT + green * GREEN_WEIGHT + blue * BLUE_WEIGHT) / WEIGHTS
                    count++
                }
            }

            return sum / count < DARK
        }
    }
}
