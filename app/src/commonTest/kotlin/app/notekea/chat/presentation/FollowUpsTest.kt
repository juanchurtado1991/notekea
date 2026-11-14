package app.notekea.chat.presentation

import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.answer.AnswerSource
import app.notekea.chat.presentation.model.ChatPreviewData
import app.notekea.chat.presentation.model.FollowUps
import app.notekea.core.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

/** What the chat suggests asking next (the developer's request of 2026-11-13): the headings of the notes it read. */
class FollowUpsTest {
    private fun source(
        number: Int,
        heading: String
    ) = AnswerSource(
        number = number,
        note = RelativePath(value = "Web sources/Themes.md"),
        headingPath = listOf("Themes", heading),
        range = 0..1,
        text = "Text."
    )

    @Test
    fun `the headings it did not cite come first, a clip's own sections and repeats are left out, three at most`() {
        val answered = ChatPreviewData.answered.copy(
            answer = "A theme sets the look [1].",
            sources = listOf(
                source(
                    number = 1,
                    heading = "What is a theme?"
                ),
                source(
                    number = 2,
                    heading = "Original text"
                ),
                source(
                    number = 3,
                    heading = "Block themes"
                ),
                source(
                    number = 4,
                    heading = "block themes"
                ),
                source(
                    number = 5,
                    heading = "Classic themes"
                ),
                source(
                    number = 6,
                    heading = "Child themes"
                )
            )
        )

        assertEquals(
            expected = listOf(
                listOf("Block themes", "Classic themes", "Child themes"),
                emptyList()
            ),
            actual = listOf(
                FollowUps.of(entry = answered),
                FollowUps.of(entry = answered.copy(outcome = TurnOutcome.NOT_FOUND))
            )
        )
    }
}
