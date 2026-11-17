package app.notekea.chat.domain.tool.app

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The figures a word is written with, gathered and added by the app: asked what the "Spent" of a journal add up to,
 * the 4B added the first four of fourteen and answered with that (the loop's fifth run, 2026-11-16).
 */
class WrittenFiguresTest {
    private val journal = """
        # Japan journal

        "Spent" is what I spent that day, in yen.

        ## Day 1. Tokyo
        Ramen near the station.
        Spent: 6,400 yen.

        ## Day 2. Tokyo
        Spent: 9,800 yen.

        ## Day 3. Kyoto
        Spent: 19,600 yen.
    """.trimIndent()

    @Test
    fun `every line that writes the word is listed with its heading, and their figures are added`() {
        assertEquals(
            expected = "count: \"spent\" is written on 3 lines in Japan journal:\n" +
                "- Japan journal, under \"Day 1. Tokyo\": Spent: 6,400 yen.\n" +
                "- Japan journal, under \"Day 2. Tokyo\": Spent: 9,800 yen.\n" +
                "- Japan journal, under \"Day 3. Kyoto\": Spent: 19,600 yen.\n" +
                "These 3 figures add up to 35800.",
            actual = WrittenFigures.of(
                word = "spent",
                place = "Japan journal",
                texts = listOf("Japan journal" to journal)
            )
        )
    }

    @Test
    fun `a figure before the word is its figure too, and a word no line writes gives nothing`() {
        val notes =
            "Homework 1 (10 points), due 27 August.\nHomework 2 (15 points), due 3 September."

        val points = WrittenFigures.of(
            word = "points",
            place = "Statistics",
            texts = listOf("Statistics" to notes)
        )

        val rainfall = WrittenFigures.of(
            word = "rainfall",
            place = "Statistics",
            texts = listOf("Statistics" to notes)
        )

        assertEquals(
            expected = listOf(true, null),
            actual = listOf(
                points?.endsWith(suffix = "These 2 figures add up to 25."),
                rainfall
            )
        )
    }

    // T6: "Homework 1 (10 points), 27 August" read 27, the number after the word.
    @Test
    fun `the number right before the word is its figure, not the one after it`() {
        val notes = "Homework 1 (10 points), 27 August.\nHomework 2 (15 points), 3 September."

        assertEquals(
            expected = true,
            actual = WrittenFigures
                .of(
                    word = "points",
                    place = "Statistics",
                    texts = listOf("Statistics" to notes)
                )
                ?.endsWith(suffix = "These 2 figures add up to 25.")
        )
    }

    // T5: every day's spending was added, also the days the task did not ask about.
    @Test
    fun `the lines a word of the task picks out are added apart from the others`() {
        val counted = WrittenFigures.of(
            word = "spent",
            place = "Japan journal",
            texts = listOf("Japan journal" to journal),
            task = "how much did I spend in Tokyo?"
        )

        assertEquals(
            expected = "Of them, the 2 that write \"tokyo\" add up to 16200, and the other 1 to 19600",
            actual = counted
                ?.substringAfter(delimiter = "35800.\n")
                ?.substringBefore(delimiter = ":")
        )
    }
}
