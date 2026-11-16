package app.notekea.chat.eval

import app.notekea.chat.domain.agent.AgentStep

/** One web task as worked, a [Turn] at a time as the person went on with it, in [seconds]. */
internal class WebEvalResult(
    val case: WebEvalCase,
    val turns: List<Turn>,
    val seconds: Long
) {
    /** What the person [asked] in one turn, and the [steps] it took. */
    class Turn(
        val asked: String,
        val steps: List<AgentStep>
    )

    /** The last turn's end, which is what is graded. */
    val end: AgentStep
        get() = turns
            .last()
            .steps
            .last()

    /** Every step of every turn. */
    val steps: List<AgentStep> get() = turns.flatMap { it.steps }
}
