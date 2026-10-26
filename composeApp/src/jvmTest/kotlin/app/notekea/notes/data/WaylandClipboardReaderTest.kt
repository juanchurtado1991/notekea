package app.notekea.notes.data

import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.ClipboardReader
import app.notekea.notes.domain.FakeClipboardReader
import app.notekea.notes.domain.NotesError
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
 * Smoke findings of 1.8-07 on COSMIC (Wayland): Mod+V with a screenshot copied did nothing, then took seconds, because
 * Java reads the clipboard through XWayland, which passes on text only or times out. `wl-paste` reads it first, and
 * when it is missing (GNOME does not install it) the user is told to install wl-clipboard.
 */
class WaylandClipboardReaderTest {
    private val png = encoded(format = "png")
    private val jpeg = encoded(format = "jpg")

    @Test
    fun `a screenshot is read with wl-paste, as PNG, before Java, which would wait seconds for it`() {
        val commands = mutableListOf<List<String>>()
        val java = WaitingClipboardReader(text = "word")
        val reader = WaylandClipboardReader(primary = java) { command ->
            commands += command
            if (command.last() == "--list-types") "text/plain\nimage/png\n".encodeToByteArray() else png
        }

        assertContentEquals(expected = png, actual = bytesOf(image = reader.image()))
        assertEquals(
            expected = listOf(
                listOf("wl-paste", "--list-types"),
                listOf("wl-paste", "--no-newline", "--type", "image/png")
            ),
            actual = commands
        )
        assertEquals(expected = 0, actual = java.imageReads)
        assertEquals(expected = "word", actual = reader.text())
    }

    @Test
    fun `another image type becomes PNG, and with no image or no wl-paste Java is asked`() {
        val jpegOnly = WaylandClipboardReader(primary = FakeClipboardReader()) { command ->
            if (command.last() == "--list-types") "image/jpeg".encodeToByteArray() else jpeg
        }
        val converted = ImageIO.read(ByteArrayInputStream(bytesOf(image = jpegOnly.image())))

        assertEquals(expected = listOf(4, 3), actual = listOf(converted.width, converted.height))
        assertNull(
            actual = bytesOf(
                image = WaylandClipboardReader(primary = FakeClipboardReader()) {
                    "text/plain".encodeToByteArray()
                }.image()
            )
        )
        assertNull(actual = bytesOf(image = WaylandClipboardReader(primary = FakeClipboardReader()) { null }.image()))
        assertContentEquals(
            expected = png,
            actual = bytesOf(
                image = WaylandClipboardReader(primary = FakeClipboardReader(image = png)) {
                    null
                }.image()
            )
        )
    }

    @Test
    fun `an image Java cannot give asks to install wl-clipboard when wl-paste is missing, else is unreadable`() {
        val missing = WaylandClipboardReader(primary = WaitingClipboardReader(text = "word")) { null }
        val installedButEmpty = WaylandClipboardReader(primary = WaitingClipboardReader(text = "word")) { command ->
            if (command.last() == "--version") "wl-clipboard 2.2.1".encodeToByteArray() else null
        }

        assertEquals(
            expected = listOf<Outcome<ByteArray?, NotesError>>(
                Outcome.Failure(error = NotesError.ClipboardToolMissing(tool = "wl-clipboard")),
                Outcome.Failure(error = NotesError.ClipboardImageUnreadable)
            ),
            actual = listOf(missing.image(), installedButEmpty.image())
        )
    }

    @Test
    fun `readCommand returns what a command printed, and null when it fails or does not exist`() {
        val java = ProcessHandle.current().info().command().get()

        assertTrue(actual = readCommand(command = listOf(java, "--version"))?.decodeToString().orEmpty().isNotBlank())
        assertNull(actual = readCommand(command = listOf(java, "--no-such-option")))
        assertNull(actual = readCommand(command = listOf("notekea-no-such-command")))
    }

    private fun bytesOf(
        image: Outcome<ByteArray?, NotesError>
    ): ByteArray? = (image as Outcome.Success).value

    private fun encoded(
        format: String
    ): ByteArray = ByteArrayOutputStream().use { output ->
        ImageIO.write(BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB), format, output)
        output.toByteArray()
    }

    /** Java's clipboard on XWayland with the window focused: it offers the image, then times out reading it. */
    private class WaitingClipboardReader(
        private val text: String
    ) : ClipboardReader {
        var imageReads = 0

        override fun image(): Outcome<ByteArray?, NotesError> {
            imageReads++
            return Outcome.Failure(error = NotesError.ClipboardImageUnreadable)
        }

        override fun text(): String = text
    }
}
