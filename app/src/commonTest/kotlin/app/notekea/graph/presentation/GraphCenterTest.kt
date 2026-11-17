package app.notekea.graph.presentation

import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.RelativePath
import app.notekea.graph.presentation.model.GraphEffect
import app.notekea.graph.presentation.model.GraphIntent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals

/** The note the graph goes around, kept through a rename and through notes that have not come yet. */
@OptIn(ExperimentalCoroutinesApi::class)
class GraphCenterTest : GraphViewModelFixture() {
    @Test
    fun `a renamed center stays the center, whether its new notes come before or after the rename`() =
        runGraphTest {
            send(GraphIntent.OnOpen(note = AREPAS))
            send(GraphIntent.OnNotesChange(notes = notesWith(RENAMED)))

            send(
                GraphIntent.OnRenamed(
                    from = AREPAS,
                    to = RENAMED
                )
            )

            val afterNotes = viewModel.state.value.center

            send(
                GraphIntent.OnRenamed(
                    from = RENAMED,
                    to = AREPAS
                )
            )

            send(GraphIntent.OnNotesChange(notes = notesWith(AREPAS)))

            assertEquals(
                expected = listOf(RENAMED, AREPAS),
                actual = listOf(afterNotes, viewModel.state.value.center)
            )
        }

    @Test
    fun `Esc after a rename opens the note under its new name`() = runGraphTest {
        send(GraphIntent.OnOpen(note = AREPAS))
        val effects = mutableListOf<GraphEffect>()

        backgroundScope.launch {
            viewModel.effects.collect { effects += it }
        }

        send(
            GraphIntent.OnRenamed(
                from = AREPAS,
                to = RENAMED
            )
        )

        send(GraphIntent.OnBack)

        assertEquals(
            expected = listOf<GraphEffect>(GraphEffect.OpenNote(path = RENAMED)),
            actual = effects
        )
    }

    @Test
    fun `a center chosen before the notes come is kept until they do`() = runGraphTest(
        prepare = {
            viewModel.onIntent(intent = GraphIntent.OnShownChange(isShown = true))
            viewModel.onIntent(intent = GraphIntent.OnOpen(note = AREPAS))
            dispatcher.scheduler.advanceUntilIdle()
        }
    ) {
        val state = viewModel.state.value

        assertEquals(
            expected = AREPAS to true,
            actual = state.center to state.dots.any {
                it.path == MENU
            }
        )
    }

    private fun notesWith(arepas: RelativePath) = listOf(arepas, MENU, TASKS).map {
        entry(path = it.value)
    }

    private companion object {
        val RENAMED = RelativePath(value = "Recipes/Arepas reina.md")
    }
}
