package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindYogaStudioAsks {
    private val asks = listOf(
        AgentWideCase(
            task = "set Mia's rate to \$100 a class",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Chen", "Okafor")
        ),
        AgentWideCase(
            task = "add to Mia's note that she's doing a teacher training in Bali in February",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Chen", "Okafor")
        ),
        AgentWideCase(
            task = "push the maintenance appointment back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("plumb", "electric", "floor")
        ),
        AgentWideCase(
            task = "cancel the pending supplier order",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("bolster", "block")
        ),
        AgentWideCase(
            task = "set the budget status to approved",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Retreat")
        ),
        AgentWideCase(
            task = "add 'buy batteries' to my checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Retreat")
        ),
        AgentWideCase(
            task = "switch the single room booking to twin share",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Daniel", "Grace")
        ),
        AgentWideCase(
            task = "mark the unpaid renewal invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Grace", "Omar")
        ),
        AgentWideCase(
            task = "add a task to the workshop note to book a photographer",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Inversions", "Breathwork")
        )
    )

    private val traps = listOf(
        AgentWideCase(
            task = "how much do I still owe Mia Okafor for November?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("205"),
            lacks = listOf("490")
        ),
        AgentWideCase(
            task = "move the electrician to 2026-11-20, same time",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Maintenance", "2026-11-20", "14:30")
        ),
        AgentWideCase(
            task = "set the insurance line in my Finance budget to \$3400",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Finance", "3400")
        ),
        AgentWideCase(
            task = "what's catering in the retreat budget?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("3,360")
        ),
        AgentWideCase(
            task = "add a task to both Mias' notes: send the 2027 contract",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Mia Chen", "Mia Okafor")
        )
    )

    val all = asks + traps
}
