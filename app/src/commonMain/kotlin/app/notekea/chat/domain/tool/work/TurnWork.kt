package app.notekea.chat.domain.tool.work

/**
 * A turn's working memory (`docs/agent-loop.md`): the plan it wrote and ticks, and the facts it noted with their
 * passages. Both stay in the prompt whatever is trimmed, so a long task does not forget what it was doing or found.
 */
class TurnWork {
    private val steps = mutableListOf<PlanStep>()
    private val facts = mutableListOf<NotedFact>()

    val plan: List<PlanStep> get() = steps.toList()

    val noted: List<NotedFact> get() = facts.toList()

    /** The plan written again, whole: the model rewrites it as it goes, ticking what is done. */
    fun plan(steps: List<PlanStep>) {
        this.steps.clear()
        this.steps += steps
    }

    /** A fact kept once: noted again in the same words, it is not repeated. */
    fun note(fact: NotedFact) {
        if (facts.none { it.fact == fact.fact }) facts += fact
    }

    /** Both as the model reads them at every move; empty when the turn has neither. */
    fun described(): String {
        val planned = steps
            .takeIf { it.isNotEmpty() }
            ?.joinToString(
                separator = "\n",
                prefix = "Your plan:\n"
            ) { "- [${if (it.isDone) "x" else " "}] ${it.text}" }

        val known = facts
            .takeIf { it.isNotEmpty() }
            ?.joinToString(
                separator = "\n",
                prefix = "What you noted:\n"
            ) { "- ${it.fact} (\"${it.passage}\")" }

        return listOfNotNull(planned, known).joinToString(separator = "\n\n")
    }
}
