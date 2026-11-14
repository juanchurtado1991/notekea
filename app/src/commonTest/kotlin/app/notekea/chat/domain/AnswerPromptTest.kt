package app.notekea.chat.domain

import app.notekea.chat.domain.answer.AnswerFromFoundUseCase
import app.notekea.chat.domain.answer.AnswerStep
import app.notekea.chat.domain.plan.FoundNotes
import app.notekea.chat.domain.prompt.AnswerPrompt
import app.notekea.core.llm.domain.FakeLlmEngine
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** How a reply is read as not found (Auto-Test 3.3-05, 2026-11-13). */
class AnswerPromptTest {
    @Test
    fun `a reply that says what was asked was not found in the notes is not found`() {
        assertEquals(
            // The code review of 2026-11-13: "No hay…", "There is no…" also begin answers, which cite.
            expected = listOf(true, true, false, false, false, true),
            actual = listOf(
                AnswerPrompt.isNotFound(text = "No dentist appointment was found in your notes or tasks."),
                AnswerPrompt.isNotFound(text = "No trips are mentioned in the notes."),
                AnswerPrompt.isNotFound(text = "No, the dough rests for five minutes [1]."),
                AnswerPrompt.isNotFound(text = "No hay azúcar en la lista de ingredientes [1]."),
                AnswerPrompt.isNotFound(text = "There is no sugar in the list of ingredients [2]."),
                AnswerPrompt.isNotFound(text = "No hay nada sobre el dentista en tus notas.")
            )
        )
    }

    @Test
    fun `tables that do not answer the question give the not found reply, not the model's words`() =
        runTest {
            val engine = FakeLlmEngine()
            engine.reply = { "No dentist appointment was found in your notes or tasks." }

            val last = AnswerFromFoundUseCase(llmEngine = engine)(
                question = "When is my dentist appointment?",
                tables = listOf(
                    FoundNotes(
                        isTasks = true,
                        rows = emptyList()
                    )
                )
            ).last()

            assertEquals(
                expected = AnswerStep.NotFound,
                actual = last
            )
        }
}
