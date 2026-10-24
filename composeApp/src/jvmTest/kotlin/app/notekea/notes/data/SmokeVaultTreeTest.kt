package app.notekea.notes.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.FakeFileWatcher
import app.notekea.notes.domain.FakeNoteMetadataCache
import app.notekea.notes.domain.ObserveVaultTreeUseCase
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.VaultTree
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Runs the production scanner and frontmatter reader on the `Personal Notes` smoke vault, so the tree the smoke test
 * shows is the one these adapters build from real files (hidden folders, a `.txt` file, accents, mixed frontmatter).
 */
class SmokeVaultTreeTest {
    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `the personal notes vault lists its twelve notes with their frontmatter`() = runTest(context = testDispatcher) {
        val tree = loadTree()

        assertEquals(
            expected = 12,
            actual = tree.notes.size
        )
        assertEquals(
            expected = listOf(
                listOf("recipe", "venezuelan"),
                listOf("travel"),
                listOf("ideas", "notekea"),
                emptyList()
            ),
            actual = listOf(AREPAS, LISBON, ROADMAP_THOUGHTS, CAFE).map {
                tree.notes
                    .getValue(RelativePath(value = it))
                    .metadata.tags
            }
        )
        assertEquals(
            expected = emptyList(),
            actual = tree.folders.filter { folder ->
                folder.value.split(RelativePath.SEPARATOR).any { it.startsWith(prefix = ".") }
            }
        )
    }

    private suspend fun loadTree(): VaultTree {
        val dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
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
        val outcome = observeVaultTree(vaultPath = (VAULTS_DIRECTORY.toPath() / PERSONAL).toString()).first()
        return (outcome as Outcome.Success).value
    }

    companion object {
        private const val AREPAS = "Recipes/Arepas.md"
        private const val CAFE = "Recipes/Café con leche.md"
        private const val LISBON = "Travel/Lisbon trip.md"
        private const val PERSONAL = "Personal Notes"
        private const val ROADMAP_THOUGHTS = "Ideas/Notekea roadmap thoughts.md"
        private const val VAULTS_DIRECTORY = "../smoke-data/vaults"
    }
}
