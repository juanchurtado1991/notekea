package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindYogaStudioAsks {
    private val asks = listOf(
        ask(
            task = "set Mia's rate to \$100 a class",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Chen", "Okafor")
        ),
        ask(
            task = "add to Mia's note that she's doing a teacher training in Bali in February",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Chen", "Okafor")
        ),
        ask(
            task = "push the maintenance appointment back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("plumb", "electric", "floor")
        ),
        ask(
            task = "cancel the pending supplier order",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("bolster", "block")
        ),
        ask(
            task = "set the budget status to approved",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Retreat")
        ),
        ask(
            task = "add 'buy batteries' to my checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Retreat")
        ),
        ask(
            task = "switch the single room booking to twin share",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Daniel", "Grace")
        ),
        ask(
            task = "mark the unpaid renewal invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Grace", "Omar")
        ),
        ask(
            task = "add a task to the workshop note to book a photographer",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Inversions", "Breathwork")
        )
    )

    private val traps = listOf(
        answer(
            task = "how much do I still owe Mia Okafor for November?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("205"),
            lacks = listOf("490")
        ),
        change(
            task = "move the electrician to 2026-11-20, same time",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Maintenance", "2026-11-20", "14:30")
        ),
        change(
            task = "set the insurance line in my Finance budget to \$3400",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Finance", "3400")
        ),
        answer(
            task = "what's catering in the retreat budget?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("3,360")
        ),
        change(
            task = "add a task to both Mias' notes: send the 2027 contract",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "add_task", "propose"),
            holds = listOf("Mia Chen", "Mia Okafor")
        )
    )

    val all = asks + traps
}
