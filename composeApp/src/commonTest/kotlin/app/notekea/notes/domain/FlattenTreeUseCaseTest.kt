package app.notekea.notes.domain

import app.notekea.notes.domain.NoteFixtures.entry
import kotlin.test.Test
import kotlin.test.assertEquals

class FlattenTreeUseCaseTest {
    private val flattenTree = FlattenTreeUseCase(naturalNameComparator = NaturalNameComparator())
    private val tree = listOf(
        entry(path = "Welcome.md", modifiedOffsetMillis = 30L),
        entry(path = "Projects/Garden/Tomato plan.md"),
        entry(path = "Projects/Roadmap.md"),
        entry(path = "Journal/2026-10-10.md", modifiedOffsetMillis = 10L),
        entry(path = "Journal/2026-10-9.md", modifiedOffsetMillis = 20L)
    ).fold(initial = VaultTree.EMPTY) { tree, note -> tree.withNote(note = note) }
        .withFolder(path = RelativePath(value = "Empty"))

    @Test
    fun `collapsed folders show their recursive note count and hide their content`() {
        assertEquals(
            expected = listOf(
                folder(path = "Empty", depth = 0, count = 0),
                folder(path = "Journal", depth = 0, count = 2),
                folder(path = "Projects", depth = 0, count = 2),
                note(path = "Welcome.md", depth = 0)
            ),
            actual = flattenTree(tree = tree, expandedFolders = emptySet(), sort = TreeSort.NAME).map(::withoutDates)
        )
    }

    @Test
    fun `expanded folders list subfolders before notes, at the next depth`() {
        val expanded = setOf(RelativePath(value = "Projects"), RelativePath(value = "Projects/Garden"))

        assertEquals(
            expected = listOf(
                folder(path = "Empty", depth = 0, count = 0),
                folder(path = "Journal", depth = 0, count = 2),
                folder(path = "Projects", depth = 0, count = 2, isExpanded = true),
                folder(path = "Projects/Garden", depth = 1, count = 1, isExpanded = true),
                note(path = "Projects/Garden/Tomato plan.md", depth = 2),
                note(path = "Projects/Roadmap.md", depth = 1),
                note(path = "Welcome.md", depth = 0)
            ),
            actual = flattenTree(tree = tree, expandedFolders = expanded, sort = TreeSort.NAME).map(::withoutDates)
        )
    }

    @Test
    fun `an expanded folder inside a collapsed one stays hidden`() {
        val rows = flattenTree(
            tree = tree,
            expandedFolders = setOf(RelativePath(value = "Projects/Garden")),
            sort = TreeSort.NAME
        )

        assertEquals(
            expected = 4,
            actual = rows.size
        )
    }

    @Test
    fun `notes sort naturally by name or most recent first, folders always by name`() {
        val expanded = setOf(RelativePath(value = "Journal"))
        val journalNotes = { sort: TreeSort ->
            flattenTree(tree = tree, expandedFolders = expanded, sort = sort)
                .filter { it.path.value.startsWith(prefix = "Journal/") }
                .map { it.path.value }
        }

        assertEquals(
            expected = listOf(
                listOf("Journal/2026-10-9.md", "Journal/2026-10-10.md"),
                listOf("Journal/2026-10-9.md", "Journal/2026-10-10.md")
            ),
            actual = listOf(journalNotes(TreeSort.NAME), journalNotes(TreeSort.MODIFIED))
        )
        assertEquals(
            expected = listOf("Empty", "Journal", "Projects"),
            actual = flattenTree(tree = tree, expandedFolders = emptySet(), sort = TreeSort.MODIFIED)
                .filterIsInstance<TreeRow.Folder>()
                .map { it.path.value }
        )
    }

    @Test
    fun `notes without a date come last when sorting by date`() {
        val undated = entry(path = "Undated.md").let { it.copy(file = it.file.copy(modifiedAt = null)) }
        val withUndated = VaultTree.EMPTY.withNote(note = undated).withNote(note = entry(path = "Zebra.md"))

        assertEquals(
            expected = listOf("Zebra.md", "Undated.md"),
            actual = flattenTree(
                tree = withUndated,
                expandedFolders = emptySet(),
                sort = TreeSort.MODIFIED
            ).map { it.path.value }
        )
    }

    @Test
    fun `other files are listed with the notes by name or date, with their extension, and counted`() {
        val photos = VaultTree.EMPTY
            .withNote(note = entry(path = "Recipes/Arepas.md", modifiedOffsetMillis = 10L))
            .withAttachment(file = NoteFixtures.file(path = "Recipes/Zucchini.png", modifiedOffsetMillis = 30L))
            .withAttachment(file = NoteFixtures.file(path = "Recipes/Banana.pdf", modifiedOffsetMillis = 20L))
        val rows = { sort: TreeSort ->
            flattenTree(tree = photos, expandedFolders = setOf(RelativePath(value = "Recipes")), sort = sort)
        }

        assertEquals(
            expected = listOf(
                folder(path = "Recipes", depth = 0, count = 3, isExpanded = true),
                note(path = "Recipes/Arepas.md", depth = 1),
                TreeRow.File(path = RelativePath(value = "Recipes/Banana.pdf"), depth = 1, modifiedAt = null),
                TreeRow.File(path = RelativePath(value = "Recipes/Zucchini.png"), depth = 1, modifiedAt = null)
            ),
            actual = rows(TreeSort.NAME).map(::withoutDates)
        )
        assertEquals(
            expected = listOf("Recipes", "Recipes/Zucchini.png", "Recipes/Banana.pdf", "Recipes/Arepas.md"),
            actual = rows(TreeSort.MODIFIED).map { it.path.value }
        )
    }

    private fun folder(
        path: String,
        depth: Int,
        count: Int,
        isExpanded: Boolean = false
    ): TreeRow = TreeRow.Folder(
        path = RelativePath(value = path),
        depth = depth,
        fileCount = count,
        isExpanded = isExpanded
    )

    private fun note(
        path: String,
        depth: Int
    ): TreeRow = TreeRow.Note(path = RelativePath(value = path), depth = depth, modifiedAt = null)

    private fun withoutDates(
        row: TreeRow
    ): TreeRow = when (row) {
        is TreeRow.Note -> row.copy(modifiedAt = null)
        is TreeRow.File -> row.copy(modifiedAt = null)
        is TreeRow.Folder -> row
    }
}
