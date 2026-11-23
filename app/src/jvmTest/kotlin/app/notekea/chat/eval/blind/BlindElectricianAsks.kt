package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Blind Electrician requests that must be asked back, then traps whose words settle them. */
internal object BlindElectricianAsks {
    val all = listOf(
        ask(
            task = "add a $60 hardware store receipt from today to my expenses",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("van", "tool")
        ),
        ask(
            task = "set the expenses note's status to reviewed",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("van", "tool")
        ),
        ask(
            task = "add to Kelly's page that she's away until 1 December",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Nguyen", "Fraser")
        ),
        ask(
            task = "put Kelly's new mobile 0400 555 222 in her note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Nguyen", "Fraser")
        ),
        ask(
            task = "push Raj's visit back an hour",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("13", "19")
        ),
        ask(
            task = "mark the pending quote as accepted",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Okafor", "Morrison")
        ),
        ask(
            task = "move the van booking to the 27th",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("tyre", "aircon")
        ),
        ask(
            task = "add a task to send the tester off for calibration",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("insulation", "loop")
        ),
        change(
            task = "Kelly Fraser said yes to her pending quote, mark it accepted",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Q-2026-036", "accepted")
        ),
        change(
            task = "Raj's 19 Nov visit is now 9:00 instead of 7:30",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Raj Patel", "9:00")
        ),
        change(
            task = "Greg Holt finally paid his outstanding invoice, mark it paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("INV-1045", "paid")
        ),
        answer(
            task = "what's Kelly Nguyen's number?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("0412 555 381"),
            lacks = listOf("0438 555 912")
        ),
        answer(
            task = "how much have both Kellys paid me so far?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("1764")
        )
    )
}
