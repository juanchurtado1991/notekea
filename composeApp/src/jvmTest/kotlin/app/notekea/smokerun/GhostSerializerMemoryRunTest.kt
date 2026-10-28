package app.notekea.smokerun

import androidx.compose.ui.test.ExperimentalTestApi
import java.io.File
import kotlin.test.Test

/**
 * The memory run of the Ghost Serializer vault (`ROADMAP.md` 1.18), a code repository as notes: the vault opened, 20
 * of its larger notes opened in tabs and closed, a text search over all of them, the app idle, and the vault left and
 * opened again. Run in a JVM of its own by `smokeRun`.
 */
@OptIn(ExperimentalTestApi::class)
class GhostSerializerMemoryRunTest {
    @Test
    fun `memory of the Ghost Serializer vault comes back after its notes and the vault close`() = smokeRun(
        name = RUN,
        vaults = listOf(VAULT)
    ) { app ->
        openVault(app = app, name = VAULT, shownNote = FIRST_MODULE, timeoutMillis = OPEN_MILLIS)
        measureVault(
            app = app,
            run = RUN,
            vault = VAULT,
            notes = NOTES.map { it to firstLineOf(note = it) },
            search = SEARCH,
            openMillis = OPEN_MILLIS,
            longNote = null
        )
    }

    /** The note's opening sentence, under its title: a line only the note's own text shows. */
    private fun firstLineOf(
        note: String
    ): String = File(VAULT_FOLDER).walkTopDown().first { it.name == "$note.md" }.readLines()
        .dropWhile { !it.startsWith(prefix = TITLE) }.drop(n = 1).first { it.isNotBlank() }

    companion object {
        private const val FIRST_MODULE = "ghost-api"
        private const val OPEN_MILLIS = 120_000L
        private const val RUN = "ghost-serializer"
        private const val TITLE = "# "
        private const val VAULT = "Ghost Serializer"
        private const val VAULT_FOLDER = "../smoke-data/vaults/$VAULT"
        private val SEARCH = "perfect hash" to "PerfectHashFinder"

        // Large notes from every module: the parsers, writers, code generation and integrations.
        private val NOTES = listOf(
            "Ghost", "GhostJsonFlatReader", "GhostJsonStringReader", "GhostJsonReader", "GhostJsonWriter",
            "GhostJsonStringWriter", "FlatByteArrayWriter", "GhostDoubleFormatter", "PerfectHashFinder",
            "DeserializeCodeEmitter", "SerializerImportResolver", "BaseDeserializeEmitter", "GhostYamlCursorTraversal",
            "GhostEiselLemirePowers", "GhostJsonFloatParseHelpers", "GhostJsonValueCaptureScan",
            "GhostHttpMessageConverter", "ProtoTimestampCodec", "ListSerializer", "GhostSerializerLookup"
        )
    }
}
