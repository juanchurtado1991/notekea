package app.notekea.editor.data

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NotesError
import app.notekea.editor.domain.ClipboardReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * [ClipboardReader] on a Wayland desktop, where Java reads the clipboard through XWayland, which does not pass images
 * on. A copied image is read with `wl-paste` first, turned into PNG, and [primary] is asked only when that finds none
 * or is not installed; an image neither can give tells the user to install wl-clipboard. Text comes from [primary].
 */
class WaylandClipboardReader(
    private val primary: ClipboardReader,
    private val readCommand: (List<String>) -> ByteArray?
) : ClipboardReader {
    override fun image(): Outcome<ByteArray?, NotesError> {
        waylandImage()?.let {
            return Outcome.Success(value = it)
        }

        val image = primary.image()
        val isMissing = image is Outcome.Failure && readCommand(VERSION) == null

        return if (isMissing) {
            Outcome.Failure(
                error = NotesError.ClipboardToolMissing(tool = TOOL)
            )
        } else {
            image
        }
    }

    override fun text(): String? = primary.text()

    private fun waylandImage(): ByteArray? {
        val types = readCommand(LIST_TYPES)
            ?.decodeToString()
            ?.lines()
            ?.map { it.trim() }
            .orEmpty()

        val type = types.firstOrNull {
            it == PNG_TYPE
        }
            ?: types.firstOrNull { it.startsWith(prefix = IMAGE_TYPE_PREFIX) }
            ?: return null

        val bytes = readCommand(PASTE + type)?.takeIf { it.isNotEmpty() } ?: return null

        return if (type == PNG_TYPE) bytes else png(bytes = bytes)
    }

    /** The image in [bytes] encoded as PNG, or null when Java cannot read its format. */
    private fun png(bytes: ByteArray): ByteArray? {
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
