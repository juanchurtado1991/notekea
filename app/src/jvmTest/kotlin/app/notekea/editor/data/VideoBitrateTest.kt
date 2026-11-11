package app.notekea.editor.data

import kotlin.test.Test
import kotlin.test.assertEquals

/** How a video is fitted under 100 MB (#141): its bit rate, its audio and, when that rate is low, a smaller picture. */
class VideoBitrateTest {
    @Test
    fun `a short video keeps its size, a long one gets less audio and a smaller picture, and FFmpeg fits each system`() {
        val limit = 100_000_000L

        assertEquals(
            expected = listOf<Any?>(
                VideoBitrate(
                    videoKbps = 6205,
                    audioKbps = 128,
                    maxHeight = null
                ),
                VideoBitrate(
                    videoKbps = 1138,
                    audioKbps = 128,
                    maxHeight = 720
                ),
                VideoBitrate(
                    videoKbps = 147,
                    audioKbps = 64,
                    maxHeight = 480
                ),
                "linux-x86_64",
                "macosx-arm64",
                "windows-x86_64",
                null
            ),
            actual = listOf(
                VideoBitrate.of(
                    seconds = 120.0,
                    maxBytes = limit
                ),
                VideoBitrate.of(
                    seconds = 600.0,
                    maxBytes = limit
                ),
                VideoBitrate.of(
                    seconds = 3600.0,
                    maxBytes = limit
                ),
                FfmpegTool.of(
                    system = "linux",
                    architecture = "amd64"
                )?.folder,
                FfmpegTool.of(
                    system = "macosx",
                    architecture = "aarch64"
                )?.folder,
                FfmpegTool.of(
                    system = "windows",
                    architecture = "amd64"
                )?.folder,
                FfmpegTool.of(
                    system = "freebsd",
                    architecture = "amd64"
                )
            )
        )
    }
}
