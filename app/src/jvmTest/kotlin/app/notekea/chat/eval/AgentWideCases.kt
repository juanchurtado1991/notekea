package app.notekea.chat.eval

import app.notekea.chat.domain.agent.AgentStep

/**
 * A task of the wider agent evaluation (`docs/agent-loop.md`, step 0), over the `Agent Eval` vault: what is asked,
 * the [tools] of the design the loop should use for it, how the turn [ends], and what its answer or its card must
 * hold ([holds]; [computed] are those the vault does not write, worked out from it) and must not ([lacks]).
 */
class AgentWideCase(
    val task: String,
    val kind: Kind,
    val tools: List<String>,
    val ends: End,
    val holds: List<String> = emptyList(),
    val computed: List<String> = emptyList(),
    val lacks: List<String> = emptyList()
) {
    enum class Kind { DOCUMENT, LONG_NOTE, AMBIGUOUS, SEVERAL_NOTES }

    enum class End { ANSWER, PROPOSE, ASK }

    /** This task for today's agent, which has no way to ask: a question back is a finished turn that asks. */
    fun forToday(): AgentEvalCase = checked(canAsk = false)

    /** This task for the one loop, which asks with `ask_user`. */
    fun forLoop(): AgentEvalCase = checked(canAsk = true)

    private fun checked(canAsk: Boolean): AgentEvalCase = AgentEvalCase(task = task) { end ->
        val text = when (end) {
            is AgentStep.Finished -> end.summary
            is AgentStep.Proposed -> end.summary + end.steps
            is AgentStep.Asked -> end.question + end.options
            else -> ""
        }.replace(
            oldValue = ",",
            newValue = ""
        ).lowercase()

        val missing = (holds + computed).filterNot {
            it.plain() in text
        }

        val unwanted = lacks.filter {
            it.plain() in text
        }

        val isQuestion = end is AgentStep.Finished && "?" in end.summary
        val isAsked = if (canAsk) end is AgentStep.Asked else isQuestion

        val isEnd = when (ends) {
            End.ANSWER -> end is AgentStep.Finished
            End.PROPOSE -> end is AgentStep.Proposed
            End.ASK -> isAsked
        }

        when {
            !isEnd -> "ended as ${end::class.simpleName}, not as $ends"
            missing.isNotEmpty() -> "does not hold $missing"
            unwanted.isNotEmpty() -> "holds $unwanted"
            else -> null
        }
    }

    private fun String.plain(): String = replace(
        oldValue = ",",
        newValue = ""
    ).lowercase()
}

/** The tasks today's 30 do not cover: documents, long notes, requests that can be read two ways, several notes. */
object AgentWideCases {
    const val VAULT = "Agent Eval"

    private val READ = listOf("outline", "read", "answer")
    private val ASK = listOf("list", "ask_user")

    private val documents = listOf(
        AgentWideCase(
            task = "what is the monthly fee in the service agreement, and how can either side end it?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = READ,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("4,250", "60 days")
        ),
        AgentWideCase(
            task = "make a note Agreement rates with a table of the rates in Annex B of the service agreement",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = listOf("outline", "read", "write_note", "propose"),
            ends = AgentWideCase.End.PROPOSE,
            holds = listOf("Agreement rates")
        ),
        AgentWideCase(
            task = "summarize the service agreement in a note Agreement summary: parties, term, fee, how it ends",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = listOf("outline", "digest", "write_note", "propose"),
            ends = AgentWideCase.End.PROPOSE,
            holds = listOf("Agreement summary")
        ),
        AgentWideCase(
            task = "who owns the data in the service agreement, and what happens to it when the agreement ends?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = READ,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("Client", "15 days")
        ),
        AgentWideCase(
            task = "what does the service agreement say about paying in colones?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = listOf("search", "answer"),
            ends = AgentWideCase.End.ANSWER,
            lacks = listOf("exchange rate")
        ),
        AgentWideCase(
            task = "per the service agreement, how much interest is due on one monthly fee paid two months late?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("1.5"),
            computed = listOf("127.5")
        ),
        AgentWideCase(
            task = "how many member farms does the service agreement list in Naranjo and in Pital together?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = AgentWideCase.End.ANSWER,
            computed = listOf("75")
        ),
        AgentWideCase(
            task = "en el contrato de servicio, ¿hasta qué hora atiende soporte los sábados de diciembre?",
            kind = AgentWideCase.Kind.DOCUMENT,
            tools = READ,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("20:00")
        )
    )

    private val longNotes = listOf(
        AgentWideCase(
            task = "what did the last study in my sleep reading notes find?",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = READ,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("1.3")
        ),
        AgentWideCase(
            task = "which studies in my sleep reading notes used naps, and what did each find?",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = listOf("outline", "digest", "answer"),
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("Lindqvist", "Bauer", "Castellanos")
        ),
        AgentWideCase(
            task = "write a note Sleep takeaways with one line for each study of my sleep reading notes",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = listOf("outline", "digest", "write_note", "propose"),
            ends = AgentWideCase.End.PROPOSE,
            holds = listOf("Sleep takeaways")
        ),
        AgentWideCase(
            task = "how many studies are in my sleep reading notes, and which one had the most people?",
            kind = AgentWideCase.Kind.LONG_NOTE,
            tools = listOf("outline", "read", "answer"),
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("Ivanova")
        )
    )

    private val ambiguous = listOf(
        AgentWideCase(
            task = "open the budget",
            kind = AgentWideCase.Kind.AMBIGUOUS,
            tools = ASK,
            ends = AgentWideCase.End.ASK,
            holds = listOf("Home", "Work")
        ),
        AgentWideCase(
            task = "add 500 for seeds to the budget",
            kind = AgentWideCase.Kind.AMBIGUOUS,
            tools = ASK,
            ends = AgentWideCase.End.ASK,
            holds = listOf("Home", "Work")
        ),
        AgentWideCase(
            task = "summarize the supplier meeting",
            kind = AgentWideCase.Kind.AMBIGUOUS,
            tools = ASK,
            ends = AgentWideCase.End.ASK,
            holds = listOf("2026-11-03", "2026-11-10")
        ),
        AgentWideCase(
            task = "tag it as urgent",
            kind = AgentWideCase.Kind.AMBIGUOUS,
            tools = listOf("ask_user"),
            ends = AgentWideCase.End.ASK
        ),
        AgentWideCase(
            task = "move the project to Archive",
            kind = AgentWideCase.Kind.AMBIGUOUS,
            tools = ASK,
            ends = AgentWideCase.End.ASK,
            holds = listOf("Greenhouse", "Irrigation")
        )
    )

    private val severalNotes = listOf(
        AgentWideCase(
            task = "compare the two supplier meetings in a note Supplier meetings compared",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "write_note", "propose"),
            ends = AgentWideCase.End.PROPOSE,
            holds = listOf("Supplier meetings compared")
        ),
        AgentWideCase(
            task = "which of my two budgets is larger, and by how much?",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = AgentWideCase.End.ANSWER,
            computed = listOf("58,400")
        ),
        AgentWideCase(
            task = "add a task to the Greenhouse project for each open point of the last supplier meeting",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = AgentWideCase.End.PROPOSE,
            holds = listOf("member list", "early termination charge", "training sessions")
        ),
        AgentWideCase(
            task = "does the fee we agreed in the last supplier meeting match the service agreement?",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("4,250")
        )
    )

    val all = documents + longNotes + ambiguous + severalNotes

    /** The tools of the design (`docs/agent-loop.md`) a task may name. */
    val TOOLS = setOf(
        "search", "outline", "read", "digest", "list", "links", "find", "tasks", "fetch", "sources",
        "calculate", "count", "date", "plan", "note", "write_note", "edit_note", "append", "set_property",
        "add_tag", "add_task", "link", "move", "rename", "create_folder", "bookmark", "trash", "clip",
        "make_cards", "answer", "propose", "ask_user"
    )
}
