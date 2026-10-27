package app.notekea.core.presentation.about

import app.notekea.core.presentation.preview.PreviewPlatformConventions
import kotlin.test.Test
import kotlin.test.assertEquals

class AboutShortcutsTest {
    private val groups = AboutShortcuts.groups(conventions = PreviewPlatformConventions.linux)

    @Test
    fun `About lists notes, writing and the workspace, opening a note first, and no shortcut twice`() {
        val shortcuts = groups.flatMap { group -> group.shortcuts.map { it.shortcut } }

        assertEquals(
            expected = Triple(listOf(10, 8, 7), PreviewPlatformConventions.linux.quickSwitcherShortcut, shortcuts.size),
            actual = Triple(
                groups.map { it.shortcuts.size },
                groups.first().shortcuts.first().shortcut,
                shortcuts.distinct().size
            )
        )
    }
}
