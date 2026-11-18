package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindFreelanceWriterChanges {
    val all = listOf(
        AgentWideCase(
            task = "add a todo to the rent pressure zones draft: get Grainne to confirm her quotes",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Rent pressure zones", "Grainne")
        ),
        AgentWideCase(
            task = "set the canal swimmers draft status to ready",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Canal swimmers", "ready")
        ),
        AgentWideCase(
            task = "Tom just said no to the seaweed pitch, mark it rejected",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Seaweed", "rejected")
        ),
        AgentWideCase(
            task = "log an expense for today: printer ink €18, equipment",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Expenses 2026", "printer ink", "18")
        ),
        AgentWideCase(
            task = "make a note called Money owed listing every unpaid invoice with its amount and" +
                " the total",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf(
                "Money owed",
                "INV-2026-034",
                "INV-2026-038",
                "INV-2026-041",
                "INV-2026-042"
            ),
            computed = listOf("2520"),
            lacks = listOf("INV-2026-036")
        ),
        AgentWideCase(
            task = "write a note called Greenmoor sources listing each Greenmoor source and whether" +
                " they're on or off the record",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("find", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Greenmoor sources", "Declan Moran", "Peter Cullen", "Aoife Brady"),
            lacks = listOf("Declan Fitzgerald", "Grainne Walsh")
        ),
        AgentWideCase(
            task = "move the Declan Moran transcript into Sources",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Transcript", "Declan Moran", "Sources")
        ),
        AgentWideCase(
            task = "rename FOI requests to FOI log",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("FOI log")
        ),
        AgentWideCase(
            task = "tag every Greenmoor source who's off the record with #protect",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("find", "read", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Declan Moran", "Peter Cullen", "protect"),
            lacks = listOf("Declan Fitzgerald", "Grainne Walsh")
        ),
        AgentWideCase(
            task = "Ravi says the data centres invoice was paid yesterday, update it",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("INV-2026-038", "paid")
        ),
        AgentWideCase(
            task = "link my canal swimmers draft to Siobhan's note",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "link", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Canal swimmers", "Siobhan Kerr")
        ),
        AgentWideCase(
            task = "add a task to Ciara's note to re-pitch the night bus piece on Monday",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "date", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Ciara Lynch", "night bus")
        )
    )
}
