package app.notekea.chat.presentation

import app.notekea.chat.presentation.model.AnswerMarkdown
import app.notekea.chat.presentation.model.ChatPreviewData
import app.notekea.chat.presentation.model.ChatStep
import app.notekea.chat.presentation.model.PassedStep
import app.notekea.chat.presentation.viewmodel.changedBy
import kotlin.test.Test
import kotlin.test.assertEquals

/** The chat draws the Markdown the model writes and lists the steps an answer left (the smoke of 2026-11-13). */
class AnswerMarkdownTest {
    @Test
    fun `a citation of a source is inline content, and any other bracket, a link or code stays as written`() {
        val shown = AnswerMarkdown.of(
            text = "**Mix** it [1], see [[Recipes/Arepas|Arepas]] [7] and [2](https://example.com)\n```\na[1]\n```",
            numbers = setOf(1, 2),
            isWriting = false
        )

        assertEquals(
            expected = "**Mix** it [\\[1\\]](inline:citation:1), see **Arepas** [7] and [2](https://example.com)\n" +
                "```\na[1]\n```",
            actual = shown
        )
    }

    @Test
    fun `the caret follows the text being written, and never inside a block of code still open`() {
        val written = AnswerMarkdown.of(
            text = "Rest it ",
            numbers = emptySet(),
            isWriting = true
        )

        val code = AnswerMarkdown.of(
            text = "```kotlin\nval a",
            numbers = emptySet(),
            isWriting = true
        )

        assertEquals(
            expected = listOf("Rest it[​](inline:caret)", "```kotlin\nval a"),
            actual = listOf(written, code)
        )
    }

    @Test
    fun `an answer remembers each step it left, once, and none after it is done`() {
        val asked = ChatPreviewData.writing.copy(
            step = ChatStep.PLANNING,
            readNotes = 0
        )

        val entry = asked
            .changedBy { it.copy(step = ChatStep.SEARCHING) }
            .changedBy { it.copy(answer = "same step") }
            .changedBy {
                it.copy(
                    step = ChatStep.READING,
                    readNotes = 6
                )
            }
            .changedBy { it.copy(step = ChatStep.DONE) }
            .changedBy { it.copy(step = ChatStep.WRITING) }

        assertEquals(
            expected = listOf(
                PassedStep(
                    step = ChatStep.PLANNING,
                    readNotes = 0
                ),
                PassedStep(
                    step = ChatStep.SEARCHING,
                    readNotes = 0
                ),
                PassedStep(
                    step = ChatStep.READING,
                    readNotes = 6
                )
            ),
            actual = entry.passed
        )
    }
}
