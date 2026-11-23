package app.notekea.core.presentation

import androidx.compose.ui.ImageComposeScene

/**
 * The time of a scene test: each [settle] renders [frames] more frames, 16 ms apart, so what a click or a key
 * started (effects, recompositions, short animations) is on the image the test then reads.
 */
class SceneClock(private val frames: Int = 4) {
    private var frame = 0L

    fun settle(scene: ImageComposeScene) {
        repeat(times = frames) { scene.render(nanoTime = (frame++) * FRAME_NANOS) }
    }

    private companion object {
        const val FRAME_NANOS = 16_000_000L
    }
}
