package app.notekea.notes.data

import app.notekea.notes.domain.ClipboardReader
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.imageio.ImageIO

/**
 * [ClipboardReader] on the AWT system clipboard: an image is drawn into a buffer and encoded as PNG, the format Obsidian
 * saves pasted images in. A clipboard another app holds busy reads as empty.
 */
class AwtClipboardReader : ClipboardReader {
    override fun image(): ByteArray? {
        val image = contents(flavor = DataFlavor.imageFlavor) as? Image ?: return null
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

    override fun text(): String? = contents(flavor = DataFlavor.stringFlavor) as? String

    private fun contents(
        flavor: DataFlavor
    ): Any? = try {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        if (clipboard.isDataFlavorAvailable(flavor)) clipboard.getData(flavor) else null
    } catch (_: IllegalStateException) {
        null
    } catch (_: UnsupportedFlavorException) {
        null
    } catch (_: IOException) {
        null
    }

    companion object {
        private const val PNG_FORMAT = "png"
    }
}
