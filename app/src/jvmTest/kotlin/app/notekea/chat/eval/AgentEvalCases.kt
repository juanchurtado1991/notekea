package app.notekea.chat.eval

import app.notekea.chat.domain.agent.AgentStep
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionStep

/** A task for [AgentEvalChatModelTest]: what is asked and what its end must hold; the problem found, or null. */
class AgentEvalCase(
    val task: String,
    val check: (end: AgentStep) -> String?
)

/**
 * The tasks agent mode is measured on (4.7), over the committed Sample Vault: each needs a look-up before it knows what
 * to change or say. A task that changes notes must end on a card that names them; one that asks must say the answer.
 */
object AgentEvalCases {
    private val VENEZUELAN = setOf("Arepas", "Black beans")
    private val UNFINISHED = setOf("Atomic Habits", "Sapiens")
    private val RATED_FIVE = setOf("Deep Work", "Dune", "The Pragmatic Programmer")
    private val HOME = setOf("Kitchen renovation", "Vendor call")

    val all = listOf(
        tags(
            task = "find my Venezuelan recipes and tag them with latin",
            tag = "latin",
            names = VENEZUELAN
        ),
        tags(
            task = "tag the books I have not finished yet with to-read",
            tag = "to-read",
            names = UNFINISHED
        ),
        tags(
            task = "tag every book rated 5 with favorite",
            tag = "favorite",
            names = RATED_FIVE
        ),
        tags(
            task = "tag the notes about my home renovation with house",
            tag = "house",
            names = HOME
        ),
        tags(
            task = "busca las recetas venezolanas y etiquétalas con latina",
            tag = "latina",
            names = VENEZUELAN
        ),
        property(
            task = "set status done on the project that is on hold",
            property = "status",
            note = "Kitchen renovation"
        ),
        property(
            task = "mark the books I have finished with finished_on 2026-11-01",
            property = "finished_on",
            names = RATED_FIVE
        ),
        property(
            task = "give the soup recipe a rating of 4",
            property = "rating",
            note = "Tomato soup"
        ),
        property(
            task = "the Lisbon trip is now active: set its status",
            property = "status",
            note = "Trip to Lisbon"
        ),
        writes(
            task = "read my October meetings and write a note October meetings summary linking each",
            title = "October meetings summary"
        ),
        writes(
            task = "write a note Book ratings with a table of each book and its rating",
            title = "Book ratings"
        ),
        writes(
            task = "look at my open tasks and write a note This week with the ones due soonest",
            title = "This week"
        ),
        writes(
            task = "make a note Venezuelan dinner with a menu from my Venezuelan recipes",
            title = "Venezuelan dinner"
        ),
        writes(
            task = "escribe una nota Resumen de libros con lo que aprendí de cada libro",
            title = "Resumen de libros"
        ),
        task(
            task = "add a task to buy beans to the recipe that uses black beans",
            note = "Black beans"
        ),
        task(
            task = "add a task 'pack the bags' to the note about my trip",
            note = "Trip to Lisbon"
        ),
        task(
            task = "the kickoff meeting has an open task for Ana; add 'review the announcement' to that meeting",
            note = "2026-10-21 Kickoff"
        ),
        links(
            task = "link the weekly menu to every recipe it uses",
            note = "Weekly menu"
        ),
        links(
            task = "link the budget meeting to the project it talks about",
            note = "2026-10-24 Budget"
        ),
        moves(
            task = "create a folder Done projects and move the project that is on hold into it",
            note = "Kitchen renovation"
        ),
        moves(
            task = "move my Venezuelan recipes into a new folder Recipes/Venezuelan",
            names = VENEZUELAN
        ),
        says(
            task = "who owns the Notekea launch and when is it due?",
            words = listOf("Ana", "11-15")
        ),
        says(
            task = "how many books have I finished, and which?",
            words = listOf("3")
        ),
        says(
            task = "which meetings did Marta attend?",
            words = listOf("Kickoff", "Design review")
        ),
        says(
            task = "what is the budget of the kitchen renovation and who is the vendor?",
            words = listOf("4500", "Stone")
        ),
        says(
            task = "how many servings do the arepas make?",
            words = listOf("6")
        ),
        says(
            task = "how far am I in my reading challenge?",
            words = listOf("17", "24")
        ),
        says(
            task = "when are the Lisbon flights to be booked?",
            words = listOf("11-20")
        ),
        says(
            task = "¿qué libros tienen calificación 5?",
            words = listOf("Dune", "Deep Work")
        ),
        says(
            task = "which tasks of the launch are still open?",
            words = emptyList()
        )
    )

    private fun tags(
        task: String,
        tag: String,
        names: Set<String>
    ) = AgentEvalCase(task = task) { end ->
        val tagged = namesIn(end, ActionKind.ADD_TAG) {
            it.tag?.removePrefix("#") == tag
        }

        if (tagged == names) null else "tagged $tagged with $tag, not $names: ${shown(end)}"
    }

    private fun property(
        task: String,
        property: String,
        note: String? = null,
        names: Set<String> = setOfNotNull(note)
    ) = AgentEvalCase(task = task) { end ->
        val set = namesIn(end, ActionKind.SET_PROPERTY) {
            it.property == property
        }

        if (set == names) null else "set $property on $set, not $names: ${shown(end)}"
    }

    private fun writes(task: String, title: String) = AgentEvalCase(task = task) { end ->
        val steps = (end as? AgentStep.Proposed)?.steps.orEmpty()

        val written = steps.firstOrNull {
            it.title.equals(title, ignoreCase = true) && it.content.isNotEmpty()
        }

        when {
            written == null -> "no note $title written: ${shown(end)}"
            written.content.sumOf { it.text.length } < MIN_TEXT -> "$title is almost empty"
            else -> null
        }
    }

    private fun task(task: String, note: String) = AgentEvalCase(task = task) { end ->
        val isAdded = note in namesIn(end, ActionKind.ADD_TASK)

        if (isAdded) null else "no task added to $note: ${shown(end)}"
    }

    private fun links(task: String, note: String) = AgentEvalCase(task = task) { end ->
        val steps = (end as? AgentStep.Proposed)?.steps.orEmpty()

        val isLinked = steps.any { it.kind == ActionKind.LINK_NOTES && note in it.names() } ||
            steps.any {
                it.kind == ActionKind.APPEND_TEXT &&
                    note in it.names() &&
                    "[[" in it.text.orEmpty()
            }

        if (isLinked) null else "$note links nothing: ${shown(end)}"
    }

    private fun moves(
        task: String,
        note: String? = null,
        names: Set<String> = setOfNotNull(note)
    ) = AgentEvalCase(task = task) { end ->
        val moved = namesIn(end, ActionKind.MOVE)

        if (moved == names) null else "moved $moved, not $names: ${shown(end)}"
    }

    private fun says(task: String, words: List<String>) = AgentEvalCase(task = task) { end ->
        val summary = (end as? AgentStep.Finished)?.summary
        val missing = words.filterNot { summary.orEmpty().contains(it, ignoreCase = true) }

        when {
            summary.isNullOrBlank() -> "no answer: ${shown(end)}"
            missing.isNotEmpty() -> "the answer misses $missing: $summary"
            else -> null
        }
    }

    /** The names of the notes [end]'s card gives the steps of [kind] that [keep] keeps. */
    private fun namesIn(
        end: AgentStep,
        kind: ActionKind,
        keep: (ActionStep) -> Boolean = { true }
    ): Set<String> {
        val steps = (end as? AgentStep.Proposed)?.steps.orEmpty()

        return steps
            .filter {
                it.kind == kind && keep(it)
            }
            .flatMap { it.names() }
            .toSet()
    }

    private fun ActionStep.names(): List<String> = notes.map { it.nameWithoutExtension }

    private fun shown(end: AgentStep): String = when (end) {
        is AgentStep.Proposed -> end.steps.joinToString { "${it.kind.written} ${it.names()}" }
        else -> end.toString().take(n = SHOWN_CHARS)
    }

    private const val MIN_TEXT = 80
    private const val SHOWN_CHARS = 200
}
