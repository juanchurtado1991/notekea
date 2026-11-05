package app.notekea.graph.presentation

import app.notekea.core.notes.domain.NoteFixtures.entry
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals

/** The graph tab (`ROADMAP.md` 2.7): what it shows and how it moves. */
class GraphViewModelTest : GraphViewModelFixture() {

    @Test
    fun `from a note it opens small, around that note, with its preview`() = runGraphTest {
        send(GraphIntent.OnOpen(note = AREPAS))

        val state = viewModel.state.value
        assertEquals(
            expected = Triple(GraphScope.THIS_NOTE, setOf(AREPAS, MENU), "Mix the flour. See Menu."),
            actual = Triple(state.scope, state.dots.map { it.path }.toSet(), state.preview?.snippet)
        )
    }

    @Test
    fun `depth two reaches the neighbors' neighbors`() = runGraphTest {
        send(GraphIntent.OnOpen(note = AREPAS))

        send(GraphIntent.OnDepthSelect(depth = 2))

        assertEquals(
            expected = setOf(AREPAS, MENU, TASKS),
            actual = viewModel.state.value.dots.map { it.path }.toSet()
        )
    }

    @Test
    fun `with no note it asks for one, and teaches nothing about links`() = runGraphTest {
        send(GraphIntent.OnOpen(note = null))

        val state = viewModel.state.value
        assertEquals(expected = true to false, actual = state.needsNote to state.isEmpty)
        assertEquals(expected = emptyList(), actual = state.dots)
    }

    // Review: a selected note in a folder left out read as no note selected.
    @Test
    fun `a note in a folder left out is not a missing note, and one deleted is`() = runGraphTest {
        send(GraphIntent.OnOpen(note = AREPAS))
        send(GraphIntent.OnFolderToggle(folder = "Recipes"))
        assertEquals(expected = false, actual = viewModel.state.value.needsNote)

        send(GraphIntent.OnFolderToggle(folder = "Recipes"))
        send(GraphIntent.OnNotesChange(notes = listOf(MENU, TASKS).map { entry(path = it.value) }))

        assertEquals(expected = true, actual = viewModel.state.value.needsNote)
    }

    @Test
    fun `the whole vault is a choice, without orphans until asked`() = runGraphTest {
        send(GraphIntent.OnOpen(note = null))
        send(GraphIntent.OnScopeSelect(scope = GraphScope.WHOLE_VAULT))
        val hidden = viewModel.state.value.dots.map { it.path }.toSet()

        send(GraphIntent.OnOrphansToggle)

        assertEquals(
            expected = setOf(AREPAS, MENU, TASKS) to true,
            actual = hidden to (LONE in viewModel.state.value.dots.map { it.path })
        )
    }

    @Test
    fun `choosing a note around another moves it to the center`() = runGraphTest {
        send(GraphIntent.OnOpen(note = AREPAS))

        send(GraphIntent.OnNoteSelect(path = MENU))

        val center = viewModel.state.value.dots.single { it.depth == 0 }
        assertEquals(expected = MENU to 0f, actual = center.path to center.x)
    }

    @Test
    fun `an arrow key walks to a connected note, and Enter and Esc open a note`() = runGraphTest {
        val effects = mutableListOf<GraphEffect>()
        backgroundScope.launch { viewModel.effects.collect { effects += it } }
        send(GraphIntent.OnOpen(note = AREPAS))
        val menu = viewModel.state.value.dots.single { it.path == MENU }
        val direction = if (menu.y < 0f) GraphDirection.UP else GraphDirection.DOWN

        send(GraphIntent.OnMove(direction = direction))
        send(GraphIntent.OnOpenSelected)
        send(GraphIntent.OnBack)

        assertEquals(
            expected = listOf<GraphEffect>(GraphEffect.OpenNote(path = MENU), GraphEffect.OpenNote(path = AREPAS)),
            actual = effects
        )
    }

    @Test
    fun `Space asks for the side panel, where the inspector lives`() = runGraphTest {
        val effects = mutableListOf<GraphEffect>()
        backgroundScope.launch { viewModel.effects.collect { effects += it } }

        send(GraphIntent.OnInspectorToggle)

        assertEquals(expected = listOf<GraphEffect>(GraphEffect.ToggleInspector), actual = effects)
    }

    // Review: a double click opened the note selected before it, as a double tap never selects.
    @Test
    fun `a double click opens the note clicked, not the one selected`() = runGraphTest {
        val effects = mutableListOf<GraphEffect>()
        backgroundScope.launch { viewModel.effects.collect { effects += it } }
        send(GraphIntent.OnOpen(note = AREPAS))

        send(GraphIntent.OnNoteOpen(path = MENU))

        assertEquals(expected = listOf<GraphEffect>(GraphEffect.OpenNote(path = MENU)), actual = effects)
    }

    // Review: This note went back to the old center, while the panel showed the note selected on the vault.
    @Test
    fun `This note goes around the note selected on the vault`() = runGraphTest {
        send(GraphIntent.OnOpen(note = AREPAS))
        send(GraphIntent.OnScopeSelect(scope = GraphScope.WHOLE_VAULT))
        send(GraphIntent.OnNoteSelect(path = TASKS))

        send(GraphIntent.OnScopeSelect(scope = GraphScope.THIS_NOTE))

        assertEquals(expected = TASKS, actual = viewModel.state.value.center)
    }

    // Review: every autosave rebuilt the graph and ran its layout, with its tab closed.
    @Test
    fun `with its tab closed the graph does no work, and catches up when shown`() = runGraphTest {
        send(GraphIntent.OnShownChange(isShown = false))
        store.texts[LONE] = "Now see [[Menu]]."
        send(GraphIntent.OnNotesChange(notes = listOf(AREPAS, MENU, TASKS, LONE).map { entry(path = it.value) }))
        send(GraphIntent.OnOpen(note = LONE))
        assertEquals(expected = emptyList(), actual = viewModel.state.value.dots.filter { it.path == LONE })

        send(GraphIntent.OnShownChange(isShown = true))

        assertEquals(expected = true, actual = viewModel.state.value.dots.any { it.path == LONE })
    }

    @Test
    fun `a folder of the legend is left out and remembered`() = runGraphTest {
        store.texts[TASKS] = "Back to [[Menu]]."
        send(GraphIntent.OnScopeSelect(scope = GraphScope.WHOLE_VAULT))

        send(GraphIntent.OnFolderToggle(folder = "Guides"))

        assertEquals(
            expected = false to setOf("Guides"),
            actual = (TASKS in viewModel.state.value.dots.map { it.path }) to preferences.kept.excludedFolders
        )
    }

    @Test
    fun `a note with no links teaches how to link`() = runGraphTest {
        send(GraphIntent.OnOpen(note = LONE))

        assertEquals(expected = true, actual = viewModel.state.value.isEmpty)
    }
}
