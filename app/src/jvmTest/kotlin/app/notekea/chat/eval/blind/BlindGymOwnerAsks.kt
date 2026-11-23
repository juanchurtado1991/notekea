package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests that must be asked back, and traps whose words settle them. */
internal object BlindGymOwnerAsks {
    val all = listOf(
        ask(
            task = "push the technician visit back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("rower", "rig")
        ),
        ask(
            task = "cancel priya's PT session",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("snatch", "strength test")
        ),
        ask(
            task = "set ryan's hourly rate to 36",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Okafor", "Delgado")
        ),
        ask(
            task = "ryan is covering jess's saturday 8am class this week, put that on his page",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Okafor", "Delgado")
        ),
        ask(
            task = "add a task to the budget note: double-check the marketing line",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user")
        ),
        ask(
            task = "link jess's note from the schedule",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user")
        ),
        ask(
            task = "mark marcus's invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user")
        ),
        ask(
            task = "move the saturday class to 11:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Community", "Olympic")
        ),
        answer(
            task = "how many days until ryan delgado's cpr card expires?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "date", "answer"),
            computed = listOf("21 days")
        ),
        answer(
            task = "how many hours did both ryans coach in october combined?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("110")
        ),
        change(
            task = "in the event budget change t-shirts to 1240",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("T-shirts", "1240")
        ),
        change(
            task = "move the rig bolts inspection to 2026-11-20 same time",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("rig", "2026-11-20", "13:30")
        ),
        change(
            task = "mark both of marcus's unpaid invoices as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("INV-1042", "INV-1047")
        )
    )
}
