package app.notekea.chat.eval

/**
 * Tasks over the `Fresh Eval` vault, written blind to the agent and the other sets
 * (`docs/eval-fresh.md`): they measure whether the agent generalises, so they must never be
 * tuned on; once they have been, a new blind vault and set replace them.
 */
internal object AgentFreshCases {
    const val VAULT = "Fresh Eval"

    val all: List<AgentWideCase> = AgentFreshAnswers.all +
        AgentFreshChanges.all +
        AgentFreshAsks.all
}
