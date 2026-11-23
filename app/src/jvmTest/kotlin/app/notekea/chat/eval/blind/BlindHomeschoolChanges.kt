package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Homeschool vault, each clear from the words and the notes. */
internal object BlindHomeschoolChanges {
    val all = listOf(
        change(
            task = "add a task to the Tui Bush note: ring the sanctuary about the adult price",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Tui Bush", "ring the sanctuary")
        ),
        change(
            task = "set Isla's weekly plan status to done",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Isla", "done")
        ),
        change(
            task = "tag the trips we've already been on with #done-trip",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "add_tag", "add_tag", "propose"),
            holds = listOf("Glowworm", "Harbourside", "done-trip")
        ),
        change(
            task = "start today's journal: co-op cancelled, rained all morning",
            kind = Kind.DOCUMENT,
            tools = listOf("list", "date", "write_note", "propose"),
            holds = listOf("rained all morning"),
            computed = listOf("2026-11-11")
        ),
        change(
            task = "Finn finished Moa Bones, mark it in the book list",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Moa Bones", "finished")
        ),
        change(
            task = "rename the Science fair note to Science fair 2026",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Science fair 2026")
        ),
        change(
            task = "change Ruby's dentist appt to 10:45",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Ruby", "10:45")
        ),
        change(
            task = "tag the progress notes that are still drafts with #to-finish",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "add_tag", "add_tag", "propose"),
            holds = listOf("Isla", "Finn", "to-finish")
        ),
        change(
            task = "make a new note Trip costs 2026 with each trip we've been on and what it " +
                "cost after refunds",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "calculate", "write_note", "propose"),
            holds = listOf("Trip costs 2026", "Glowworm", "Harbourside", "220"),
            computed = listOf("131")
        ),
        change(
            task = "add \$35 of stationery to the curriculum budget, dated today",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "edit_note", "propose"),
            holds = listOf("35", "stationery")
        ),
        change(
            task = "put Hannah Sorensen's piano recital in my appointments",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "edit_note", "propose"),
            holds = listOf("recital", "14:00")
        ),
        change(
            task = "update the observatory note with the new start time",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "edit_note", "propose"),
            holds = listOf("20:30")
        )
    )
}
