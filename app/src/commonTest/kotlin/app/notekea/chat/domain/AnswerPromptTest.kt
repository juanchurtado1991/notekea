package app.notekea.chat.domain

import app.notekea.chat.domain.prompt.AnswerPrompt
import kotlin.test.Test
import kotlin.test.assertEquals

/** How a reply written from excerpts is read as not found (Auto-Test 3.3-05, 2026-11-13). */
class AnswerPromptTest {
    @Test
    fun `a reply that says what was asked was not found in the notes is not found`() {
        assertEquals(
            expected = listOf(true, true, false),
            actual = listOf(
                AnswerPrompt.isNotFound(text = "No dentist appointment was found in your notes or tasks."),
                AnswerPrompt.isNotFound(text = "No trips are mentioned in the notes."),
                AnswerPrompt.isNotFound(text = "No, the dough rests for five minutes [1].")
            )
        )
    }
}
