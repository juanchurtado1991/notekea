package app.notekea.chat.domain.context

/**
 * What [ContextBudget.fit] kept of what a turn found: the [entries] for the prompt, how many of the oldest were
 * [dropped], which the turn's next fit is told, and the results [ContextBudget.shown] [folded] to their first line.
 */
data class FittedContext(
    val entries: List<String>,
    val dropped: Int,
    val folded: Set<Int> = emptySet()
) {
    /** The results not shown whole, which recall answers from. */
    val hidden: Set<Int> get() = (0 until dropped).toSet() + folded
}
