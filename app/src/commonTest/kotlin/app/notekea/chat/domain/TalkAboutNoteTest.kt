package app.notekea.chat.domain

import app.notekea.chat.domain.answer.AnswerStep
import app.notekea.chat.domain.answer.ConverseUseCase
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Talk instead of an answer: about a note, it reads the note and shows it as the source. */
class TalkAboutNoteTest {
    private val store = FakeNoteContentStore()
    private val engine = FakeLlmEngine()
    private val cache = NoteTextCache(contentStore = store)

    @Test
    fun `talk about a note reads it and shows it as the source`() = runTest {
        store.texts[AREPAS] = "Mix corn flour with warm water and salt."
        engine.reply = { "I like it: it is short and clear." }

        val talk = ConverseUseCase(
            llmEngine = engine,
            noteTextCache = cache
        )(
            message = "what do you think of it?",
            history = listOf(
                ChatExchange(
                    question = "How do I make arepas?",
                    reply = "Mix corn flour."
                )
            ),
            vaultPath = VAULT,
            note = entry(path = AREPAS.value)
        ).toList().last() as AnswerStep.Done

        assertEquals(
            expected = listOf(AREPAS),
            actual = talk
                .answer
                .sources
                .map { it.note }
        )
    }

    private companion object {
        val AREPAS = RelativePath(value = "Recipes/Arepas.md")
    }
}
