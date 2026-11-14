package app.notekea.chat.domain.tool

import app.notekea.chat.domain.tool.app.Arithmetic
import app.notekea.chat.domain.tool.app.CalculateTool
import app.notekea.chat.domain.tool.app.CountTool
import app.notekea.chat.domain.tool.app.DatePhrases
import app.notekea.chat.domain.tool.app.DateTool
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.TextFolder
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
            entry(path = "Recipes/Soup.md"),
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

    @Test
    fun `notes, words and tasks are counted in the vault, a folder, a tag or one note`() = runTest {
        store.texts[arepas] = "Corn flour and warm water."

        val count = CountTool(
            noteTextCache = NoteTextCache(
                contentStore = store,
                textFolder = TextFolder()
            )
        )

        assertEquals(
            expected = listOf(
                "count: 3 notes in the vault.",
                "count: 2 notes in Recipes.",
                "count: 1 notes in #latin.",
                "count: 5 words in Arepas.",
                "count: 2 open tasks in the vault.",
                "count: 1 done tasks in the vault.",
                "count: no folder, tag or note \"Trips\"."
            ),
            actual = listOf(
                said(count, "what" to "notes"),
                said(count, "what" to "notes", "where" to "Recipes"),
                said(count, "what" to "notes", "where" to "#latin"),
                said(count, "what" to "words", "where" to "Arepas"),
                said(count, "what" to "open tasks"),
                said(count, "what" to "done tasks"),
                said(count, "what" to "notes", "where" to "Trips")
            )
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
            "someday"
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
}
