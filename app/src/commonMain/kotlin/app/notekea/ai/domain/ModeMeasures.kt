package app.notekea.ai.domain

/**
 * What each chat mode scored on each size against the size's target: Auto reaches for Deep thinking or the agent
 * only on a size where that mode was measured to the target, and for Deep thinking only where it beats one draft.
 */
object ModeMeasures {
    /** Whether Auto answers a question that asks to reason with three drafts on [size]. */
    fun isDeepPicked(size: ModelSize): Boolean {
        val measured = MEASURED.getValue(key = size)

        return measured.deep >= measured.target && measured.deep > measured.fast
    }

    /** Whether Auto hands the agent what the plan or the passages could not do on [size]. */
    fun isAgentPicked(size: ModelSize): Boolean {
        val measured = MEASURED.getValue(key = size)

        return measured.agent >= measured.target
    }

    // Percent right: the 437 answerable questions with one draft ([fast]) and three ([deep]), and the agent on the
    // blind pool's 439 tasks, which it was never tuned on. docs/gemma-plan.md, "The modes measured".
    private class Measured(
        val target: Double,
        val fast: Double,
        val deep: Double,
        val agent: Double
    )

    private val MEASURED = mapOf(
        ModelSize.SMALL to Measured(
            target = 65.0,
            fast = 83.3,
            deep = 82.4,
            agent = 58.1
        ),
        ModelSize.MEDIUM to Measured(
            target = 80.0,
            fast = 88.1,
            deep = 88.6,
            agent = 79.4
        ),
        ModelSize.LARGE to Measured(
            target = 90.0,
            fast = 91.1,
            deep = 90.6,
            agent = 84.7
        )
    )
}
