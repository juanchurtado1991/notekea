package app.notekea.shell.presentation

import app.notekea.core.notes.domain.RelativePath
import app.notekea.shell.domain.EditorPane
import app.notekea.shell.domain.Workspace
import app.notekea.shell.presentation.workspace.WorkspaceState
import kotlin.test.Test
import kotlin.test.assertEquals

/** What the tabs say about the note being read. */
class WorkspaceStateTest {
    @Test
    fun `the note being read may be the one beside the active tab`() {
        val workspace = Workspace(
            panes = listOf(
                EditorPane(
                    tabs = listOf(Workspace.GRAPH),
                    active = 0
                ),
                EditorPane(listOf(NOTE), active = 0)
            )
        )

        assertEquals(
            expected = NOTE,
            actual = WorkspaceState(workspace = workspace).noteShown
        )
    }

    @Test
    fun `with only the graph open no note is being read`() {
        val workspace = Workspace(
            panes = listOf(
                EditorPane(
                    tabs = listOf(Workspace.GRAPH),
                    active = 0
                )
            )
        )

        assertEquals(
            expected = null,
            actual = WorkspaceState(workspace = workspace).noteShown
        )
    }

    // With one pane the Query tab hides the note it is opened from, which Save in still has to offer.
    @Test
    fun `a note hidden behind a tool's tab is still at hand`() {
        val pane = EditorPane(
            tabs = listOf(NOTE, Workspace.QUERY),
            active = 1
        )

        assertEquals(
            expected = NOTE,
            actual = WorkspaceState(workspace = Workspace(panes = listOf(pane))).noteAtHand
        )
    }

    private companion object {
        val NOTE = RelativePath(value = "Recipes/Arepas.md")
    }
}
