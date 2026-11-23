package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests the Blind Choir vault cannot settle, then traps whose words do settle them. */
internal object BlindChoirAsks {
    val all = listOf(
        ask(
            task = "set the budget to approved",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("winter")
        ),
        ask(
            task = "Rhian wants to swap to alto, change her part",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Morgan", "Hughes")
        ),
        ask(
            task = "push the venue booking back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Aidan", "Ely", "Penylan")
        ),
        ask(
            task = "add a task to chase the pending sheet music order",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Calon Lân", "Suo Gân", "folders")
        ),
        ask(
            task = "Dafydd paid his subs, tick him off",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Evans", "Price")
        ),
        ask(
            task = "add Sian Rowlands 07700 900845 to my contacts",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("emergency")
        ),
        ask(
            task = "move the sectional to Friday, same time",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("soprano", "tenor", "bass")
        ),
        ask(
            task = "tick off the raffle prize, I picked it up this morning",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("voucher", "wine")
        ),
        ask(
            task = "add lip trills to the warm-ups note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("library", "rotation")
        ),
        change(
            task = "Rhian's back from leave next week, set her status to active",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "set_property", "propose"),
            holds = listOf("Rhian Hughes", "active"),
            lacks = listOf("Rhian Morgan")
        ),
        answer(
            task = "who's Rhian Hughes's emergency contact?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Gethin Hughes"),
            lacks = listOf("Iwan Morgan")
        ),
        change(
            task = "set the winter concert budget to approved",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "set_property", "propose"),
            holds = listOf("budget", "approved")
        ),
        change(
            task = "both Dafydds paid their subs tonight, tick them off",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Dafydd Evans", "Dafydd Price")
        ),
        change(
            task = "the bass sectional's moved to Room 3, update it",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("bass", "Room 3")
        )
    )
}
