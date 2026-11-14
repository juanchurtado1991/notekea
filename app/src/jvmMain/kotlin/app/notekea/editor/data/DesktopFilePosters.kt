package app.notekea.editor.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.notes.domain.RelativePath
import app.notekea.editor.domain.FilePosters
import app.notekea.editor.domain.FilesShownHere
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

/**
 * [FilePosters] on the desktop: a PDF's first page ([pdfPage], a PNG) and a video's first frame that is not black
 * ([VideoPoster] over [videoFrame]), each made once and kept in [folder] under the file's place, size and date, so a
 * file that changes gets a new one.
 */
internal class DesktopFilePosters(
    private val folder: Path,
    private val pdfPage: suspend (vaultPath: String, path: RelativePath) -> ByteArray?,
    private val videoFrame: suspend (file: String, seconds: Int) -> ByteArray?,
    private val dispatchers: AppDispatchers
) : FilePosters {
    // One poster made at a time: a note with ten videos does not start ten FFmpegs.
    private val making = Mutex()

    override suspend fun of(
        vaultPath: String,
        path: RelativePath
    ): ByteArray? = making.withLock {
        val file = File(vaultPath, path.value)
        if (!file.isFile) return@withLock null
        val kept = folder.resolve(nameOf(file = file))

        withContext(context = dispatchers.io) {
            if (Files.isRegularFile(kept)) Files.readAllBytes(kept) else null
        }
            // Made to its end and kept even when who asked stopped waiting (the note was typed in): each keystroke
            // threw the poster away as it was ready and started FFmpeg again (the code review of 2026-11-13).
            ?: withContext(context = NonCancellable) {
                made(
                    vaultPath = vaultPath,
                    path = path,
                    file = file
                )?.also { poster ->
                    withContext(context = dispatchers.io) {
                        Files.createDirectories(folder)
                        Files.write(kept, poster)
                    }
                }
            }
    }

    private suspend fun made(
        vaultPath: String,
        path: RelativePath,
        file: File
    ): ByteArray? = when {
        FilesShownHere.isPdf(path = path) -> pdfPage(vaultPath, path)

        FilesShownHere.isVideo(path = path) ->
            VideoPoster(frameAt = { videoFrame(file.absolutePath, it) }).pick()

        else -> null
    }

    private fun nameOf(file: File): String {
        val key = "${file.absolutePath}|${file.length()}|${file.lastModified()}"

        val digest = MessageDigest
            .getInstance(SHA_256)
            .digest(key.toByteArray())
            .joinToString(separator = "") { HEX.format(it) }

        return "$digest$PNG"
    }

    private companion object {
        const val SHA_256 = "SHA-256"
        const val HEX = "%02x"
        const val PNG = ".png"
    }
}
