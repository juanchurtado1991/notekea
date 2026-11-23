package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests that must be asked back, and traps whose words settle them, in Blind Housing Coop. */
internal object BlindHousingCoopAsks {
    val all = listOf(
        ask(
            task = "add a line Contingency 5000 to the budget",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("renovation")
        ),
        ask(
            task = "add a task to give Anna back her ladder",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Berger", "Novak")
        ),
        ask(
            task = "Anna has a new email, anna.k@posteo.at, update her note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Berger", "Novak")
        ),
        ask(
            task = "move the elevator appointment to 2026-11-20, same time",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("inspection", "cable")
        ),
        ask(
            task = "mark the open contractor invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Dachwerk", "Kaminski")
        ),
        ask(
            task = "in the minutes change the stairwell windows vote to 13 in favour",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "ask_user")
        ),
        ask(
            task = "the leak is fixed, close the request",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("sink", "attic")
        ),
        ask(
            task = "set the insurance policy's renewal to 2027-12-31",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("building", "liability")
        ),
        ask(
            task = "push the garden committee meeting to 19:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("planting", "tool shed")
        ),
        answer(
            task = "how much is building insurance in next year's operating budget?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("6840")
        ),
        answer(
            task = "which units are both Annas in?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Berger", "Novak", "3B", "5A")
        ),
        change(
            task = "the board approved Anna Novak's balcony screen, tag her note #approved",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_tag", "propose"),
            holds = listOf("Anna Novak", "approved")
        ),
        change(
            task = "the Lift-Tec invoice is paid, mark it",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Contractor invoices", "Lift-Tec")
        ),
        change(
            task = "the plumber fixed the leak in 1A, close that request",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Requests log", "R-31")
        )
    )
}
