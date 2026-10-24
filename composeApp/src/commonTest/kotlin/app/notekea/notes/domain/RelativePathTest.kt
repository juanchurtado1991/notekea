package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class RelativePathTest {
    @Test
    fun `name, parent and display name come from the last segment`() {
        val note = RelativePath(value = "Projects/Garden/Tomato plan.md")

        assertEquals(
            expected = listOf("Tomato plan.md", "Tomato plan", "Projects/Garden"),
            actual = listOf(note.name, note.nameWithoutExtension, note.parent?.value)
        )
    }

    @Test
    fun `top-level entries live in the root and the root has no parent`() {
        assertEquals(
            expected = listOf(RelativePath.ROOT, null),
            actual = listOf(RelativePath(value = "Welcome.md").parent, RelativePath.ROOT.parent)
        )
    }

    @Test
    fun `the markdown extension is recognized in any case and kept for other files`() {
        val paths = listOf("Welcome.md", "README.MD", "shopping.txt", "md").map { RelativePath(value = it) }

        assertEquals(
            expected = listOf(true, true, false, false),
            actual = paths.map { it.isMarkdown }
        )
        assertEquals(
            expected = listOf("Welcome", "README", "shopping.txt", "md"),
            actual = paths.map { it.nameWithoutExtension }
        )
    }

    @Test
    fun `children are joined with a slash and the root adds no separator`() {
        assertEquals(
            expected = listOf("Journal", "Journal/2026-10-24.md"),
            actual = listOf(
                RelativePath.ROOT.child(name = "Journal").value,
                RelativePath(value = "Journal").child(name = "2026-10-24.md").value
            )
        )
    }

    @Test
    fun `a path is within a folder only through a whole segment`() {
        val folder = RelativePath(value = "Travel")

        assertEquals(
            expected = listOf(true, true, false, true),
            actual = listOf(
                RelativePath(value = "Travel").isWithin(folder = folder),
                RelativePath(value = "Travel/Lisbon trip.md").isWithin(folder = folder),
                RelativePath(value = "Travel plans.md").isWithin(folder = folder),
                RelativePath(value = "Travel plans.md").isWithin(folder = RelativePath.ROOT)
            )
        )
    }
}
