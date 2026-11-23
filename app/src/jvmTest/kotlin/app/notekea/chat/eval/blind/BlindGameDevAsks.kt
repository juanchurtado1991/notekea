package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindGameDevAsks {
    val all = listOf(
        // Asks: the words name one thing and the vault has several that fit.
        ask(
            task = "mark sam's last invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "ask_user"),
            holds = listOf("Okafor", "Delisle")
        ),
        ask(
            task = "add a task to send sam the new palette",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Okafor", "Delisle")
        ),
        ask(
            task = "push sam's contract end date to 2027-03-31",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Okafor", "Delisle")
        ),
        ask(
            task = "add the tag #finance to my budget note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("budget")
        ),
        ask(
            task = "rename my ideas note to Brainstorm",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("marketing")
        ),
        ask(
            task = "move the appointment to friday at 2pm",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Brightkite", "bank")
        ),
        ask(
            task = "cancel the pending order",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("monitor", "capture card", "pen display")
        ),
        ask(
            task = "tag the playtest note #archived",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("playtest", "14")
        ),
        ask(
            task = "mark the critical bug as fixed",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("save", "boss"),
            lacks = listOf("B-06")
        ),
        // Traps: they look ambiguous, but the words settle them.
        answer(
            task = "how much do i still owe the sam who does the character sprites?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            holds = listOf("Okafor"),
            computed = listOf("1254"),
            lacks = listOf("1904")
        ),
        change(
            task = "mark sam delisle's lighthouse invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Sam Delisle Contract", "SD-04")
        ),
        change(
            task = "add the tag #finance to the tidewell budget",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "add_tag", "propose"),
            holds = listOf("Game/Budget", "finance"),
            lacks = listOf("Business/Budget")
        ),
        change(
            task = "add 'devlog video every month' to the marketing ideas note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "append", "propose"),
            holds = listOf("Marketing/Ideas", "devlog video every month"),
            lacks = listOf("Game/Ideas")
        ),
        answer(
            task = "how much are all my pending hardware orders together?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("1208.49"),
            lacks = listOf("1298.47")
        )
    )
}
