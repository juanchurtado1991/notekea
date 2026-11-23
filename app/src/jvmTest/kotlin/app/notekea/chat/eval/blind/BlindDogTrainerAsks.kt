package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindDogTrainerAsks {
    val all = listOf(
        ask(
            task = "add to Megan's note that she's moving in december",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Ashby", "Pruitt")
        ),
        ask(
            task = "move Megan's next session to 16:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Ashby", "Pruitt")
        ),
        ask(
            task = "push the vet appointment back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Juniper", "Pepper")
        ),
        ask(
            task = "move Juniper's vet appointment to the afternoon, 15:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user")
        ),
        ask(
            task = "bump insurance in my budget to $150",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user")
        ),
        ask(
            task = "add buy dry-erase markers to my schedule note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("class")
        ),
        ask(
            task = "mark Walt's invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user")
        ),
        ask(
            task = "drop the blue merle puppy's price to $2600",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Orange")
        ),
        ask(
            task = "rename the budget note to Budget 2027",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Business", "Personal")
        ),
        // Traps: they look ambiguous, but the words settle them.
        answer(
            task = "when's Megan Pruitt's next session?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "answer"),
            holds = listOf("10:30"),
            lacks = listOf("15:00")
        ),
        answer(
            task = "did both Megans pay their last invoice?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "answer"),
            holds = listOf("Ashby", "Pruitt")
        ),
        change(
            task = "move Pepper's vet appointment to 2026-11-21, same time",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Pepper", "2026-11-21")
        ),
        change(
            task = "in my business budget raise advertising to $175",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Advertising", "175")
        ),
        change(
            task = "mark both of Walt's invoices as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Invoices", "042", "047")
        )
    )
}
