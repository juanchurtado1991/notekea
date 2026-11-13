package app.notekea.chat.domain.agent

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionProblem

/**
 * When a request goes to agent mode (4.7) instead of the plan of 3.9: help, the vault's overview and talk never do,
 * whatever the mode, since the plan answers them better.
 */
enum class AgentUse {
    /** Normal and Deep thinking, and a model that does not offer it. */
    NEVER,

    /** Auto: only a plan that names something the vault does not hold as named ("the recipe with black beans"). */
    WHEN_STUCK,

    /** Agent mode: every question and action. */
    FOR_WORK;

    /** Whether [problem] of a checked plan is one a look-up may solve, for [WHEN_STUCK]. */
    fun isStuckOn(problem: ActionProblem): Boolean {
        val isUnnamed = problem is ActionProblem.Missing && problem.field in NAMING_FIELDS

        val isUnknown =
            problem is ActionProblem.UnknownNote ||
                problem is ActionProblem.UnknownFolder ||
                problem is ActionProblem.UnknownTask ||
                problem is ActionProblem.UnknownQuery

        return this == WHEN_STUCK && (isUnknown || isUnnamed)
    }

    private companion object {
        val NAMING_FIELDS =
            setOf(ActionField.NOTE, ActionField.NOTES, ActionField.TARGET, ActionField.FOLDER)
    }
}
