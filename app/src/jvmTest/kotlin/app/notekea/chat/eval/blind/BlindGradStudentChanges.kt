package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Grad Student vault, each clear from the words and the notes. */
internal object BlindGradStudentChanges {
    val all = listOf(
        change(
            task = "add a task to my chapter 2 draft: redo figure 2.3 with error bars",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Chapter 2", "figure 2.3")
        ),
        change(
            task = "set the status of chapter 1 to review",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Chapter 1", "review")
        ),
        change(
            task = "tag the Mbeki paper #fieldwork",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            holds = listOf("Mbeki", "fieldwork")
        ),
        change(
            task = "log today's spawning: 2 females 2 males, fertilisation 85%",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "append", "propose"),
            holds = listOf("Spawning log", "85"),
            computed = listOf("2026-11-11")
        ),
        change(
            task = "make a note called LT50 comparison with my LT50 and both of Kowalski's values",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "write_note", "propose"),
            holds = listOf("LT50 comparison", "24.3", "25.8", "27.1")
        ),
        change(
            task = "rename Packing list to Fieldwork packing list",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Fieldwork packing list")
        ),
        change(
            task = "move the Tanaka methods paper into the Lab folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            holds = listOf("Tanaka", "Lab")
        ),
        change(
            task = "change the 2026-11-18 confocal booking to 15:00–18:00",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("2026-11-18", "15:00")
        ),
        change(
            task = "mark the formalin as paid in the lab budget",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Formalin", "paid")
        ),
        change(
            task = "tag every paper I rated 4 or higher with #core",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "add_tag", "add_tag", "propose"),
            holds = listOf("Kowalski", "Arden", "Tanaka", "core"),
            lacks = listOf("Mbeki", "Fischer")
        ),
        change(
            task = "add #followup to every meeting note that still has an open action item",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "tasks", "add_tag", "add_tag", "add_tag", "propose"),
            holds = listOf("2026-10-28", "2026-11-04", "2026-11-10", "followup"),
            lacks = listOf("2026-10-14")
        ),
        change(
            task = "write a Fieldwork contacts note: station manager's phone and Priya's email",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "write_note", "propose"),
            holds = listOf("Fieldwork contacts", "555 0142", "p.nair@westmere.example")
        ),
        change(
            task = "finished the Fischer paper, set it to read with rating 3",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "set_property", "propose"),
            holds = listOf("Fischer", "rating", "3")
        )
    )
}
