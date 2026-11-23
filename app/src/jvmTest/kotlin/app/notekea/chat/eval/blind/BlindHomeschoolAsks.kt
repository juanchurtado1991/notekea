package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests the Blind Homeschool vault cannot settle, and traps whose words do settle them. */
internal object BlindHomeschoolAsks {
    val all = listOf(
        ask(
            task = "add to Hannah's note that she's away the first week of December",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Ngata", "Sorensen")
        ),
        ask(
            task = "add 'return library books Friday' to the weekly plan",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Isla", "Finn", "Ruby")
        ),
        ask(
            task = "push the co-op meeting back an hour",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user")
        ),
        ask(
            task = "set reviewed: yes on my budget note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Curriculum")
        ),
        ask(
            task = "mark Hannah's October invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Ngata", "Sorensen")
        ),
        ask(
            task = "push the trip back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("list", "read", "ask_user"),
            holds = listOf("Observatory", "Tui")
        ),
        ask(
            task = "set the progress note's status to reviewed",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Isla", "Finn")
        ),
        ask(
            task = "move the dentist appointment to 2 December",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Isla", "Ruby")
        ),
        change(
            task = "mark Hannah Sorensen's October invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Hannah Sorensen", "paid")
        ),
        change(
            task = "add 'return library books Friday' to Ruby's weekly plan",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Ruby", "return library books")
        ),
        answer(
            task = "what time is Isla's dentist appointment?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("15:45")
        ),
        answer(
            task = "how much is left in the curriculum budget?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("1227")
        ),
        change(
            task = "move the 25 Nov co-op meeting to 11:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("11:00", "Meetings 2026")
        )
    )
}
