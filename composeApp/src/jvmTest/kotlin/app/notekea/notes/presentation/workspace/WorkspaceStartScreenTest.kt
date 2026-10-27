package app.notekea.notes.presentation.workspace

import androidx.compose.material3.Text
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.clickText
import app.notekea.core.presentation.descriptions
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.notes.domain.NoteEntry
import app.notekea.notes.domain.NoteFixtures.file
import app.notekea.notes.domain.NoteMetadata
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.VaultTree
import app.notekea.notes.domain.Workspace
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The editor without tabs and the tab of an unsaved note (`ROADMAP.md` 1.14), rendered headless: each start action runs
 * its own action, and only the unsaved note's tab shows the dot that says so.
 */
class WorkspaceStartScreenTest {
    private val notes = listOf("Welcome.md", "Ideas.md").map { RelativePath(value = it) }
    private val tree = notes.fold(initial = VaultTree.EMPTY) { tree, path ->
        tree.withNote(note = NoteEntry(file = file(path = path.value), metadata = NoteMetadata.NONE))
    }

    @Test
    fun `an editor without tabs offers find a note, a new note, today's note and a search, each doing its own`() {
        val chosen = mutableListOf<String>()
        val actions = WorkspaceStartActions(
            onFindNote = { chosen += FIND },
            onNewNote = { chosen += NEW },
            onToday = { chosen += TODAY },
            onSearchNotes = { chosen += SEARCH }
        )
        UiThread.run {
            scene(workspace = Workspace.EMPTY, actions = actions, unsaved = emptySet()).use { scene ->
                scene.render(nanoTime = 0L)
                listOf(SEARCH, TODAY, NEW, FIND).forEach { scene.clickText(text = it) { scene.render(nanoTime = 0L) } }
            }
        }

        assertEquals(expected = listOf(SEARCH, TODAY, NEW, FIND), actual = chosen)
    }

    @Test
    fun `only the tab of a note with unsaved changes shows the unsaved dot`() {
        var descriptions = emptyList<String>()
        val workspace = notes.fold(initial = Workspace.EMPTY) { it, path -> it.open(path = path) }
        UiThread.run {
            scene(workspace = workspace, actions = WorkspaceStartActions(), unsaved = setOf(notes[0])).use { scene ->
                scene.render(nanoTime = 0L)
                descriptions = scene.descriptions()
            }
        }

        assertEquals(expected = 1, actual = descriptions.count { it == UNSAVED })
    }

    private fun scene(
        workspace: Workspace,
        actions: WorkspaceStartActions,
        unsaved: Set<RelativePath>
    ) = ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
        NotekeaTheme(darkTheme = false) {
            WorkspaceContent(
                state = WorkspaceState(workspace = workspace, tree = tree),
                platformConventions = PreviewPlatformConventions.linux,
                onIntent = {},
                startActions = actions,
                unsavedPaths = unsaved
            ) { _, path, _ -> Text(text = path.value) }
        }
    }

    companion object {
        private const val FIND = "Find a note"
        private const val HEIGHT = 600
        private const val NEW = "New note"
        private const val SEARCH = "Search in notes"
        private const val TODAY = "Today’s note"
        private const val UNSAVED = "Unsaved changes"
        private const val WIDTH = 1000
    }
}
