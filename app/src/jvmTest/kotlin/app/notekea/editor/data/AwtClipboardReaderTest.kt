package app.notekea.editor.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

/** On Wayland the clipboard may never give its image: the read gives up long before Java's 10 seconds. */
class AwtClipboardReaderTest {
    @Test
    fun `a selection timeout makes Java give up on a clipboard owner after it, instead of ten seconds`() {
        val previous = System.getProperty(TIMEOUT_PROPERTY)

        try {
            AwtClipboardReader(selectionTimeout = 1.seconds)

            assertEquals(
                expected = "1000",
                actual = System.getProperty(TIMEOUT_PROPERTY)
            )
        } finally {
            if (previous == null) {
                System.clearProperty(TIMEOUT_PROPERTY)
            } else {
                System.setProperty(TIMEOUT_PROPERTY, previous)
            }
        }
    }

    companion object {
        private const val TIMEOUT_PROPERTY = "sun.awt.datatransfer.timeout"
    }
}
