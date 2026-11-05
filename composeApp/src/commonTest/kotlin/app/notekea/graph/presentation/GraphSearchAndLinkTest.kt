package app.notekea.graph.presentation

import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals

/** The graph tab (`ROADMAP.md` 2.7): finding a note, and linking notes close in meaning. */
class GraphSearchAndLinkTest : GraphViewModelFixture() {

    // Review: a note open in an editor went on with its old text, and the next save was a conflict.
    @Test
    fun `Link and Undo tell the editors the note changed`() = runGraphTest {
        val effects = mutableListOf<GraphEffect>()
        backgroundScope.launch { viewModel.effects.collect { effects += it } }
        send(GraphIntent.OnOpen(note = AREPAS))

        send(GraphIntent.OnLink(path = TASKS))
        send(GraphIntent.OnLinkUndo)

        val rewritten = GraphEffect.NoteRewritten(path = AREPAS)
        assertEquals(expected = listOf<GraphEffect>(rewritten, rewritten), actual = effects)
    }

    // Review: a quick second click wrote the link twice, and Undo took back one.
    @Test
    fun `a second click on Link while the first runs writes nothing more`() = runGraphTest {
        send(GraphIntent.OnOpen(note = AREPAS))
        val text = store.texts[AREPAS]

        viewModel.onIntent(intent = GraphIntent.OnLink(path = TASKS))
        send(GraphIntent.OnLink(path = TASKS))

        assertEquals(expected = "$text\n\n[[Tasks]]\n", actual = store.texts[AREPAS])
    }

    @Test
    fun `a match brings its own graph from the whole vault, and the next one too`() = runGraphTest {
        send(GraphIntent.OnScopeSelect(scope = GraphScope.WHOLE_VAULT))
        send(GraphIntent.OnSearchOpen)
        send(GraphIntent.OnQueryChange(query = "a"))
        val first = viewModel.state.value.matches.first()

        send(GraphIntent.OnMatchStep(isForward = true))
        val found = viewModel.state.value
        assertEquals(
            expected = listOf(GraphScope.THIS_NOTE, first, first, first),
            actual = listOf(found.scope, found.center, found.selected, found.revealed)
        )

        send(GraphIntent.OnMatchStep(isForward = false))
        send(GraphIntent.OnSearchClose)
        val state = viewModel.state.value
        assertEquals(expected = Triple(false, "", null), actual = Triple(state.isSearching, state.query, state.revealed))
        assertEquals(expected = emptyList(), actual = state.matches)
    }

    @Test
    fun `with no note, search finds any note and goes to its graph`() = runGraphTest {
        send(GraphIntent.OnOpen(note = null))
        send(GraphIntent.OnQueryChange(query = "tasks"))
        assertEquals(expected = listOf(TASKS), actual = viewModel.state.value.matches)

        send(GraphIntent.OnMatchStep(isForward = true))

        assertEquals(expected = TASKS to false, actual = viewModel.state.value.center to viewModel.state.value.needsNote)
    }

    @Test
    fun `a search with nothing found goes nowhere`() = runGraphTest {
        send(GraphIntent.OnOpen(note = AREPAS))
        send(GraphIntent.OnQueryChange(query = "zzz"))

        send(GraphIntent.OnMatchStep(isForward = true))

        assertEquals(expected = AREPAS, actual = viewModel.state.value.center)
    }

    @Test
    fun `Link joins the notes at once, and Undo takes it back`() = runGraphTest {
        send(GraphIntent.OnOpen(note = AREPAS))
        val text = store.texts[AREPAS]

        send(GraphIntent.OnLink(path = TASKS))
        assertEquals(expected = "$text\n\n[[Tasks]]\n", actual = store.texts[AREPAS])
        assertEquals(expected = TASKS, actual = viewModel.state.value.linked)
        assertEquals(expected = true, actual = viewModel.state.value.dots.any { it.path == TASKS })

        send(GraphIntent.OnLinkUndo)
        assertEquals(expected = text, actual = store.texts[AREPAS])
        assertEquals(expected = false, actual = viewModel.state.value.dots.any { it.path == TASKS })
    }

    @Test
    fun `Undo leaves a note edited since then alone, and says so`() = runGraphTest {
        send(GraphIntent.OnOpen(note = AREPAS))
        send(GraphIntent.OnLink(path = TASKS))
        store.texts[AREPAS] = "Edited meanwhile."

        send(GraphIntent.OnLinkUndo)

        assertEquals(expected = "Edited meanwhile.", actual = store.texts[AREPAS])
        assertEquals(expected = true, actual = viewModel.state.value.linkFailed)
    }

    @Test
    fun `the inspector suggests notes close in meaning that no link joins yet`() = runGraphTest(
        prepare = {
            store.texts[TASKS] = SAME_MEANING
            store.texts[LONE] = SAME_MEANING
            store.texts[MENU] = "$SAME_MEANING [[Tasks]]"
        }
    ) {
        indexed(paths = listOf(TASKS, LONE, MENU))
        send(GraphIntent.OnScopeSelect(scope = GraphScope.WHOLE_VAULT))

        send(GraphIntent.OnNoteSelect(path = TASKS))

        assertEquals(expected = listOf(LONE), actual = viewModel.state.value.preview?.suggested)
    }

    // Review: a note of a folder left out of the graph was offered, and Link joined it out of sight.
    @Test
    fun `no note of a folder left out is suggested`() = runGraphTest(
        prepare = {
            store.texts[TASKS] = SAME_MEANING
            store.texts[MENU] = SAME_MEANING
        }
    ) {
        indexed(paths = listOf(TASKS, MENU))
        send(GraphIntent.OnScopeSelect(scope = GraphScope.WHOLE_VAULT))
        send(GraphIntent.OnFolderToggle(folder = "Recipes"))

        send(GraphIntent.OnNoteSelect(path = TASKS))

        assertEquals(expected = emptyList(), actual = viewModel.state.value.preview?.suggested)
    }
}
