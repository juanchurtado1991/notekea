package app.notekea.chat.eval

import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.loop.FrameCount
import app.notekea.chat.domain.loop.FramePrompts
import app.notekea.chat.domain.loop.FrameReason
import app.notekea.chat.domain.loop.FrameTarget
import app.notekea.chat.domain.loop.TaskFrame
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.runTest
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

    // docs/loop-goal-plan.md, step 1.1: the frame's questions write the request after the notes, and went unrecorded.
    @Test
    fun `a frame question is kept under its task, as a frame call`() = runTest {
        val trace = EvalTrace(engine = FakeLlmEngine())

        trace
            .recorded
            .generate(
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.SYSTEM,
                        text = FramePrompts.MARK_NOTES
                    ),
                    ChatMessage(
                        role = ChatRole.USER,
                        text = "Notes:\nN1 Home/Budget\n\nRequest: the budget's total"
                    )
                ),
                options = GenerationOptions()
            )
            .collect()

        assertEquals(
            expected = listOf(TracedCall.Role.FRAME),
            actual = trace.of(task = "the budget's total").map { it.role }
        )
    }

    @Test
    fun `the frame line says what settled it or what is open`() {
        val home = RelativePath(value = "Home/Budget.md")
        val work = RelativePath(value = "Work/Budget.md")

        val settled = TaskFrame(
            targets = listOf(FrameTarget(note = work)),
            count = FrameCount.ONE,
            candidates = listOf(home, work),
            reason = FrameReason.MEMORY
        )

        val open = settled.copy(
            targets = listOf(FrameTarget(note = home), FrameTarget(note = work)),
            reason = FrameReason.MARKS
        )

        assertEquals(
            expected = listOf(
                "one, settled by memory: Work/Budget",
                "one, open between: Home/Budget; Work/Budget (by marks)"
            ),
            actual = listOf(settled, open).map { EvalTraceReport.frameOf(frame = it) }
        )
    }
}
