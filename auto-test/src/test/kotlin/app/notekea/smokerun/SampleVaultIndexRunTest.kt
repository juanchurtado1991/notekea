package app.notekea.smokerun

import androidx.compose.ui.test.ExperimentalTestApi
import kotlin.test.Test

/** The index memory run of the Sample Vault (`ROADMAP.md` 2.9), in a JVM of its own in `smokeRun`. */
@OptIn(ExperimentalTestApi::class)
class SampleVaultIndexRunTest {
    @Test
    fun `memory comes back after the Sample Vault is indexed and searched`() = smokeRun(name = RUN) { app ->
        openVault(app = app, name = SAMPLE_VAULT, shownNote = HOME)
        measureIndexing(app = app, run = RUN, vault = SAMPLE_VAULT, notes = NOTES, search = SEARCH)
    }

    companion object {
        private const val HOME = "Home"
        private const val NOTES = 40
        private const val RUN = "index-sample-vault"
        private val SEARCH = "arepas water" to "Arepas"
    }
}
