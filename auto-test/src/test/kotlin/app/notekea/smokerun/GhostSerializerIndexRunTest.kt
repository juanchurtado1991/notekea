package app.notekea.smokerun

import androidx.compose.ui.test.ExperimentalTestApi
import kotlin.test.Test

/** The index memory run of the Ghost Serializer vault, in a JVM of its own in `smokeRun`. */
@OptIn(ExperimentalTestApi::class)
class GhostSerializerIndexRunTest {
    @Test
    fun `memory comes back after the Ghost Serializer vault is indexed and searched`() = smokeRun(
        name = RUN,
        vaults = listOf(VAULT)
    ) { app ->
        openVault(
            app = app,
            name = VAULT,
            shownNote = FIRST_MODULE,
            timeoutMillis = OPEN_MILLIS
        )

        measureIndexing(
            app = app,
            run = RUN,
            vault = VAULT,
            notes = NOTES,
            search = SEARCH
        )
    }

    companion object {
        private const val FIRST_MODULE = "ghost-api"
        private const val NOTES = 371
        private const val OPEN_MILLIS = 120_000L
        private const val RUN = "index-ghost-serializer"
        private const val VAULT = "Ghost Serializer"
        private val SEARCH = "perfect hash" to "PerfectHashFinder"
    }
}
