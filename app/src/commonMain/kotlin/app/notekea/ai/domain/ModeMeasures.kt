package app.notekea.ai.domain

/**
 * What the agent scored on each size against the size's target: Auto hands it what the plan or the passages could
 * not do only on a size where it was measured to the target.
 */
object ModeMeasures {
    /** Whether Auto hands the agent what the plan or the passages could not do on [size]. */
    fun isAgentPicked(size: ModelSize): Boolean {
        val measured = MEASURED.getValue(key = size)

        return measured.agent >= measured.target
    }

    // Percent right of the agent on the blind pool's 439 tasks, which it was never tuned on. docs/gemma-plan.md,
    // "The modes measured" and "The E2B tuned".
    private class Measured(
        val target: Double,
        val agent: Double
    )

    private val MEASURED = mapOf(
        ModelSize.SMALL to Measured(
            target = 65.0,
            agent = 59.0
        ),
        ModelSize.MEDIUM to Measured(
            target = 80.0,
            agent = 79.4
        ),
        ModelSize.LARGE to Measured(
            target = 90.0,
            agent = 84.7
        )
    )
}
