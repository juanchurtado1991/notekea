package app.notekea.editor.data

import app.notekea.core.domain.AppDispatchers
import co.touchlab.kermit.Logger
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * A video's frames as pictures, with the FFmpeg Notekea downloads once ([FfmpegInstaller], the one that makes videos
 * smaller): the frame at a second as a PNG at most [WIDTH] wide, null past the video's end or when FFmpeg is not
 * there.
 */
internal class FfmpegVideoFrames(
    private val installer: FfmpegInstaller,
    private val dispatchers: AppDispatchers
) {
    private val logger = Logger.withTag(tag = LOG_TAG)

    suspend fun frameAt(
        file: String,
        seconds: Int
    ): ByteArray? = withContext(context = dispatchers.io) {
        val program = installer.program(onDownload = {}) ?: return@withContext null

        val arguments = listOf(
            program, "-hide_banner", "-loglevel", "error", "-ss", seconds.toString(), "-i", file,
            "-frames:v", "1", "-vf", "scale=$WIDTH:-2", "-f", "image2pipe", "-vcodec", "png", "-"
        )

        try {
            val process = ProcessBuilder(arguments)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start()

            try {
                val bytes = process.inputStream.readBytes()
                val hasEnded = process.waitFor(WAIT_SECONDS, TimeUnit.SECONDS)

                // A picture FFmpeg did not finish is half a picture, kept for good as the video's poster.
                val isWhole =
                    hasEnded &&
                        process.exitValue() == 0 &&
                        bytes.isNotEmpty()

                bytes.takeIf { isWhole }
            } finally {
                // Stopped while it read (the note closed), FFmpeg is still running: end it here.
                if (process.isAlive) process.destroyForcibly()
            }
        } catch (exception: IOException) {
            logger.w { "A video's frame could not be read: ${exception::class.simpleName}" }
            null
        }
    }

    private companion object {
        const val LOG_TAG = "FfmpegVideoFrames"
        const val WIDTH = 480
        const val WAIT_SECONDS = 20L
    }
}
