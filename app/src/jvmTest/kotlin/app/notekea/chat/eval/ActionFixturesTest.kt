package app.notekea.chat.eval

import app.notekea.core.llm.domain.FakeLlmEngine
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ActionFixturesTest {
    // The routing net of the one agent loop (docs/agent-loop.md, step 0): the model's recorded replies give the
    // plans they gave when recorded, so a change to what reads and reshapes a plan shows here, with no model.
    @Test
    fun `every recorded reply of the model still becomes the plan it became`() = runBlocking {
        val requests = ActionFixtures.requests().associateBy {
            it.get(ActionFixtures.REQUEST).asText()
        }

        val recorded = ActionFixtures.recorded()

        val changed = recorded.filter { fixture ->
            val replies = fixture
                .get(ActionFixtures.REPLIES)
                .map { it.asText() }

            var next = 0

            val engine = FakeLlmEngine().apply {
                // A plan asked more times than when recorded gets the last reply again.
                reply = {
                    replies[next++.coerceAtMost(maximumValue = replies.lastIndex)]
                }
            }

            val isRight = ActionFixtures.isRight(
                request = requests.getValue(fixture.get(ActionFixtures.REQUEST).asText()),
                engine = engine
            )

            isRight != fixture.get(ActionFixtures.RIGHT).asBoolean()
        }

        assertEquals(
            expected = emptyList(),
            actual = changed.map { it.get(ActionFixtures.REQUEST).asText() }
        )

        assertEquals(
            expected = requests.size,
            actual = recorded.size
        )
    }
}
