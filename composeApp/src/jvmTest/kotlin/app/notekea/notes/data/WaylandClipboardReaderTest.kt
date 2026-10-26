package app.notekea.notes.data

import app.notekea.notes.domain.FakeClipboardReader
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Smoke finding of 1.8-07 on COSMIC (Wayland): Mod+V with a screenshot copied did nothing, because Java reads the
 * clipboard through XWayland, which passes on text only. The screenshot is read with `wl-paste` instead.
 */
class WaylandClipboardReaderTest {
    private val png = encoded(format = "png")
    private val jpeg = encoded(format = "jpg")

    @Test
    fun `a screenshot that Java cannot see is read with wl-paste, as PNG`() {
        val commands = mutableListOf<List<String>>()
        val reader = WaylandClipboardReader(primary = FakeClipboardReader(text = "word")) { command ->
            commands += command
            if (command.last() == "--list-types") "text/plain\nimage/png\n".encodeToByteArray() else png
        }

        assertContentEquals(expected = png, actual = reader.image())
        assertEquals(
            expected = listOf(
                listOf("wl-paste", "--list-types"),
                listOf("wl-paste", "--no-newline", "--type", "image/png")
            ),
            actual = commands
        )
        assertEquals(expected = "word", actual = reader.text())
    }

    @Test
    fun `another image type becomes PNG, and no image, no wl-paste or an image Java sees need nothing more`() {
        val jpegOnly = WaylandClipboardReader(primary = FakeClipboardReader()) { command ->
            if (command.last() == "--list-types") "image/jpeg".encodeToByteArray() else jpeg
        }
        val converted = ImageIO.read(ByteArrayInputStream(jpegOnly.image()))

        assertEquals(expected = listOf(4, 3), actual = listOf(converted.width, converted.height))
        assertNull(
            actual = WaylandClipboardReader(primary = FakeClipboardReader()) {
                "text/plain".encodeToByteArray()
            }.image()
        )
        assertNull(actual = WaylandClipboardReader(primary = FakeClipboardReader()) { null }.image())
        assertContentEquals(
            expected = png,
            actual = WaylandClipboardReader(primary = FakeClipboardReader(image = png)) { error("not needed") }.image()
        )
    }

    @Test
    fun `readCommand returns what a command printed, and null when it fails or does not exist`() {
        val java = ProcessHandle.current().info().command().get()

        assertTrue(actual = readCommand(command = listOf(java, "--version"))?.decodeToString().orEmpty().isNotBlank())
        assertNull(actual = readCommand(command = listOf(java, "--no-such-option")))
        assertNull(actual = readCommand(command = listOf("notekea-no-such-command")))
    }

    private fun encoded(
        format: String
    ): ByteArray = ByteArrayOutputStream().use { output ->
        ImageIO.write(BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB), format, output)
        output.toByteArray()
    }
}
