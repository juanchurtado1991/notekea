package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests the care home vault cannot settle, then traps whose words do settle them. */
internal object BlindCareHomeAsks {
    val all = listOf(
        ask(
            task = "push the family meeting back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Kaye", "Booth")
        ),
        ask(
            task = "add to Margaret's note that she's joined the choir",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Booth", "Kaye")
        ),
        ask(
            task = "add £18 for refreshments to the budget",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("activities", "outings")
        ),
        ask(
            task = "log 3 more hours for Tom this month",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Ashby", "Reyes")
        ),
        ask(
            task = "mark the pending order as delivered",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("PlayWell", "Greenleaf")
        ),
        ask(
            task = "move the music booking to 3pm",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Ukuleles", "Delia")
        ),
        ask(
            task = "set the minibus booking to confirmed",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("garden centre", "christmas market")
        ),
        ask(
            task = "rename the budget note to Budget 2026-27",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("activities", "outings")
        ),
        answer(
            task = "how much is left in the activities budget?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("2635")
        ),
        answer(
            task = "what do both Margarets like doing?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "read", "answer"),
            holds = listOf("knitting", "gardening")
        ),
        change(
            task = "mark the Brightcraft order that's still pending as delivered",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("SO-105", "delivered")
        ),
        change(
            task = "move Margaret Kaye's family meeting to 14:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Kaye", "14:00")
        ),
        change(
            task = "add a task to both Margarets' notes: follow up on the flu jab",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "add_task", "propose"),
            holds = listOf("Margaret Booth", "Margaret Kaye")
        )
    )
}
