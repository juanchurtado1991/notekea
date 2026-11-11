package app.notekea.editor.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.editor.domain.CompressError
import app.notekea.editor.domain.CompressStep
import app.notekea.editor.domain.VideoCompressor
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID

/**
 * [VideoCompressor] with FFmpeg ([FfmpegTool], installed by [FfmpegInstaller] into [toolsFolder] the first time):
 * H.264 (OpenH264) and AAC in an `.mp4`, at the bit rate that fits ([VideoBitrate]), tried again a little lower when
 * the copy is still too big; the copy goes in [workFolder]. Four threads, so the computer stays usable meanwhile.
 */
class FfmpegVideoCompressor internal constructor(
    private val installer: FfmpegInstaller,
    private val workFolder: Path,
    private val dispatchers: AppDispatchers
) : VideoCompressor {
    private val logger = Logger.withTag(tag = LOG_TAG)

    override suspend fun isOverLimit(
        source: String,
        maxBytes: Long
    ): Boolean = withContext(context = dispatchers.io) {
        try {
            Files.size(Path.of(source)) > maxBytes
        } catch (_: IOException) {
            false
        }
    }

    override suspend fun discard(path: String) = withContext(context = dispatchers.io) {
        val file = Path.of(path)

        try {
            Files.deleteIfExists(file)
            file.parent?.let { Files.deleteIfExists(it) }
        } catch (_: IOException) {
            logger.w { "A compressed copy could not be deleted" }
        }

        Unit
    }

    override fun compress(
        source: String,
        maxBytes: Long
    ): Flow<CompressStep> = channelFlow {
        val program = installer.program { send(CompressStep.Downloading(fraction = it)) }
            ?: return@channelFlow send(CompressStep.Failed(error = CompressError.ToolUnavailable))

        val seconds = durationOf(
            program = program,
            source = source
        )
            ?: return@channelFlow send(CompressStep.Failed(error = CompressError.Failed))

        val name = File(source).nameWithoutExtension
        val target = workFolder.resolve(UUID.randomUUID().toString()).resolve("$name$MP4")
        Files.createDirectories(target.parent)

        var bitrate = VideoBitrate.of(
            seconds = seconds,
            maxBytes = maxBytes
        )

        repeat(times = ATTEMPTS) {
            val isMade = encode(
                program = program,
                source = source,
                target = target,
                bitrate = bitrate,
                seconds = seconds
            )

            if (isMade && Files.size(target) <= maxBytes) {
                return@channelFlow send(CompressStep.Done(path = target.toString()))
            }

            bitrate = bitrate.copy(videoKbps = bitrate.videoKbps * LOWER_PERCENT / PERCENT)
        }

        logger.w { "A video could not be made small enough" }
        send(CompressStep.Failed(error = CompressError.Failed))
    }.flowOn(context = dispatchers.io)

    // The video's length in seconds, as FFmpeg reads it ("Duration: 00:02:00.00").
    private suspend fun durationOf(
        program: String,
        source: String
    ): Double? {
        val output = run(arguments = listOf(program, "-hide_banner", "-i", source)) { }
        val match = DURATION.find(input = output) ?: return null
        val (hours, minutes, seconds) = match.destructured

        val whole = hours.toDouble() * SECONDS_IN_HOUR + minutes.toDouble() * SECONDS_IN_MINUTE

        return whole + seconds.toDouble()
    }

    private suspend fun ProducerScope<CompressStep>.encode(
        program: String,
        source: String,
        target: Path,
        bitrate: VideoBitrate,
        seconds: Double
    ): Boolean {
        val video = bitrate.videoKbps

        val scale = bitrate
            .maxHeight
            ?.let { listOf("-vf", "scale=-2:'min($it,ih)'") }
            .orEmpty()

        val arguments =
            listOf(program, "-hide_banner", "-nostats", "-y", "-i", source, "-threads", THREADS) +
                listOf("-c:v", "libopenh264", "-pix_fmt", "yuv420p", "-b:v", "${video}k") +
                listOf("-maxrate", "${video * MAX_RATE_PERCENT / PERCENT}k", "-bufsize", "${video * 2}k") + scale +
                listOf("-c:a", "aac", "-b:a", "${bitrate.audioKbps}k", "-movflags", "+faststart") +
                listOf("-progress", "pipe:1", target.toString())

        run(arguments = arguments) { line ->
            PROGRESS.matchEntire(input = line)?.let {
                val done = it.groupValues[1].toDouble() / MICROS / seconds
                trySend(CompressStep.Compressing(fraction = done.toFloat().coerceIn(0f, 1f)))
            }
        }

        return Files.isRegularFile(target) && Files.size(target) > 0
    }

    /** Runs FFmpeg with [arguments], giving each line it writes to [onLine]; stopped when the collection is. */
    private suspend fun run(
        arguments: List<String>,
        onLine: (String) -> Unit
    ): String {
        val process = ProcessBuilder(arguments).redirectErrorStream(true).start()
        val job = currentCoroutineContext()[Job]
        val stop = job?.invokeOnCompletion { process.destroyForcibly() }
        val output = StringBuilder()

        try {
            process
                .inputStream
                .bufferedReader()
                .useLines { lines ->
                    lines.forEach { line ->
                        currentCoroutineContext().ensureActive()
                        output.appendLine(line)
                        onLine(line)
                    }
                }

            process.waitFor()
        } finally {
            stop?.dispose()
        }

        return output.toString()
    }

    private companion object {
        const val LOG_TAG = "FfmpegVideoCompressor"
        const val MP4 = ".mp4"
        const val THREADS = "4"
        const val ATTEMPTS = 3
        const val PERCENT = 100
        const val LOWER_PERCENT = 85
        const val MAX_RATE_PERCENT = 110
        const val MICROS = 1_000_000.0
        const val SECONDS_IN_HOUR = 3600
        const val SECONDS_IN_MINUTE = 60
        val DURATION = Regex(pattern = "Duration: (\\d+):(\\d+):(\\d+(?:\\.\\d+)?)")
        val PROGRESS = Regex(pattern = "out_time_us=(\\d+)")
    }
}
