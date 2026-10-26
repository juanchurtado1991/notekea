package app.notekea.notes.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

/** Smoke finding of 1.8-07 on Wayland: Java waited 10 seconds for an image the clipboard never gave. */
class AwtClipboardReaderTest {
    @Test
    fun `a selection timeout makes Java give up on a clipboard owner after it, instead of ten seconds`() {
        val previous = System.getProperty(TIMEOUT_PROPERTY)
        try {
            AwtClipboardReader(selectionTimeout = 1.seconds)

            assertEquals(expected = "1000", actual = System.getProperty(TIMEOUT_PROPERTY))
        } finally {
            if (previous ==
                null
            ) {
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
