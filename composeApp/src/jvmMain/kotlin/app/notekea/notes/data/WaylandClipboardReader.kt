package app.notekea.notes.data

import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.ClipboardReader
import app.notekea.notes.domain.NotesError
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * [ClipboardReader] on a Wayland desktop. Java reads the clipboard through XWayland, which does not pass images on:
 * without focus it offers none, with focus it offers one and Java waits seconds for it until the owner times out. So a
 * copied screenshot is read with `wl-paste` (wl-clipboard) first, PNG first, other image types turned into PNG, and
 * [primary] is asked only when `wl-paste` finds no image or is not installed; when [primary] offers an image it cannot
 * give and `wl-paste` is missing, the user is told to install wl-clipboard. [readCommand] runs a command and returns
 * its output, or null when it could not run (see `ProcessCommands.kt`). Text comes from [primary].
 */
class WaylandClipboardReader(
    private val primary: ClipboardReader,
    private val readCommand: (List<String>) -> ByteArray?
) : ClipboardReader {
    override fun image(): Outcome<ByteArray?, NotesError> {
        waylandImage()?.let { return Outcome.Success(value = it) }
        val image = primary.image()
        val isMissing = image is Outcome.Failure && readCommand(VERSION) == null
        return if (isMissing) Outcome.Failure(error = NotesError.ClipboardToolMissing(tool = TOOL)) else image
    }

    override fun text(): String? = primary.text()

    private fun waylandImage(): ByteArray? {
        val types = readCommand(LIST_TYPES)?.decodeToString()?.lines()?.map { it.trim() }.orEmpty()
        val type = types.firstOrNull { it == PNG_TYPE }
            ?: types.firstOrNull { it.startsWith(prefix = IMAGE_TYPE_PREFIX) }
            ?: return null
        val bytes = readCommand(PASTE + type)?.takeIf { it.isNotEmpty() } ?: return null
        return if (type == PNG_TYPE) bytes else png(bytes = bytes)
    }

    /** The image in [bytes] encoded as PNG, or null when Java cannot read its format. */
    private fun png(
        bytes: ByteArray
    ): ByteArray? {
        val image = ImageIO.read(ByteArrayInputStream(bytes)) ?: return null
        return ByteArrayOutputStream().use { output ->
            ImageIO.write(image, PNG_FORMAT, output)
            output.toByteArray()
        }
    }

    companion object {
        /** The environment variable a Wayland session sets, naming its display. */
        const val WAYLAND_DISPLAY = "WAYLAND_DISPLAY"
        private const val IMAGE_TYPE_PREFIX = "image/"
        private const val PNG_FORMAT = "png"
        private const val PNG_TYPE = "image/png"
        private const val TOOL = "wl-clipboard"
        private val LIST_TYPES = listOf("wl-paste", "--list-types")
        private val PASTE = listOf("wl-paste", "--no-newline", "--type")
        private val VERSION = listOf("wl-paste", "--version")
    }
}
