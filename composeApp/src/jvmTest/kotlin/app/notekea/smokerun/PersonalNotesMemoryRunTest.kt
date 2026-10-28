package app.notekea.smokerun

import java.io.File
import androidx.compose.ui.test.ExperimentalTestApi
import kotlin.test.Test

/**
 * The memory run of Personal Notes (`ROADMAP.md` 1.18): every note opened in tabs and closed, a long note scrolled in
 * Edit and Preview, the app idle, and the vault left and opened again. Run in a JVM of its own by `smokeRun`.
 */
@OptIn(ExperimentalTestApi::class)
class PersonalNotesMemoryRunTest {
    @Test
    fun `memory of Personal Notes comes back after its notes and the vault close`() = smokeRun(
        name = RUN,
        vaults = listOf(VAULT),
        prepare = { vaults -> PerformanceVaults.writeLongNote(vault = File(vaults, VAULT)) }
    ) { app ->
        openVault(app = app, name = VAULT, shownNote = WELCOME)
        measureVault(
            app = app,
            run = RUN,
            vault = VAULT,
            notes = NOTES,
            search = SEARCH,
            longNote = PerformanceVaults.LONG_NOTE to PerformanceVaults.LONG_NOTE_END,
            openMillis = OPEN_MILLIS
        )
    }

    companion object {
        private const val OPEN_MILLIS = 10_000L
        private const val RUN = "personal-notes"
        private const val VAULT = "Personal Notes"
        private const val WELCOME = "Welcome"
        private val SEARCH = "tomato" to "Tomato plan"
        private val NOTES = listOf(
            "Arepas", "Café con leche", "Pasta e fagioli", "Lisbon trip", "Packing checklist", "Tomato plan",
            "Reading list", "Old idea", "Notekea roadmap thoughts", "2026-10-20", "2026-10-21", "2026-10-22"
        ).map { it to it }
    }
}
