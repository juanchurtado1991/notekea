package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindPhotographerAsks {
    val all = listOf(
        ask(
            task = "add a task to my insurance note: send the renewal form",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Business", "Gear")
        ),
        ask(
            task = "change the mileage rate in my rates note to \$0.60",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Business", "Team")
        ),
        ask(
            task = "add a task to jordan's note to send the contract addendum",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Ellis", "Pike")
        ),
        ask(
            task = "change jordan's start time to 7pm on the schedule",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Ellis", "Pike")
        ),
        ask(
            task = "update the repair ETA to next friday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("70-200", "Body B")
        ),
        ask(
            task = "take the lens off my wishlist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("28-70", "135")
        ),
        ask(
            task = "push the gallery deadline back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Hollis", "Sandoval", "Westbrook")
        ),
        ask(
            task = "set the location for teresa's session to Grant Park",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("engagement", "bridal")
        ),
        // Traps: they look ambiguous, but the words settle them.
        answer(
            task = "how many days till my gear insurance renews?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "date", "answer"),
            computed = listOf("34 days")
        ),
        change(
            task = "add a task to Jordan Pike's note: send the gala shot list",
            kind = Kind.AMBIGUOUS,
            tools = listOf("add_task", "propose"),
            holds = listOf("Jordan Pike", "gala shot list"),
            lacks = listOf("Jordan Ellis")
        ),
        change(
            task = "update the mileage rate in Team/Rates to \$0.55",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Team", "0.55")
        ),
        change(
            task = "mark the flash repair as done",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Speedlight"),
            lacks = listOf("70-200")
        ),
        answer(
            task = "how many days until the sandoval gallery is due?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "date", "answer"),
            computed = listOf("24 days")
        ),
        change(
            task = "add 'send the timeline questionnaire' to both jordans' notes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("add_task", "add_task", "propose"),
            holds = listOf("Jordan Ellis", "Jordan Pike", "timeline questionnaire")
        )
    )
}
