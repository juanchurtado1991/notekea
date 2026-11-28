package app.notekea.chat.domain.loop

import app.notekea.core.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

/** What else a request could be about beside the line a change is made to, counted from what the notes write. */
class RequestCoverTest {
    private val plants = RelativePath(value = "Home/Plants.md")
    private val garden = RelativePath(value = "Home/Garden plants.md")
    private val diary = RelativePath(value = "Journal/Diary.md")

    private fun coverOf(
        task: String,
        line: String,
        texts: Map<RelativePath, String>,
        alike: List<RelativePath> = listOf(plants)
    ): Pair<List<String>, Boolean>? = RequestCover
        .of(
            task = task,
            line = line,
            target = plants,
            texts = texts,
            alike = alike
        )
        ?.let { cover ->
            cover.entries.map { it.optionFor(target = plants) } to cover.isBetter
        }

    private val sections = """
        ## Watering
        - [ ] Water the fern
        - [ ] Water the cactus
        - The can is in the shed
        ## Later
        - [ ] Water the orchid
    """.trimIndent()

    @Test
    fun `the entries beside the line are those of its section and its kind`() {
        assertEquals(
            expected = listOf("- [ ] Water the fern", "- [ ] Water the cactus") to false,
            actual = coverOf(
                task = "the watering is done",
                line = "Water the fern",
                texts = mapOf(plants to sections)
            )
        )
    }

    @Test
    fun `an entry without a name or a number the line shares with the request is another thing`() {
        val text = """
            - 09:00 call Ana
            - 09:00 call Leo
            - 11:00 call Ana
        """.trimIndent()

        assertEquals(
            expected = listOf("- 09:00 call Ana") to false,
            actual = coverOf(
                task = "move the 09:00 call with Ana",
                line = "- 09:00 call Ana",
                texts = mapOf(plants to text)
            )
        )
    }

    @Test
    fun `those that write more of the request than the line are what it is about`() {
        assertEquals(
            expected = listOf("Journal/Diary: - repot the orchid upstairs") to true,
            actual = coverOf(
                task = "the orchid upstairs is done",
                line = "Water the orchid",
                texts = mapOf(
                    plants to sections,
                    diary to "- repot the orchid upstairs"
                )
            )
        )
    }

    @Test
    fun `as much in a note of a like name is another one, and elsewhere the same thing written twice`() {
        assertEquals(
            expected = listOf("- [ ] Water the orchid", "Home/Garden plants: - [ ] Feed the orchid") to false,
            actual = coverOf(
                task = "the orchid is done",
                line = "Water the orchid",
                texts = mapOf(
                    plants to sections,
                    garden to "- [ ] Feed the orchid",
                    diary to "- saw the orchid"
                ),
                alike = listOf(plants, garden)
            )
        )
    }

    @Test
    fun `a line is the entry it is whole before one it is a part of, and one that is not there has no cover`() {
        val text = """
            ## Today
            - [ ] Call the plumber again
            ## Tomorrow
            - [ ] Call the plumber
            - [ ] Call the painter
        """.trimIndent()

        assertEquals(
            expected = listOf(
                listOf("- [ ] Call the plumber", "- [ ] Call the painter") to false,
                null
            ),
            actual = listOf("- [ ] Call the plumber", "- [ ] Call the bank").map {
                coverOf(
                    task = "done with the calls",
                    line = it,
                    texts = mapOf(plants to text)
                )
            }
        )
    }

    @Test
    fun `a long section gives the nearest entries, the line among them`() {
        val text = (1..20).joinToString(separator = "\n") { "- visit ${'a' + it}" }

        val (entries) = coverOf(
            task = "move the visit",
            line = "- visit t",
            texts = mapOf(plants to text)
        ) ?: (emptyList<String>() to false)

        assertEquals(
            expected = listOf(12, true, true),
            actual = listOf(entries.size, "- visit t" in entries, "- visit u" in entries)
        )
    }
}
