package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Homeschool vault, each clear from the words and the notes. */
internal object BlindHomeschoolChanges {
    val all = listOf(
        AgentWideCase(
            task = "add a task to the Tui Bush note: ring the sanctuary about the adult price",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Tui Bush", "ring the sanctuary")
        ),
        AgentWideCase(
            task = "set Isla's weekly plan status to done",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Isla", "done")
        ),
        AgentWideCase(
            task = "tag the trips we've already been on with #done-trip",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Glowworm", "Harbourside", "done-trip")
        ),
        AgentWideCase(
            task = "start today's journal: co-op cancelled, rained all morning",
            kind = Kind.DOCUMENT,
            tools = listOf("list", "date", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("rained all morning"),
            computed = listOf("2026-11-11")
        ),
        AgentWideCase(
            task = "Finn finished Moa Bones, mark it in the book list",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Moa Bones", "finished")
        ),
        AgentWideCase(
            task = "rename the Science fair note to Science fair 2026",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Science fair 2026")
        ),
        AgentWideCase(
            task = "change Ruby's dentist appt to 10:45",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Ruby", "10:45")
        ),
        AgentWideCase(
            task = "tag the progress notes that are still drafts with #to-finish",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Isla", "Finn", "to-finish")
        ),
        AgentWideCase(
            task = "make a new note Trip costs 2026 with each trip we've been on and what it " +
                "cost after refunds",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "calculate", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Trip costs 2026", "Glowworm", "Harbourside", "220"),
            computed = listOf("131")
        ),
        AgentWideCase(
            task = "add \$35 of stationery to the curriculum budget, dated today",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("35", "stationery")
        ),
        AgentWideCase(
            task = "put Hannah Sorensen's piano recital in my appointments",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("recital", "14:00")
        ),
        AgentWideCase(
            task = "update the observatory note with the new start time",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("20:30")
        )
    )
}
