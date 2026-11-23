package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindHouseholdAsks {
    val all = listOf(
        // Two notes named Budget: Money/Budget.md and Renovation/Budget.md.
        ask(
            task = "what's left in the budget?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Renovation")
        ),
        ask(
            task = "add a misc line of 200 to the budget",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Renovation")
        ),
        ask(
            task = "rename the budget note to Budget 2026",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Renovation")
        ),
        // Two people named Priya: Priya Nandakumar (accountant), Priya Shah (broker).
        ask(
            task = "add a task to call priya on monday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Nandakumar", "Shah")
        ),
        ask(
            task = "update priya's phone number to 416-555-0199",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Nandakumar", "Shah")
        ),
        // Three site visits with Daniel Okafor.
        ask(
            task = "move my appointment with daniel to 11:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("measurements", "cabinet", "electrical")
        ),
        ask(
            task = "cancel the appointment with daniel",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("measurements", "cabinet", "electrical")
        ),
        // Four appliance orders on three delivery dates.
        ask(
            task = "one of the appliance deliveries has to move a week later, change it",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Fridge", "Dishwasher", "Range")
        ),
        // Two life policies in one note.
        ask(
            task = "change the coverage on the life insurance policy to 800,000",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Nora", "Theo")
        ),
        // Traps: the words settle which one.
        answer(
            task = "in the money folder budget, what are groceries planned at?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("900")
        ),
        answer(
            task = "what's priya shah's email?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("pshah@harbourline.example"),
            lacks = listOf("priya@nandakumar-cpa.example")
        ),
        answer(
            task = "what does priya nandakumar charge us?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("425")
        ),
        change(
            task = "change the cabinet sample review with daniel to 17:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Daniel Okafor", "17:00")
        ),
        change(
            task = "add a task to both budget notes: review on Dec 1",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "add_task", "propose"),
            holds = listOf("Money", "Renovation", "review")
        )
    )
}
