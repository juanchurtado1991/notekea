package app.notekea.notes.data

import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.ClipboardReader
import app.notekea.notes.domain.NotesError
import java.awt.HeadlessException
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.time.Duration

/**
 * [ClipboardReader] on the AWT system clipboard: an image is drawn into a buffer and encoded as PNG, the format Obsidian
 * saves pasted images in. A clipboard another app holds busy reads as empty; an image it offers but does not give
 * within [selectionTimeout] (when set; Java's own wait is 10 seconds) is reported as unreadable.
 */
class AwtClipboardReader(
    selectionTimeout: Duration? = null
) : ClipboardReader {
    init {
        selectionTimeout?.let {
            System.setProperty(SELECTION_TIMEOUT_PROPERTY, it.inWholeMilliseconds.toString())
        }
    }

    override fun image(): Outcome<ByteArray?, NotesError> {
        val clipboard = clipboard() ?: return Outcome.Success(value = null)
        if (!isAvailable(flavor = DataFlavor.imageFlavor)) return Outcome.Success(value = null)
        val image = try {
            clipboard.getData(DataFlavor.imageFlavor) as? Image
        } catch (_: IOException) {
            return Outcome.Failure(error = NotesError.ClipboardImageUnreadable)
        } catch (_: UnsupportedFlavorException) {
            null
        } catch (_: IllegalStateException) {
            null
        } ?: return Outcome.Success(value = null)
        return Outcome.Success(value = png(image = image))
    }

    override fun text(): String? = try {
        clipboard()?.takeIf { isAvailable(flavor = DataFlavor.stringFlavor) }
            ?.getData(DataFlavor.stringFlavor) as? String
    } catch (_: IOException) {
        null
    } catch (_: UnsupportedFlavorException) {
        null
    } catch (_: IllegalStateException) {
        null
    }

    private fun png(
        image: Image
    ): ByteArray? {
        val width = image.getWidth(null)
        val height = image.getHeight(null)
        if (width <= 0 || height <= 0) return null
        val buffered = image as? BufferedImage ?: BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB).also {
            it.createGraphics().apply {
                drawImage(image, 0, 0, null)
                dispose()
            }
        }
        return ByteArrayOutputStream().use { output ->
            ImageIO.write(buffered, PNG_FORMAT, output)
            output.toByteArray()
        }
    }

    private fun isAvailable(
        flavor: DataFlavor
    ): Boolean = try {
        clipboard()?.isDataFlavorAvailable(flavor) == true
    } catch (_: IllegalStateException) {
        false
    }

    private fun clipboard() = try {
        Toolkit.getDefaultToolkit().systemClipboard
    } catch (_: HeadlessException) {
        null
    }

    companion object {
        private const val PNG_FORMAT = "png"

        // How long Java's X11 clipboard waits for the owner of a selection, in milliseconds.
        private const val SELECTION_TIMEOUT_PROPERTY = "sun.awt.datatransfer.timeout"
    }
}
