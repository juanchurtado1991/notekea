package app.notekea.chat.domain.tool

import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.plan.FoundNotes
import app.notekea.chat.domain.plan.FoundRow
import app.notekea.chat.domain.tool.app.CountTool
import app.notekea.chat.domain.tool.lookup.TasksTool
import app.notekea.chat.domain.tool.reading.DigestTool
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.llm.domain.LlmError
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.TextFolder
import app.notekea.core.notes.domain.frontmatter.NoteProperty
import app.notekea.core.notes.domain.frontmatter.PropertyValue
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The chat tools' findings of the code review of 2026-11-22, each as the rule that fixes it. */
class CodeReviewToolsTest {
    private val engine = FakeLlmEngine()
    private val store = FakeNoteContentStore()
    private val runner = FakeActionRunner()
    private val lease = RelativePath(value = "Home/Lease.md")

    private val cache = NoteTextCache(
        contentStore = store,
        textFolder = TextFolder
    )

    private val context = ToolContext(
        vaultPath = VAULT,
        notes = listOf(
            entry(
                path = lease.value,
                metadata = NoteMetadata.NONE.copy(
                    properties = listOf(
                        NoteProperty(
                            key = "keywords",
                            value = PropertyValue.Number(text = "3")
                        )
                    )
                )
            )
        ),
        task = "what is due until tomorrow?",
        openNote = null,
        today = CalendarDate(
            year = 2026,
            month = 11,
            day = 14
        )
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

    // The code review of 2026-11-22: "today" and "tomorrow" were not read, and every task came back unfiltered.
    @Test
    fun `the tasks tool reads a day said in words, and says which day it cannot read`() = runTest {
        runner.found = FoundNotes(
            isTasks = true,
            rows = listOf(
                row(
                    text = "water the plants",
                    date = CalendarDate(
                        year = 2026,
                        month = 11,
                        day = 15
                    )
                ),
                row(
                    text = "renew the passport",
                    date = CalendarDate(
                        year = 2027,
                        month = 2,
                        day = 3
                    )
                )
            )
        )

        val tasks = TasksTool(actionRunner = runner)
        val soon = said(tasks, "from" to "today", "to" to "tomorrow")

        assertEquals(
            expected = listOf(
                listOf("water the plants"),
                "tasks: I cannot tell which day soon is. Write it as 2026-12-06."
            ),
            actual = listOf(
                listOf("water the plants", "renew the passport").filter {
                    it in soon
                },
                said(tasks, "from" to "soon")
            )
        )
    }

    // The code review of 2026-11-22: a model failure inside a digest was not caught, and ended the whole turn.
    @Test
    fun `a digest whose model fails says its parts were not read`() = runTest {
        store.texts[lease] = "The rent is 950 euros a month."
        engine.failure = LlmError.EngineFailed

        val digest = said(
            DigestTool(
                llmEngine = engine,
                noteTextCache = cache
            ),
            "notes" to "Lease",
            "question" to "how much is the rent?"
        )

        assertEquals(
            expected = true,
            actual = "could not be read, as the model failed" in digest
        )
    }

    // The code review of 2026-11-22: "keywords" holds "words", so the property was counted as the notes' words.
    @Test
    fun `a property whose name holds words or notes is summed, not counted as them`() = runTest {
        store.texts[lease] = "The rent is 950 euros a month."

        assertEquals(
            expected = "count: keywords sums 3 over 1 notes (Lease 3).",
            actual = said(CountTool(noteTextCache = cache), "what" to "keywords")
        )
    }

    private fun row(
        text: String,
        date: CalendarDate
    ) = FoundRow(
        note = lease,
        cells = listOf(text),
        due = "later",
        date = date
    )
}
