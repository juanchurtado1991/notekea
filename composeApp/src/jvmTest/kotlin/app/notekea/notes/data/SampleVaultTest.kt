package app.notekea.notes.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.notes.domain.FrontmatterContent
import app.notekea.notes.domain.FrontmatterReader
import app.notekea.notes.domain.LinkKind
import app.notekea.notes.domain.LinkResolver
import app.notekea.notes.domain.NoteDocument
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The Sample Vault of `ROADMAP.md` 1.16 holds what it promises: every wiki link leads to a note but the two missing
 * on purpose, every callout type is shown, every note's properties can be read, and its Obsidian settings put
 * attachments, daily notes and templates where the guide says.
 */
class SampleVaultTest {
    private val testDispatcher = StandardTestDispatcher()
    private val vault = File(SAMPLE_VAULT)
    private val notes = vault.walkTopDown()
        .filter { it.isFile && it.extension == MARKDOWN && !it.path.contains(SETTINGS_FOLDER) }
        .associate { RelativePath(value = it.relativeTo(base = vault).invariantSeparatorsPath) to it.readText() }

    @Test
    fun `every wiki link leads to a note, but the missing note and picture the guides show on purpose`() {
        val resolver = LinkResolver(notePaths = notes.keys)
        val finder = JetBrainsMarkdownLinkFinder()
        val unresolved = notes.flatMap { (path, text) ->
            finder.find(markdown = text).filter { it.kind == LinkKind.WIKI }
                .map { it.target.substringBefore(delimiter = ANCHOR) }
                .filter { it.isNotEmpty() && !it.contains(other = FILE_DOT) }
                .filter { resolver.closestWiki(target = it, source = path) == null }
        }.toSet()

        assertEquals(expected = setOf("Shopping list"), actual = unresolved)
    }

    @Test
    fun `the vault shows every callout type and reads every note's properties`() {
        val callouts = Regex(pattern = "\\[!(\\w+)]").findAll(input = notes.values.joinToString()).map {
            it.groupValues[1]
        }
        val reader = FrontmatterReader()
        val invalid = notes.filter { (_, text) ->
            reader.read(header = NoteDocument.fromText(text = text).header) is FrontmatterContent.Invalid
        }.keys

        assertEquals(
            expected = CALLOUT_TYPES to emptySet(),
            actual = callouts.filter { it in CALLOUT_TYPES }.toSet() to invalid
        )
    }

    @Test
    fun `its Obsidian settings put attachments, daily notes and templates in their folders`() =
        runTest(context = testDispatcher) {
            val settings = ObsidianVaultSettingsReader(
                fileSystem = FileSystem.SYSTEM,
                dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
            )
            val path = vault.path

            assertEquals(
                expected = listOf("Assets", "Journal/Daily", "Templates", 40),
                actual = listOf(
                    settings.attachmentFolderPath(vaultPath = path),
                    settings.dailyNotes(vaultPath = path)?.folder,
                    settings.templates(vaultPath = path)?.folder,
                    notes.size
                )
            )
        }

    companion object {
        private const val ANCHOR = "#"
        private const val FILE_DOT = "."
        private const val MARKDOWN = "md"
        private const val SAMPLE_VAULT = "../smoke-data/vaults/Sample Vault"
        private const val SETTINGS_FOLDER = ".obsidian"
        private val CALLOUT_TYPES = setOf(
            "note", "abstract", "info", "todo", "tip", "success", "question", "warning", "failure", "danger", "bug",
            "example", "quote"
        )
    }
}
