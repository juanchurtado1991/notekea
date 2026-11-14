package app.notekea.chat.domain.context

/**
 * What [ContextBudget.fit] kept of what a turn found: the [entries] for the prompt, and how many of the oldest were
 * [dropped], which the turn's next fit is told.
 */
data class FittedContext(
    val entries: List<String>,
    val dropped: Int
)
