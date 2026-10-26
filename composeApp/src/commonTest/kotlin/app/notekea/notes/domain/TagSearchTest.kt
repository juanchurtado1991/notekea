package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class TagSearchTest {
    private val search = TagSearch()
    private val tags = listOf("smoke", "recipe", "recipe/drinks", "travel", "weekend-recipe")

    @Test
    fun `the most used tags come first, and typed text finds tags starting with it before tags containing it`() {
        assertEquals(
            expected = listOf(
                listOf("smoke", "recipe"),
                listOf("recipe", "recipe/drinks", "weekend-recipe"),
                listOf("travel")
            ),
            actual = listOf(
                search.search(tags = tags, query = "", limit = 2),
                search.search(tags = tags, query = "Rec", limit = 10),
                search.search(tags = tags, query = "#trav", limit = 10)
            )
        )
    }

    @Test
    fun `typed text is offered as a new tag only when it is a valid tag the vault does not have`() {
        assertEquals(
            expected = listOf("weekend-plan", "lunch", null, null, null),
            actual = listOf("weekend plan", "#lunch", "Recipe", "2026", "a.b").map {
                search.newTag(tags = tags, query = it)
            }
        )
    }
}
