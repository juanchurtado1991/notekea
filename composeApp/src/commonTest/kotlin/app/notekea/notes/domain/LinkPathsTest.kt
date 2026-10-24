package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class LinkPathsTest {
    @Test
    fun `percent runs decode as UTF-8 and everything else stays`() {
        assertEquals(
            expected = listOf("Café con leche.md", "100% sure.md", "Lisbon trip.md", "🍅 Tomatoes.md"),
            actual = listOf(
                "Caf%C3%A9%20con%20leche.md",
                "100% sure.md",
                "Lisbon%20trip.md",
                "🍅%20Tomatoes.md"
            ).map(::percentDecoded)
        )
    }

    @Test
    fun `targets resolve from a folder, climbing with dot dot but never above the vault`() {
        val garden = RelativePath(value = "Projects/Garden")

        assertEquals(
            expected = listOf("Projects/Garden/Seeds.md", "Projects/Roadmap.md", "Recipes/Arepas.md", null),
            actual = listOf("Seeds.md", "../Roadmap.md", "./../../Recipes/Arepas.md", "../../../Outside.md").map {
                resolvedFrom(folder = garden, target = it)?.value
            }
        )
    }

    @Test
    fun `a path relative to a folder climbs only what is not shared`() {
        val garden = RelativePath(value = "Projects/Garden")

        assertEquals(
            expected = listOf("Seeds.md", "../Roadmap.md", "../../Recipes/Arepas.md", "Recipes/Arepas.md"),
            actual = listOf(
                relativeTo(folder = garden, target = RelativePath(value = "Projects/Garden/Seeds.md")),
                relativeTo(folder = garden, target = RelativePath(value = "Projects/Roadmap.md")),
                relativeTo(folder = garden, target = RelativePath(value = "Recipes/Arepas.md")),
                relativeTo(folder = RelativePath.ROOT, target = RelativePath(value = "Recipes/Arepas.md"))
            )
        )
    }

    @Test
    fun `only spaces are encoded, as Obsidian writes them`() {
        assertEquals(
            expected = "Recipes/Café%20con%20leche.md",
            actual = spacesEncoded(target = "Recipes/Café con leche.md")
        )
    }
}
