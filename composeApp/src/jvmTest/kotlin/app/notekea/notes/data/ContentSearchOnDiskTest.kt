package app.notekea.notes.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.FakeFileWatcher
import app.notekea.notes.domain.FakeNoteMetadataCache
import app.notekea.notes.domain.MarkdownLineStripper
import app.notekea.notes.domain.NaturalNameComparator
import app.notekea.notes.domain.NoteContentStore
import app.notekea.notes.domain.NoteTextCache
import app.notekea.notes.domain.ObserveVaultTreeUseCase
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.SearchNoteContentUseCase
import app.notekea.notes.domain.TextFolder
import app.notekea.notes.domain.VaultTree
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Content search over the production scanner, frontmatter reader and content store, on real files: Windows line
 * breaks, accents, tags in frontmatter and in code, and a note changed on disk between two searches.
 */
@OptIn(kotlin.io.path.ExperimentalPathApi::class)
class ContentSearchOnDiskTest {
    private val testDispatcher = StandardTestDispatcher()
    private val dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    private val vault: Path = Files.createTempDirectory(TEMP_PREFIX)
    private val reads = mutableListOf<RelativePath>()
    private val store = counting(real = OkioNoteContentStore(fileSystem = FileSystem.SYSTEM, dispatchers = dispatchers))
    private val folder = TextFolder()
    private val search = SearchNoteContentUseCase(
        noteTextCache = NoteTextCache(contentStore = store, textFolder = folder),
        textFolder = folder,
        naturalNameComparator = NaturalNameComparator(),
        markdownLineStripper = MarkdownLineStripper(),
        dispatchers = dispatchers
    )

    @AfterTest
    fun tearDown() {
        vault.deleteRecursively()
    }

    @Test
    fun `real notes are found by words and tags, with clean snippets`() = runTest(context = testDispatcher) {
        write(path = AREPAS, text = "---\r\ntags: [recipe, venezuelan]\r\n---\r\nMix the corn flour.\r\n")
        write(path = CAFE, text = "Café for breakfast #recipe/drinks\n")
        write(path = CODE, text = "```\n#recipe\n```\n")
        val notes = loadTree().notes.values

        assertEquals(
            expected = listOf(
                AREPAS to null,
                CAFE to "Café for breakfast #recipe/drinks"
            ),
            actual = search(vault.toString(), notes, "#recipe").map { it.note.file.path.value to it.snippet }
        )
        assertEquals(
            expected = listOf(AREPAS to "Mix the corn flour.", CAFE to "Café for breakfast #recipe/drinks"),
            actual = listOf("corn", "cafe").map { query ->
                search(vault.toString(), notes, query).single().let { it.note.file.path.value to it.snippet }
            }
        )
    }

    @Test
    fun `a note changed on disk is read again, the others come from memory`() = runTest(context = testDispatcher) {
        write(path = AREPAS, text = "Corn\n")
        write(path = CAFE, text = "Coffee\n")
        search(vault.toString(), loadTree().notes.values, "corn")
        reads.clear()

        write(path = CAFE, text = "Coffee with corn bread\n", modifiedLater = true)
        val results = search(vault.toString(), loadTree().notes.values, "corn")

        assertEquals(
            expected = listOf(RelativePath(value = CAFE)) to listOf(AREPAS, CAFE),
            actual = reads.toList() to results.map { it.note.file.path.value }
        )
    }

    private fun write(
        path: String,
        text: String,
        modifiedLater: Boolean = false
    ) {
        val file = vault.resolve(path)
        file.parent.createDirectories()
        file.writeText(text = text)
        if (modifiedLater) {
            Files.setLastModifiedTime(file, FileTime.fromMillis(System.currentTimeMillis() + LATER_MILLIS))
        }
    }

    private suspend fun loadTree(): VaultTree {
        val observeVaultTree = ObserveVaultTreeUseCase(
            noteScanner = OkioNoteScanner(fileSystem = FileSystem.SYSTEM, dispatchers = dispatchers),
            noteMetadataReader = OkioNoteMetadataReader(
                fileSystem = FileSystem.SYSTEM,
                dispatchers = dispatchers,
                parser = GhostFrontmatterParser()
            ),
            fileWatcher = FakeFileWatcher(),
            noteMetadataCache = FakeNoteMetadataCache()
        )
        return (observeVaultTree(vaultPath = vault.toString()).first() as Outcome.Success).value
    }

    /** The real store, recording which notes it reads. */
    private fun counting(
        real: NoteContentStore
    ): NoteContentStore = object : NoteContentStore by real {
        override suspend fun read(
            vaultPath: String,
            path: RelativePath
        ): String? = real.read(vaultPath = vaultPath, path = path).also { reads += path }
    }

    companion object {
        private const val AREPAS = "Recipes/Arepas.md"
        private const val CAFE = "Recipes/Café.md"
        private const val CODE = "Code.md"
        private const val LATER_MILLIS = 5_000L
        private const val TEMP_PREFIX = "notekea-search"
    }
}
