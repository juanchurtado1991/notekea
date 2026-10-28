package app.notekea.smokerun

import androidx.compose.ui.test.ExperimentalTestApi
import kotlin.test.Test

/**
 * The memory run of the Large Vault (`ROADMAP.md` 1.18): 5,000 notes opened, 20 of them opened in tabs and closed, the
 * app idle, and the vault left and opened again. Run in a JVM of its own by `smokeRun`.
 */
@OptIn(ExperimentalTestApi::class)
class LargeVaultMemoryRunTest {
    @Test
    fun `memory of the Large Vault comes back after its notes and the vault close`() = smokeRun(
        name = RUN,
        vaults = emptyList(),
        prepare = PerformanceVaults::writeLargeVault
    ) { app ->
        openVault(
            app = app,
            name = PerformanceVaults.LARGE_VAULT,
            shownNote = FIRST_FOLDER,
            timeoutMillis = OPEN_MILLIS
        )
        measureVault(
            app = app,
            run = RUN,
            vault = PerformanceVaults.LARGE_VAULT,
            notes = NOTES,
            search = SEARCH,
            longNote = null,
            openMillis = OPEN_MILLIS
        )
    }

    companion object {
        private const val FIRST_FOLDER = "Folder 01"
        private const val NOTE_COUNT = 20
        private const val OPEN_MILLIS = 120_000L
        private const val RUN = "large-vault"

        // Every note says "See": the search reads the text of all 5,000, and the first in name order shows first.
        private val SEARCH = "see" to PerformanceVaults.largeNoteName(folder = 1, note = 1)
        private val NOTES = (1..NOTE_COUNT).map { index ->
            // Every note of a folder links to the folder's first note, a line only the note's text shows.
            PerformanceVaults.largeNoteName(folder = index, note = index) to
                "See [[${PerformanceVaults.largeNoteName(folder = index, note = 1)}]]"
        }
    }
}
