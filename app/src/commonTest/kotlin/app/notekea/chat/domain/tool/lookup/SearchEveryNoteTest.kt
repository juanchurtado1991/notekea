package app.notekea.chat.domain.tool.lookup

import app.notekea.chat.domain.answer.AnswerFromNotesUseCase
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * A search names every note that writes its words, not only the best passages: the loop's fifth run tagged all six
 * journal notes for "the ones that mention the garden", as four passages showed none of them.
 */
class SearchEveryNoteTest {
    private val engine = FakeLlmEngine()
    private val store = FakeNoteContentStore()
    private val cache = NoteTextCache(contentStore = store)

    private val days = (20..23).map { RelativePath(value = "Journal/2026-10-$it.md") }

    private val search = SearchTool(
        answerFromNotes = AnswerFromNotesUseCase(
            textEmbedder = FakeTextEmbedder(),
            vectorIndex = FakeVectorIndex(),
            noteTextCache = cache,
            llmEngine = engine
        ),
        noteTextCache = cache
    )

    @Test
    fun `every note that writes the words is named, and the others are not`() = runTest {
        store.texts[days[0]] = "Watered the Garden after work."
        store.texts[days[1]] = "A quiet day at the office."
        store.texts[days[2]] = "Planted beans in the garden."
        store.texts[days[3]] = "Read a book."

        val said = search.run(
            call = ToolCall(
                name = SearchTool.NAME,
                arguments = mapOf(SearchTool.QUERY to "garden")
            ),
            context = ToolContext(
                vaultPath = VAULT,
                notes = days.map { entry(path = it.value) },
                task = "tag the journal notes that mention the garden with garden-days",
                openNote = null
            )
        ).forModel

        assertTrue(
            actual = "Every note that writes \"garden\" (2): Journal/2026-10-20, Journal/2026-10-22" in said,
            message = said
        )
    }

    // The code review of 2026-11-22: a word in quotes, in brackets or after "¿" kept its mark and matched no note.
    @Test
    fun `a word written in quotes, in brackets or after an opening question mark is still found`() =
        runTest {
            store.texts[days[0]] = "Watered the Garden after work."
            store.texts[days[1]] = "A quiet day at the office."

            val said = search.run(
                call = ToolCall(
                    name = SearchTool.NAME,
                    arguments = mapOf(SearchTool.QUERY to "¿\"garden\" (work)?")
                ),
                context = ToolContext(
                    vaultPath = VAULT,
                    notes = days.take(n = 2).map { entry(path = it.value) },
                    task = "which notes mention the garden and work?",
                    openNote = null
                )
            ).forModel

            assertTrue(
                actual = "Every note that writes \"garden work\" (1): Journal/2026-10-20" in said,
                message = said
            )
        }
}
