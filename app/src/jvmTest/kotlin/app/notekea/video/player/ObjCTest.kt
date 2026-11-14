package app.notekea.video.player

import com.sun.jna.Platform
import org.junit.Assume.assumeTrue
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The YouTube player finds Notekea's own window among the program's by its title (the code review of 2026-11-13):
 * the list of windows is read as any list of Cocoa's, which this checks on one made here. macOS only.
 */
class ObjCTest {
    @Test
    fun `the objects of a list are read in its order, and a list with none gives none`() {
        assumeTrue(Platform.isMac())
        val arrays = ObjC.classOf(name = "NSMutableArray")!!
        val list = ObjC.send(arrays, "array")!!
        val none = ObjC.items(array = list).size

        listOf("Notekea", "Settings").forEach {
            ObjC.tell(list, "addObject:", ObjC.stringOf(text = it))
        }

        assertEquals(
            expected = listOf<Any>(0, listOf("Notekea", "Settings")),
            actual = listOf(none, ObjC.items(array = list).map { ObjC.textOf(string = it) })
        )
    }

    @Test
    fun `with no window of that title the player is not shown, and nothing fails`() {
        assumeTrue(Platform.isMac())

        val shown = MacWebView.show(
            html = "<p>x</p>",
            origin = "https://app.notekea",
            windowTitle = "No such window"
        )

        // The tests show no window at all: none titled so, none with the keyboard, none to fall back to.
        assertEquals(
            expected = null,
            actual = shown
        )
    }
}
