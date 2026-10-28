package app.notekea.smokerun

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import kotlin.test.Test

/**
 * The idle run of a performance pass (`ROADMAP.md`, "Closing a milestone"): the Sample Vault open, notes opened and
 * closed, then the app left alone, its memory sampled after the work, after closing the notes and after one minute and
 * after the whole idle time the `smokeRun` task asks for. A plain test run does the work without waiting.
 */
@OptIn(ExperimentalTestApi::class)
class IdleRunTest {
    @Test
    fun `memory comes back after notes close and stays put while the app is idle`() = smokeRun(name = RUN) { app ->
        openVault(app = app, name = SAMPLE_VAULT, shownNote = HOME)
        NOTES.forEach { (name, text) -> openNote(name = name, shownText = text) }
        SmokeRunReport.sample(run = RUN, moment = "notes-open")
        repeat(times = NOTES.size) { pressShortcut(key = Key.W) }
        waitForIdle()
        SmokeRunReport.sample(run = RUN, moment = "notes-closed")
        val idle = SmokeRunReport.idleSeconds
        if (idle > 0) {
            Thread.sleep(minOf(idle, MINUTE) * MILLIS)
            SmokeRunReport.sample(run = RUN, moment = "idle-1min")
            Thread.sleep((idle - MINUTE).coerceAtLeast(minimumValue = 0) * MILLIS)
            SmokeRunReport.sample(run = RUN, moment = "idle-${idle}s")
        }
    }

    companion object {
        private const val HOME = "Home"
        private const val MILLIS = 1_000L
        private const val MINUTE = 60L
        private const val RUN = "idle"
        private val NOTES = listOf(
            "Arepas" to "Ingredients",
            "Pancakes" to "Rest the batter",
            "Embeds" to "A section of a note",
            "Properties" to "Text, number, checkbox",
            "Images and files" to "![[Weekly menu.pdf]]"
        )
    }
}
