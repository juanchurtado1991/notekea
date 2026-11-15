package app.notekea.chat.eval

import app.notekea.chat.domain.agent.AgentStep
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvalTraceReportTest {
    private val route = TracedCall(
        role = TracedCall.Role.ROUTE,
        prompt = "Request: rent in the second year?",
        written = "{\"work\":\"ask\",\"skill\":\"none\"}"
    )

    private fun step(
        results: String,
        written: String
    ) = TracedCall(
        role = TracedCall.Role.STEP,
        prompt = "Task: rent in the second year?\n\nResults so far:\n$results\n\nSteps left: 9. Your next call:",
        written = written
    )

    private val end = AgentStep.Finished(summary = "650 USD")

    @Test
    fun `a value the tools gave back and the answer left out is found, then not said`() {
        val calls = listOf(
            route,
            step(
                results = "Nothing yet.",
                written = "{\"tool\":\"read\"}"
            ),
            step(
                results = "rent rises to 676 in the second year",
                written = "{\"tool\":\"answer\"}"
            )
        )

        assertEquals(
            expected = "found it, then did not say it",
            actual = EvalTraceReport.categoryOf("does not hold [676]", end, calls)
        )
    }

    @Test
    fun `a value no tool gave back was never found`() {
        val calls = listOf(
            route,
            step(
                results = "the rent is 650 USD",
                written = "{\"tool\":\"answer\"}"
            )
        )

        assertEquals(
            expected = "never found it",
            actual = EvalTraceReport.categoryOf("does not hold [676]", end, calls)
        )
    }

    @Test
    fun `a change routed to ask is counted as routed wrong`() {
        assertEquals(
            expected = "routed to ask, which cannot end that way",
            actual = EvalTraceReport.categoryOf(
                problem = "ended as Finished, not as PROPOSE",
                end = end,
                calls = listOf(route)
            )
        )
    }

    @Test
    fun `each call is shown once after what it was newly given`() {
        val traced = EvalTraceReport.traced(
            calls = listOf(
                route,
                step(
                    results = "Nothing yet.",
                    written = "{\"tool\":\"read\"}"
                ),
                step(
                    results = "the rent is 650 USD",
                    written = "{\"tool\":\"answer\"}"
                )
            )
        )

        assertTrue(actual = "- 1. route: {\"work\":\"ask\"" in traced)
        val answered = "  - gave: the rent is 650 USD\n- 3. step: {\"tool\":\"answer\"}"

        assertTrue(actual = answered in traced)

        assertEquals(
            expected = "ask",
            actual = EvalTraceReport.routeOf(calls = listOf(route))
        )
    }

    @Test
    fun `a call that stops before its JSON closes is counted as cut off`() {
        val calls = listOf(
            route,
            step(
                results = "Nothing yet.",
                written = "{\"tool\":\"write_note\",\"text\":\"A long"
            )
        )

        assertEquals(
            expected = "wrote a call too long to end within its tokens",
            actual = EvalTraceReport.categoryOf("no note Summary written: nothing", end, calls)
        )
    }
}
