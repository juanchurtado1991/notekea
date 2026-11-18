package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindDogTrainerAsks {
    val all = listOf(
        AgentWideCase(
            task = "add to Megan's note that she's moving in december",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Ashby", "Pruitt")
        ),
        AgentWideCase(
            task = "move Megan's next session to 16:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Ashby", "Pruitt")
        ),
        AgentWideCase(
            task = "push the vet appointment back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Juniper", "Pepper")
        ),
        AgentWideCase(
            task = "move Juniper's vet appointment to the afternoon, 15:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "bump insurance in my budget to $150",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "add buy dry-erase markers to my schedule note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("class")
        ),
        AgentWideCase(
            task = "mark Walt's invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "drop the blue merle puppy's price to $2600",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Orange")
        ),
        AgentWideCase(
            task = "rename the budget note to Budget 2027",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Business", "Personal")
        ),
        // Traps: they look ambiguous, but the words settle them.
        AgentWideCase(
            task = "when's Megan Pruitt's next session?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("10:30"),
            lacks = listOf("15:00")
        ),
        AgentWideCase(
            task = "did both Megans pay their last invoice?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Ashby", "Pruitt")
        ),
        AgentWideCase(
            task = "move Pepper's vet appointment to 2026-11-21, same time",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Pepper", "2026-11-21")
        ),
        AgentWideCase(
            task = "in my business budget raise advertising to $175",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Advertising", "175")
        ),
        AgentWideCase(
            task = "mark both of Walt's invoices as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Invoices", "042", "047")
        )
    )
}
