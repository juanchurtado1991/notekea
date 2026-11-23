package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Film Student notes. */
internal object BlindFilmStudentChanges {
    val all = listOf(
        change(
            task = "add a task to the wrap checklist: return the slider to Lumen",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            holds = listOf("Wrap checklist", "slider")
        ),
        change(
            task = "draft 3 is final, set its status to locked",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Draft 3", "locked")
        ),
        change(
            task = "tag every location whose permit is still pending with #permit-pending",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "add_tag", "add_tag", "propose"),
            holds = listOf("Westport Pier", "Laundromat on Oak", "permit-pending")
        ),
        change(
            task = "make me a Crew contacts note with every crew member's phone number",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf(
                "search",
                "read",
                "read",
                "read",
                "read",
                "read",
                "write_note",
                "propose"
            ),
            holds = listOf("Crew contacts", "323-555-0148", "213-555-0192", "818-555-0137"),
            lacks = listOf("310-555-0175")
        ),
        change(
            task = "rename Draft 2 notes to Draft 2 feedback",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Draft 2 feedback")
        ),
        change(
            task = "tag every crew member who hasn't signed their deal memo with #needs-memo",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "add_tag", "propose"),
            holds = listOf("Taylor Kim", "Jordan Pierce", "needs-memo")
        ),
        change(
            task = "new note in Festivals called Deadlines: every festival I'm entering " +
                "with its deadline, soonest first",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "write_note", "propose"),
            holds = listOf(
                "Deadlines",
                "Lantern Bay",
                "Ridgeview",
                "Night Owl",
                "Coastline"
            ),
            lacks = listOf("Sundown")
        ),
        change(
            task = "add to my Nov 10 journal: generator delivery confirmed with Valley Power",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "append", "propose"),
            holds = listOf("2026-11-10", "generator delivery confirmed")
        ),
        change(
            task = "bookmark the grant application",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "bookmark", "propose"),
            holds = listOf("Grant application")
        ),
        change(
            task = "move the scouting log into the Production folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            holds = listOf("Scouting log", "Production")
        ),
        change(
            task = "add a task to the wrap checklist to pay each rental that's still unpaid",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "add_task", "add_task", "add_task", "propose"),
            holds = listOf("haze", "generator", "monitor")
        ),
        change(
            task = "change shot 4B's lens to 50mm",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("4B", "50mm")
        )
    )
}
