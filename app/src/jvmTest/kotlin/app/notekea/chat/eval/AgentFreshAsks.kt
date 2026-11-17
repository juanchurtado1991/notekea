package app.notekea.chat.eval

import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** The requests of [AgentFreshCases] that cannot be done without asking which note or person. */
internal object AgentFreshAsks {
    private val ASK = listOf("search", "ask_user")

    val all = listOf(
        AgentWideCase(
            task = "Add a task to call Lucía tomorrow",
            kind = Kind.AMBIGUOUS,
            tools = ASK,
            ends = End.ASK,
            holds = listOf("Benítez", "Ortega")
        ),
        AgentWideCase(
            task = "Put 'confirm Saturday' in Pedro's note",
            kind = Kind.AMBIGUOUS,
            tools = ASK,
            ends = End.ASK,
            holds = listOf("Ruiz", "Vidal")
        ),
        AgentWideCase(
            task = "Add 'something inspired by Kyoto' to my Ideas note",
            kind = Kind.AMBIGUOUS,
            tools = ASK,
            ends = End.ASK
        ),
        AgentWideCase(
            task = "Mark the invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = ASK,
            ends = End.ASK
        ),
        AgentWideCase(
            task = "Move the recipe to an Archive folder",
            kind = Kind.AMBIGUOUS,
            tools = ASK,
            ends = End.ASK
        ),
        AgentWideCase(
            task = "Change the appointment to 11:00",
            kind = Kind.AMBIGUOUS,
            tools = ASK,
            ends = End.ASK
        ),
        AgentWideCase(
            task = "Mark the pending order as shipped",
            kind = Kind.AMBIGUOUS,
            tools = ASK,
            ends = End.ASK,
            holds = listOf("1044", "1045")
        ),
        AgentWideCase(
            task = "Change the rate to 30 €",
            kind = Kind.AMBIGUOUS,
            tools = ASK,
            ends = End.ASK
        ),
        AgentWideCase(
            task = "Set the task's due date to Friday",
            kind = Kind.AMBIGUOUS,
            tools = ASK,
            ends = End.ASK
        ),
        AgentWideCase(
            task = "Update the stock of the A4 print to 10",
            kind = Kind.AMBIGUOUS,
            tools = ASK,
            ends = End.ASK,
            holds = listOf("Higuera", "Lisboa")
        )
    )
}
