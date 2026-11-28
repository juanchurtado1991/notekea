package app.notekea.chat.domain.tool

import app.notekea.chat.domain.tool.app.Arithmetic
import app.notekea.chat.domain.tool.app.CalculateTool
import app.notekea.chat.domain.tool.app.CountTool
import app.notekea.chat.domain.tool.app.DatePhrases
import app.notekea.chat.domain.tool.app.DateTool
import app.notekea.chat.domain.tool.app.DaysTool
import app.notekea.chat.domain.tool.app.TallyTool
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.frontmatter.NoteProperty
import app.notekea.core.notes.domain.frontmatter.PropertyValue
import app.notekea.core.notes.domain.tasks.NoteTask
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * What the app does itself and does not ask of the model (`docs/agent-loop.md`, step 4): sums, counts and dates,
 * which a small model gets wrong with confidence.
 */
class AppToolsTest {
    private val store = FakeNoteContentStore()
    private val arepas = RelativePath(value = "Recipes/Arepas.md")
    private val today = CalendarDate(
        year = 2026,
        month = 11,
        day = 14
    )

    private fun task(isDone: Boolean) = NoteTask(
        stateIndex = 0,
        endIndex = 0,
        text = "a task",
        isDone = isDone
    )

    private val context = ToolContext(
        vaultPath = VAULT,
        notes = listOf(
            entry(
                path = arepas.value,
                metadata = NoteMetadata.NONE.copy(
                    tags = listOf("latin"),
                    tasks = listOf(task(isDone = false), task(isDone = true), task(isDone = false))
                )
            ),
            entry(
                path = "Recipes/Soup.md",
                metadata = NoteMetadata.NONE.copy(
                    properties = listOf(
                        NoteProperty(
                            key = "servings",
                            value = PropertyValue.Number(text = "2")
                        )
                    )
                )
            ),
            entry(path = "Books/Dune.md")
        ),
        task = "how much is the rent a year?",
        openNote = null,
        today = today
    )

    private suspend fun said(
        tool: ChatTool,
        vararg arguments: Pair<String, String>
    ): String = tool.run(
        call = ToolCall(
            name = tool.name,
            arguments = arguments.toMap()
        ),
        context = context
    ).forModel

    @Test
    fun `arithmetic is done with the usual precedence, percents and thousands, and what is not arithmetic is none`() {
        val sums = listOf(
            "4250 * 12 + 18 * 5",
            "15% of 240",
            "(2 + 3) ^ 2",
            "-4 + 10 / 4",
            "1,250.50 x 2",
            "10 / 0",
            "two plus two",
            "2 +",
            "(2 + 3"
        )

        assertEquals(
            expected = listOf("51090", "36", "25", "-1.5", "2501", null, null, null, null),
            actual = sums.map { sum ->
                Arithmetic
                    .valueOf(expression = sum)
                    ?.let { Arithmetic.written(value = it) }
            }
        )
    }

    // Code review, 2026-11-15: large and small results came back as "1.234567825E7", and -3^2 as 9.
    @Test
    fun `results are written out in full, and a minus before a power is taken after it`() {
        val sums = listOf("12345678.25 * 1", "1 / 2000", "-3^2 + 10", "2 ^ -1", "-(0.00001)")

        assertEquals(
            expected = listOf("12345678.25", "0.0005", "1", "0.5", "0"),
            actual = sums.map { sum ->
                Arithmetic
                    .valueOf(expression = sum)
                    ?.let { Arithmetic.written(value = it) }
            }
        )
    }

    @Test
    fun `a sum is answered with what it comes to, and one it cannot do says how to write it`() =
        runTest {
            val calculate = CalculateTool()

            assertEquals(
                expected = listOf("calculate: 950 * 12 = 11400", true),
                actual = listOf(
                    said(calculate, "expression" to "950 * 12"),
                    "Write only numbers" in said(calculate, "expression" to "the rent times twelve")
                )
            )
        }

    // The loop's dev run, 2026-11-18: "120 + (2027-03-01 - 2026-11-11)" came to 95.
    @Test
    fun `a sum with a date in it is refused and sent to days`() = runTest {
        val said = said(CalculateTool(), "expression" to "120 + (2027-03-01 - 2026-11-11)")

        assertEquals(
            expected = true,
            actual = "a date is no number" in said && DaysTool.NAME in said
        )
    }

    @Test
    fun `notes, words and tasks are counted in the vault, a folder, a tag or one note`() = runTest {
        store.texts[arepas] = "Corn flour and warm water."

        val count = CountTool(
            noteTextCache = NoteTextCache(contentStore = store)
        )

        assertEquals(
            expected = listOf(
                "count: 3 notes in the vault.",
                "count: 2 notes in Recipes.",
                "count: 1 notes in #latin.",
                "count: 5 words in Arepas.",
                "count: 2 open tasks in the vault.",
                "count: 1 done tasks in the vault.",
                "count: no folder, tag or note \"Trips\".",
                // A property summed: the loop's fifth run answered 10 and 4 for the 16 servings of the recipes.
                "count: servings sums 2 over 1 notes (Soup 2)."
            ),
            actual = listOf(
                said(count, "what" to "notes"),
                said(count, "what" to "notes", "where" to "Recipes"),
                said(count, "what" to "notes", "where" to "#latin"),
                said(count, "what" to "words", "where" to "Arepas"),
                said(count, "what" to "open tasks"),
                said(count, "what" to "done tasks"),
                said(count, "what" to "notes", "where" to "Trips"),
                said(count, "what" to "servings", "where" to "Recipes")
            )
        )
    }

    // The loop's dev run, 2026-11-19: the unpaid sessions were counted as the first paid one, "2026" their one word.
    @Test
    fun `a tally counts each figure on the line closest to its words, not the first that shares a year`() =
        runTest {
            store.texts[arepas] = listOf(
                "| 1 | 2026-08-12 | Assessment | \$45 | paid |",
                "| 7 | 2026-10-14 | Single-leg strength | \$45 | unpaid |",
                "| 8 | 2026-10-28 | Hop tests | \$45 | unpaid |"
            ).joinToString(separator = "\n")

            val tally = TallyTool(
                noteTextCache = NoteTextCache(contentStore = store)
            )

            val said = tally.run(
                call = ToolCall(
                    name = tally.name,
                    arguments = mapOf(
                        "how" to "sum",
                        "items" to "\$45 | 2026-10-14 Single-leg strength\n\$45 | 2026-10-28 Hop tests"
                    )
                ),
                context = context
            ).forModel

            assertEquals(
                expected = listOf(true, true, false),
                actual = listOf("Single-leg" in said, "Hop tests" in said, "Assessment" in said)
            )
        }

    // The E2B's blind pool, seed 10, 2026-11-27: "1.240 € | Pedido maderas octubre" was refused, as the table row
    // that writes it does not write the note's name.
    @Test
    fun `a tally finds a figure on a line of the note its item names`() = runTest {
        store.texts[arepas] = "| Roble francés | 0,6 m³ | 1.240 € |\n| Nogal americano | 0,5 m³ | 1.860 € |"

        val tally = TallyTool(
            noteTextCache = NoteTextCache(contentStore = store)
        )

        val said = tally.run(
            call = ToolCall(
                name = tally.name,
                arguments = mapOf(
                    "how" to "sum",
                    "items" to "1.240 € | Arepas\n1.860 € | Arepas"
                )
            ),
            context = context
        ).forModel

        assertEquals(
            expected = listOf(true, true),
            actual = listOf(said.startsWith(prefix = "tally sum of 2:"), "Roble" in said)
        )
    }

    // 14 November 2026 is a Saturday.
    @Test
    fun `a phrase names its day from today, in English and in Spanish`() {
        val phrases = listOf(
            "tomorrow",
            "pasado mañana",
            "next Friday",
            "el lunes",
            "in 3 weeks",
            "hace 2 meses",
            "end of month",
            "fin de año",
            "next month",
            "2027-02-03",
            "someday",
            // A day and a month's name: the date tool could not say "el 15 de noviembre" (the loop's fifth run).
            "el 15 de noviembre",
            "November 3rd, 2027",
            "31 de febrero"
        )

        assertEquals(
            expected = listOf(
                "2026-11-15 (Sunday)",
                "2026-11-16 (Monday)",
                "2026-11-20 (Friday)",
                "2026-11-16 (Monday)",
                "2026-12-05 (Saturday)",
                "2026-09-14 (Monday)",
                "2026-11-30 (Monday)",
                "2026-12-31 (Thursday)",
                "2026-12-14 (Monday)",
                "2027-02-03 (Wednesday)",
                null,
                "2026-11-15 (Sunday)",
                "2027-11-03 (Wednesday)",
                null
            ),
            actual = phrases.map { phrase ->
                DatePhrases
                    .of(
                        phrase = phrase,
                        today = today
                    )
                    ?.let { DatePhrases.written(date = it) }
            }
        )
    }

    @Test
    fun `a month on from the 31st is the month's last day, and the tool says today with the date`() =
        runTest {
            val january = DatePhrases.of(
                phrase = "in 1 month",
                today = CalendarDate(
                    year = 2026,
                    month = 1,
                    day = 31
                )
            )

            assertEquals(
                expected = listOf(
                    "2026-02-28 (Saturday)",
                    "date \"next Friday\": 2026-11-20 (Friday). Today is 2026-11-14 (Saturday).",
                    true
                ),
                actual = listOf(
                    january?.let { DatePhrases.written(date = it) },
                    said(DateTool(), "phrase" to "next Friday"),
                    "I cannot tell" in said(DateTool(), "phrase" to "someday")
                )
            )
        }

    // The code review of 2026-11-22: a tally refused, then made again with its figures put right, had the same key
    // ("sum of 2") and was told its result was above.
    @Test
    fun `two tallies of as many lines are two calls, and the same tally twice is one`() {
        val tally = TallyTool(
            noteTextCache = NoteTextCache(contentStore = store)
        )

        val registry = ToolRegistry(tools = listOf(tally))

        val keys =
            listOf("45 | Hop tests\n54 | Assessment", "45 | Hop tests\n45 | Assessment").map {
                registry.keyOf(
                    call = ToolCall(
                        name = tally.name,
                        arguments = mapOf(
                            "how" to "sum",
                            "items" to it
                        )
                    ),
                    context = context
                )
            }

        assertEquals(
            expected = listOf(false, "sum of 2"),
            actual = listOf(
                keys.first() == keys.last(),
                tally.targetOf(
                    call = ToolCall(
                        name = tally.name,
                        arguments = mapOf(
                            "how" to "sum",
                            "items" to "45 | Hop tests\n54 | Assessment"
                        )
                    ),
                    context = context
                )
            )
        )
    }
}
