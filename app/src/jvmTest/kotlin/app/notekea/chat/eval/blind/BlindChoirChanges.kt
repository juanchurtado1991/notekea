package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Choir vault, each clear from the words and the notes. */
internal object BlindChoirChanges {
    val all = listOf(
        change(
            task = "Kate Mills paid her subs tonight, tick her off",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Kate Mills")
        ),
        change(
            task = "the Calon Lân copies came today, mark that order received",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Calon Lân", "received")
        ),
        change(
            task = "paid the Penylan deposit this morning, update the bookings",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Penylan")
        ),
        change(
            task = "add a task to the plan for the 18th: hand out the new Calon Lân copies",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("2026-11-18", "Calon Lân")
        ),
        change(
            task = "set the spring concert to confirmed",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            holds = listOf("Spring Concert 2027", "confirmed")
        ),
        change(
            task = "tag the member note of everyone who still owes subs with #subs-due",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "list", "add_tag", "propose"),
            holds = listOf("Dafydd Evans", "Dafydd Price", "Joanna Webb", "subs-due"),
            lacks = listOf("Gareth Lewis", "Rhian Morgan")
        ),
        change(
            task = "make a note called Concert night contacts with the numbers for the church, " +
                "the dress rehearsal hall and Owain",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "write_note", "propose"),
            holds = listOf(
                "Concert night contacts",
                "029 2018 0644",
                "07700 900539",
                "07700 900184"
            )
        ),
        change(
            task = "move the 2026-11-04 rehearsal note into a new folder Rehearsals/Past",
            kind = Kind.DOCUMENT,
            tools = listOf("create_folder", "move", "propose"),
            holds = listOf("Past", "2026-11-04")
        ),
        change(
            task = "rename the term plan to Autumn term 2026",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Autumn term 2026")
        ),
        change(
            task = "Owain's fee for the winter concert is now £200, update it",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("200")
        ),
        change(
            task = "add tonight's attendance: 21 present, apologies from Kate Mills and Tom Ashby",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("2026-11-11", "Tom Ashby")
        ),
        change(
            task = "tag Rhian Morgan's note #soloist",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            holds = listOf("Rhian Morgan", "soloist")
        )
    )
}
