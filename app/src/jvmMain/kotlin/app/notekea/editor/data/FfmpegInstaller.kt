package app.notekea.editor.data

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CancellationException
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.zip.ZipFile

/**
 * FFmpeg on this computer: in [folder] once installed, else downloaded the first time a video needs it (#141) from
 * Maven Central, checked against [FfmpegTool.sha256] and its size, and only then unpacked; a download that does not
 * match is thrown away. [tool] is this system's, null where there is none (then no video is compressed).
 */
internal class FfmpegInstaller(
    private val folder: Path,
    private val tool: FfmpegTool?
) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private val installed: Path get() = folder.resolve(FfmpegTool.VERSION)

    private val client by lazy {
        HttpClient(engineFactory = OkHttp) {
            expectSuccess = false

            install(HttpTimeout) {
                connectTimeoutMillis = CONNECT_MILLIS
                socketTimeoutMillis = SOCKET_MILLIS
            }
        }
    }

    /** The FFmpeg program, installed first when needed with its progress on [onDownload]; null when it cannot be. */
    suspend fun program(onDownload: suspend (Float) -> Unit): String? {
        val tool = tool ?: return null
        val program = installed.resolve(tool.program)
        if (Files.isRegularFile(installed.resolve(DONE))) return program.toString()

        return try {
            val jar = download(
                tool = tool,
                onDownload = onDownload
            ) ?: return null

            unpack(
                tool = tool,
                jar = jar
            )

            Files.deleteIfExists(jar)
            File(program.toString()).setExecutable(true)
            Files.writeString(installed.resolve(DONE), tool.sha256)
            program.toString()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: IOException) {
            logger.w { "FFmpeg could not be installed: ${exception::class.simpleName}" }
            null
        }
    }

    private suspend fun download(
        tool: FfmpegTool,
        onDownload: suspend (Float) -> Unit
    ): Path? {
        Files.createDirectories(folder)
        val jar = folder.resolve("${tool.jarName}$PART")
        val digest = MessageDigest.getInstance(SHA_256)
        var received = 0L
        var shownPercent = -1L

        client.prepareGet(urlString = tool.url).execute { response ->
            if (!response.status.isSuccess()) return@execute
            val channel = response.bodyAsChannel()
            val buffer = ByteArray(BUFFER_BYTES)

            Files.newOutputStream(jar).use { out ->
                while (received <= tool.size) {
                    val read = channel.readAvailable(buffer)
                    if (read < 0) break
                    if (read == 0) continue
                    out.write(buffer, 0, read)
                    digest.update(buffer, 0, read)
                    received += read

                    // Once a percent, so the note above which it shows is not redrawn for every piece.
                    val percent = received * PERCENT / tool.size

                    if (percent != shownPercent) {
                        shownPercent = percent
                        onDownload((received.toFloat() / tool.size).coerceAtMost(maximumValue = 1f))
                    }
                }
            }
        }

        val sha = digest.digest().joinToString(separator = "") { "%02x".format(it) }
        val isWhole = received == tool.size && sha == tool.sha256
        if (isWhole) return jar

        logger.w { "FFmpeg's download did not match its checksum" }
        Files.deleteIfExists(jar)

        return null
    }

    // The program and its libraries, from the jar's folder for this system, without Java's own bindings.
    private fun unpack(
        tool: FfmpegTool,
        jar: Path
    ) {
        Files.createDirectories(installed)
        val prefix = "$JAR_ROOT/${tool.folder}/"

        ZipFile(jar.toFile()).use { zip ->
            zip
                .entries()
                .asSequence()
                .filter {
                    !it.isDirectory &&
                        it.name.startsWith(prefix = prefix) &&
                        JNI !in it.name
                }
                .forEach { entry ->
                    val target = installed.resolve(entry.name.removePrefix(prefix = prefix))
                    Files.createDirectories(target.parent)

                    zip.getInputStream(entry).use {
                        Files.copy(it, target, StandardCopyOption.REPLACE_EXISTING)
                    }
                }
        }
    }

    private companion object {
        const val LOG_TAG = "FfmpegInstaller"
        const val DONE = "installed"
        const val PART = ".part"
        const val SHA_256 = "SHA-256"
        const val JAR_ROOT = "org/bytedeco/ffmpeg"
        const val JNI = "jni"
        const val BUFFER_BYTES = 64 * 1024
        const val PERCENT = 100
        const val CONNECT_MILLIS = 15_000L
        const val SOCKET_MILLIS = 30_000L
    }
}
