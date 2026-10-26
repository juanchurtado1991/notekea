package app.notekea.notes.data

import app.notekea.notes.domain.ClipboardReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * [ClipboardReader] on a Wayland desktop. Java reads the clipboard through XWayland, which passes text on but not
 * images, so when [primary] finds no image a copied screenshot is read with `wl-paste` (wl-clipboard), PNG first,
 * other image types turned into PNG. [readCommand] runs a command and returns its output, or null when it could not
 * run (see `ProcessCommands.kt`). Text comes from [primary].
 */
class WaylandClipboardReader(
    private val primary: ClipboardReader,
    private val readCommand: (List<String>) -> ByteArray?
) : ClipboardReader {
    override fun image(): ByteArray? = primary.image() ?: waylandImage()

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
        private val LIST_TYPES = listOf("wl-paste", "--list-types")
        private val PASTE = listOf("wl-paste", "--no-newline", "--type")
    }
}
