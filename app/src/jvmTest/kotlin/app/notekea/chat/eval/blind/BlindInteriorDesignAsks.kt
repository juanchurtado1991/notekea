package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests to ask back, and traps that only look ambiguous. */
internal object BlindInteriorDesignAsks {
    val all = listOf(
        ask(
            task = "add a line for a £600 rug to the budget",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Hampstead", "Islington", "Clapham")
        ),
        ask(
            task = "move my meeting with ravi to 15:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("10:30", "11:00", "10:00")
        ),
        ask(
            task = "add a task to chase Patel about the unpaid invoice",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Ravi", "Meera")
        ),
        ask(
            task = "move dan's clapham slot to 2026-11-27",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Whelan", "Okafor")
        ),
        ask(
            task = "change the due date of the fenwick order for hampstead to 2026-12-01",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Orbit", "Arc")
        ),
        ask(
            task = "mark the pending linden order as delivered",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Oat", "Fern", "Teal")
        ),
        ask(
            task = "set dan's day rate to £340",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Okafor", "Whelan")
        ),
        ask(
            task = "add 3 m of piping cord from linden to the orders note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Hampstead", "Islington", "Clapham")
        ),
        answer(
            task = "how much have I invoiced Ravi Patel this year?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("5930")
        ),
        answer(
            task = "when's dan whelan coming to the islington flat?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("wardrobe")
        ),
        change(
            task = "move both pending fenwick orders on hampstead to 2026-12-01",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Orbit", "wall lights", "2026-12-01")
        ),
        change(
            task = "add a task to the islington budget: review the contractor overspend",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Islington", "contractor overspend")
        ),
        change(
            task = "move my 2026-11-28 meeting with ravi to 15:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("15:00", "Hampstead")
        )
    )
}
