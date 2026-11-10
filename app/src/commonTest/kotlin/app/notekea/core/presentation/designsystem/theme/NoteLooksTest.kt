package app.notekea.core.presentation.designsystem.theme

import kotlin.test.Test
import kotlin.test.assertEquals

class NoteLooksTest {
    private val looks = NoteLooks.of(
        notePaths = listOf(
            "Home.md",
            "Recipes/Soups/Tomato soup.md",
            "Journal/Daily/2026-11-09.md",
            "Books/Reading list.md",
            "Recipes/Arepas.md"
        ),
        dailyFolder = "Journal/Daily"
    )

    // The graph colors the same folders in the same order, so a folder has one color across the app.
    @Test
    fun `a top-level folder with notes has its color by name order, shared by all inside it`() {
        assertEquals(
            expected = listOf(
                GraphPalette.colorAt(index = 0),
                GraphPalette.colorAt(index = 1),
                GraphPalette.colorAt(index = 2),
                GraphPalette.colorAt(index = 2)
            ),
            actual = listOf(
                "Books",
                "Journal/Daily/2026-11-09.md",
                "Recipes",
                "Recipes/Soups/Tomato soup.md"
            ).map { looks.colorOf(path = it) }
        )
    }

    @Test
    fun `a note at the root and a folder with no notes have no color`() {
        assertEquals(
            expected = listOf(null, null),
            actual = listOf("Home.md", "Attachments").map { looks.colorOf(path = it) }
        )
    }

    @Test
    fun `daily notes are those in the daily folder, none when they are kept at the root`() {
        assertEquals(
            expected = listOf(true, false, false),
            actual = listOf(
                looks.isDaily(path = "Journal/Daily/2026-11-09.md"),
                looks.isDaily(path = "Journal/Ideas.md"),
                looks
                    .copy(dailyFolder = "")
                    .isDaily(path = "Home.md")
            )
        )
    }
}
