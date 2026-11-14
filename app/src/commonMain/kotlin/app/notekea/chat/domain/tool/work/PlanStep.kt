package app.notekea.chat.domain.tool.work

/** A step of the plan a turn wrote for itself, and whether it is done. */
data class PlanStep(
    val text: String,
    val isDone: Boolean
)
