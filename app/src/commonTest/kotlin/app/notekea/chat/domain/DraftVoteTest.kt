package app.notekea.chat.domain

import app.notekea.chat.domain.agent.DraftVote
import app.notekea.chat.domain.prompt.AnswerPrompt
import kotlin.test.Test
import kotlin.test.assertEquals

class DraftVoteTest {
    @Test
    fun `the draft the others agree with wins, and most drafts finding nothing find nothing`() {
        val agreed = "The dough takes two and a half cups of water [1]."

        assertEquals(
            expected = listOf(agreed, null, agreed),
            actual = listOf(
                DraftVote.winner(
                    drafts = listOf(
                        "It takes 3 cups of milk [2].",
                        agreed,
                        "The dough takes two and a half cups of warm water [1]."
                    )
                ),
                DraftVote.winner(drafts = listOf(AnswerPrompt.NOT_FOUND, agreed, " NOT_FOUND ")),
                DraftVote.winner(drafts = listOf(AnswerPrompt.NOT_FOUND, agreed, agreed))
            )
        )
    }
}
