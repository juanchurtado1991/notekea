package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** A vault's bookmarks (`ROADMAP.md` 1.13): toggled, reordered, and following renames and deletions. */
class BookmarksTest {
    private val bookmarks =
        Bookmarks(paths = listOf("Welcome.md", "Recipes", "Recipes/Arepas.md", "plate.png").map(::path))

    @Test
    fun `a toggle adds at the end or removes, and a move puts a bookmark among the others`() {
        assertEquals(
            expected = listOf(
                listOf("Welcome.md", "Recipes", "Recipes/Arepas.md", "plate.png", "Ideas.md"),
                listOf("Welcome.md", "Recipes/Arepas.md", "plate.png"),
                listOf("plate.png", "Welcome.md", "Recipes", "Recipes/Arepas.md"),
                listOf("Welcome.md", "Recipes", "Recipes/Arepas.md", "plate.png")
            ),
            actual = listOf(
                bookmarks.toggled(path = path("Ideas.md")),
                bookmarks.toggled(path = path("Recipes")),
                bookmarks.moved(from = 3, to = 0),
                bookmarks.moved(from = 1, to = 9)
            ).map { it.paths.map(RelativePath::value) }
        )
    }

    @Test
    fun `a renamed folder takes the bookmarks inside it along, and a deleted one removes them`() {
        assertEquals(
            expected = listOf(
                listOf("Welcome.md", "Cooking", "Cooking/Arepas.md", "plate.png"),
                listOf("Welcome.md", "plate.png"),
                listOf("Home.md", "Recipes", "Recipes/Arepas.md", "plate.png")
            ),
            actual = listOf(
                bookmarks.renamed(from = path("Recipes"), to = path("Cooking")),
                bookmarks.deleted(path = path("Recipes")),
                bookmarks.renamed(from = path("Welcome.md"), to = path("Home.md"))
            ).map { it.paths.map(RelativePath::value) }
        )
    }

    private fun path(
        value: String
    ): RelativePath = RelativePath(value = value)
}
