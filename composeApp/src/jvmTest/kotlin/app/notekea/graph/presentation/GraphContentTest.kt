package app.notekea.graph.presentation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.platform.LocalShortcutDispatcher
import app.notekea.core.presentation.platform.ShortcutDispatcher
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/** The graph tab beside an editor (`ROADMAP.md` 2.7). */
class GraphContentTest {
    // Review: in a split, the graph took Mod+F from the note the keyboard was in.
    @Test
    fun `Mod+F finds on the graph only while its pane is the active one`() {
        assertEquals(expected = listOf(true, false), actual = listOf(offersFind(isActive = true), offersFind(false)))
    }

    private fun offersFind(
        isActive: Boolean
    ): Boolean {
        val dispatcher = ShortcutDispatcher()
        val conventions = PreviewPlatformConventions.linux
        var isOffered = false
        UiThread.run {
            ImageComposeScene(width = WIDTH, height = HEIGHT) {
                CompositionLocalProvider(LocalShortcutDispatcher provides dispatcher) {
                    NotekeaTheme(darkTheme = false) {
                        GraphContent(
                            state = GraphPreviewData.state,
                            platformConventions = conventions,
                            onIntent = {},
                            isActive = isActive
                        )
                    }
                }
            }.use { scene ->
                scene.render(nanoTime = 0L)
                isOffered = dispatcher.isAvailable(shortcut = conventions.findShortcut)
            }
        }
        return isOffered
    }

    private companion object {
        const val WIDTH = 800
        const val HEIGHT = 500
    }
}
