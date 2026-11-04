package app.notekea.notes.presentation.index

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.clickText
import app.notekea.core.presentation.texts
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.notes.domain.IndexProgress
import app.notekea.core.notes.domain.IndexRunError
import app.notekea.core.notes.domain.RelativePath
import app.notekea.notes.domain.SkipReason
import app.notekea.notes.domain.SkippedNote
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

/**
 * The search index's status, details and notice (`ROADMAP.md` 2.3, design B), rendered headless: what each state says,
 * and what a click on the status, Rebuild index and Dismiss send.
 */
class SearchIndexScreenTest {
    private var frame = 0L

    @Test
    fun `the status says how far a run is, how many notes are ready, or that search is unavailable`() {
        val running = SearchIndexState(isRunning = true, progress = IndexProgress(total = 1204, done = 337))
        val ready = SearchIndexState(readyNotes = 1204)
        val failed = SearchIndexState(readyNotes = 3, error = IndexRunError.Model)

        val shown = listOf(running, ready, failed).map { state ->
            render { SearchIndexStatus(state = state, onIntent = {}) }.texts
        }

        assertEquals(
            expected = listOf("Indexing · 27 %", "1,204 notes ready to search", "Search unavailable"),
            actual = shown.map { it.single() }
        )
    }

    @Test
    fun `a click on the status opens its details, a second one closes them, and Rebuild index starts over`() {
        val intents = mutableListOf<SearchIndexIntent>()
        val start = SearchIndexState(
            isRunning = true,
            progress = IndexProgress(
                total = 1204,
                done = 340,
                skipped = listOf(SkippedNote(HUGE, SkipReason.TOO_LARGE))
            ),
            remaining = 2.minutes
        )

        val opened = render(clicks = listOf(STATUS)) { DetailsHost(start = start, intents = intents) }.texts
        val closed = render(clicks = listOf(STATUS, STATUS)) { DetailsHost(start = start, intents = intents) }.texts
        render(clicks = listOf(STATUS, "Rebuild index")) { DetailsHost(start = start, intents = intents) }

        assertTrue(actual = "340 of 1,204 notes · about 2 minutes left" in opened, message = opened.toString())
        assertTrue(actual = "Huge export.md — larger than 5 MB" in opened, message = opened.toString())
        assertTrue(actual = "Rebuild index" !in closed, message = closed.toString())
        assertEquals(expected = SearchIndexIntent.OnRebuildClick, actual = intents.last())
    }

    /** The status with its details opening and closing as the ViewModel does on its intents. */
    @Composable
    private fun DetailsHost(
        start: SearchIndexState,
        intents: MutableList<SearchIndexIntent>
    ) {
        var state by remember { mutableStateOf(value = start) }
        SearchIndexStatus(state = state) { intent ->
            intents += intent
            state = when (intent) {
                SearchIndexIntent.OnDetailsToggle -> state.copy(isDetailsOpen = !state.isDetailsOpen)
                SearchIndexIntent.OnDetailsDismiss -> state.copy(isDetailsOpen = false)
                else -> state
            }
        }
    }

    @Test
    fun `the first run's notice says the notes are being read, and Dismiss hides it`() {
        val intents = mutableListOf<SearchIndexIntent>()

        val notice = render(clicks = listOf("Dismiss")) { SearchIndexNotice(onIntent = { intents += it }) }.texts

        assertTrue(actual = notice.any { it.startsWith(prefix = "Notekea is reading your notes.") })
        assertEquals(expected = listOf<SearchIndexIntent>(SearchIndexIntent.OnNoticeDismiss), actual = intents)
    }

    private class Rendered(val texts: List<String>)

    private fun render(
        clicks: List<String> = emptyList(),
        content: @Composable () -> Unit
    ): Rendered {
        var texts = emptyList<String>()
        UiThread.run {
            ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
                NotekeaTheme(darkTheme = false) { content() }
            }.use { scene ->
                settle(scene = scene)
                clicks.forEach { scene.clickText(text = it) { settle(scene = scene) } }
                texts = scene.texts()
            }
        }
        return Rendered(texts = texts)
    }

    private fun settle(
        scene: ImageComposeScene
    ) {
        repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = (frame++) * FRAME_NANOS) }
    }

    private companion object {
        val HUGE = RelativePath(value = "Huge export.md")
        const val STATUS = "Indexing · 28 %"
        const val FRAME_NANOS = 16_000_000L
        const val SETTLE_FRAMES = 5
        const val WIDTH = 900
        const val HEIGHT = 600
    }
}
