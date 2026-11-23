package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindFounderAsks {
    val all = listOf(
        ask(
            task = "move my meeting with rachel to next thursday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Kim", "Duarte")
        ),
        ask(
            task = "set rachel's status to committed",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Kim", "Duarte")
        ),
        ask(
            task = "add a task to my pipeline note: chase everyone who went quiet",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user")
        ),
        ask(
            task = "tick off the first item on my checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user")
        ),
        ask(
            task = "push the onsite to friday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Kai", "Nadia")
        ),
        ask(
            task = "move the pecan street call to monday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user")
        ),
        ask(
            task = "append 'minutes sent to the board' to the board meeting note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user")
        ),
        ask(
            task = "mark the pecan street invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user")
        ),
        // Traps: they look ambiguous, but the words settle them.
        answer(
            task = "when's my next call with rachel kim?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "answer"),
            holds = listOf("15:30"),
            lacks = listOf("10:00")
        ),
        answer(
            task = "how much are the two rachels putting in together?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("750")
        ),
        change(
            task = "add a task to my fundraising checklist: send the data room link to rachel kim",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "add_task", "propose"),
            holds = listOf("data room link")
        ),
        change(
            task = "mark both unpaid pecan street invoices as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("INV-1059", "INV-1074")
        )
    )
}
