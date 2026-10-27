package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class WorkspaceTest {
    private val arepas = RelativePath(value = "Recipes/Arepas.md")
    private val welcome = RelativePath(value = "Welcome.md")
    private val plate = RelativePath(value = "attachments/plate.png")
    private val today = RelativePath(value = "Journal/2026-10-27.md")

    @Test
    fun `opening adds a tab after the active one and shows it, an open file goes to its tab, a background one waits`() {
        val opened = Workspace.EMPTY.open(
            path = arepas
        ).open(path = welcome).select(pane = 0, tab = 0).open(path = plate)
        val again = opened.open(path = welcome)
        val background = again.open(path = today, inBackground = true)

        assertEquals(
            expected = listOf(
                EditorPane(tabs = listOf(arepas, plate, welcome), active = 1),
                EditorPane(tabs = listOf(arepas, plate, welcome), active = 2),
                EditorPane(tabs = listOf(arepas, plate, welcome, today), active = 2)
            ),
            actual = listOf(opened, again, background).map { it.panes.single() }
        )
    }

    @Test
    fun `closing shows the tab to the right, or to the left at the end, and close others keeps one`() {
        val three = Workspace.EMPTY.open(path = arepas).open(path = welcome).open(path = plate)

        assertEquals(
            expected = listOf(
                EditorPane(tabs = listOf(arepas, plate), active = 1),
                EditorPane(tabs = listOf(arepas, welcome), active = 1),
                EditorPane(tabs = listOf(welcome, plate), active = 1),
                EditorPane(tabs = listOf(welcome), active = 0),
                EditorPane()
            ),
            actual = listOf(
                three.select(pane = 0, tab = 1).close(pane = 0, tab = 1),
                three.close(pane = 0, tab = 2),
                three.close(pane = 0, tab = 0),
                three.closeOthers(pane = 0, tab = 1),
                Workspace.EMPTY.open(path = arepas).close(pane = 0, tab = 0)
            ).map { it.panes.single() }
        )
    }

    @Test
    fun `tabs cycle around both ends, and Mod 1 to 8 jump to theirs while 9 is the last`() {
        val three = Workspace.EMPTY.open(path = arepas).open(path = welcome).open(path = plate)

        assertEquals(
            expected = listOf(0, 1, 0, 2, 2),
            actual = listOf(
                three.cycle(step = 1),
                three.cycle(step = -1),
                three.jump(number = 1),
                three.jump(number = 9),
                three.jump(number = 5)
            ).map { it.panes.single().active }
        )
    }

    @Test
    fun `split right moves the active tab to a new pane on the right, and a second split does nothing`() {
        val split = Workspace.EMPTY.open(path = arepas).open(path = welcome).splitRight()

        assertEquals(
            expected = Workspace(
                panes = listOf(
                    EditorPane(tabs = listOf(arepas), active = 0),
                    EditorPane(tabs = listOf(welcome), active = 0)
                ),
                activePane = 1
            ),
            actual = split
        )
        assertEquals(expected = split, actual = split.splitRight())
    }

    @Test
    fun `a tab dragged to the other pane lands where it is dropped, and a pane left empty closes`() {
        val split = Workspace.EMPTY.open(path = arepas).open(path = welcome).splitRight()

        val moved = split.open(path = plate).move(path = arepas, toPane = 1, toIndex = 1)
        val emptied = split.move(path = welcome, toPane = 0, toIndex = 0)

        assertEquals(
            expected = Workspace(
                panes = listOf(EditorPane(tabs = listOf(welcome, arepas, plate), active = 1)),
                activePane = 0
            ),
            actual = moved
        )
        assertEquals(
            expected = Workspace(
                panes = listOf(EditorPane(tabs = listOf(welcome, arepas), active = 0)),
                activePane = 0
            ),
            actual = emptied
        )
    }

    @Test
    fun `a file opens in one tab only, even from the other pane, and closing the right pane's last tab merges`() {
        val split = Workspace.EMPTY.open(path = arepas).open(path = welcome).splitRight().focus(pane = 0)

        val reopened = split.open(path = welcome)
        val closed = split.close(pane = 1, tab = 0)

        assertEquals(
            expected = 1 to Workspace(panes = listOf(EditorPane(tabs = listOf(arepas), active = 0)), activePane = 0),
            actual = reopened.activePane to closed
        )
    }

    @Test
    fun `renames and moves follow the tabs, folders included, and deleted files close theirs`() {
        val workspace = Workspace.EMPTY.open(path = arepas).open(path = welcome).open(path = plate)

        val renamed = workspace.renamed(from = RelativePath(value = "Recipes"), to = RelativePath(value = "Food"))
        val deleted = workspace.without(path = RelativePath(value = "attachments"))
        val missing = workspace.keepingOnly(existing = setOf(welcome))

        assertEquals(
            expected = listOf(
                listOf("Food/Arepas.md", "Welcome.md", "attachments/plate.png"),
                listOf("Recipes/Arepas.md", "Welcome.md"),
                listOf("Welcome.md")
            ),
            actual = listOf(renamed, deleted, missing).map { it.panes.single().tabs.map { tab -> tab.value } }
        )
    }
}
