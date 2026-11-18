package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Film Student notes. */
internal object BlindFilmStudentChanges {
    val all = listOf(
        AgentWideCase(
            task = "add a task to the wrap checklist: return the slider to Lumen",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Wrap checklist", "slider")
        ),
        AgentWideCase(
            task = "draft 3 is final, set its status to locked",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Draft 3", "locked")
        ),
        AgentWideCase(
            task = "tag every location whose permit is still pending with #permit-pending",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Westport Pier", "Laundromat on Oak", "permit-pending")
        ),
        AgentWideCase(
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
            ends = End.PROPOSE,
            holds = listOf("Crew contacts", "323-555-0148", "213-555-0192", "818-555-0137"),
            lacks = listOf("310-555-0175")
        ),
        AgentWideCase(
            task = "rename Draft 2 notes to Draft 2 feedback",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Draft 2 feedback")
        ),
        AgentWideCase(
            task = "tag every crew member who hasn't signed their deal memo with #needs-memo",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Taylor Kim", "Jordan Pierce", "needs-memo")
        ),
        AgentWideCase(
            task = "new note in Festivals called Deadlines: every festival I'm entering " +
                "with its deadline, soonest first",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf(
                "Deadlines",
                "Lantern Bay",
                "Ridgeview",
                "Night Owl",
                "Coastline"
            ),
            lacks = listOf("Sundown")
        ),
        AgentWideCase(
            task = "add to my Nov 10 journal: generator delivery confirmed with Valley Power",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-11-10", "generator delivery confirmed")
        ),
        AgentWideCase(
            task = "bookmark the grant application",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "bookmark", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Grant application")
        ),
        AgentWideCase(
            task = "move the scouting log into the Production folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Scouting log", "Production")
        ),
        AgentWideCase(
            task = "add a task to the wrap checklist to pay each rental that's still unpaid",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "add_task", "add_task", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("haze", "generator", "monitor")
        ),
        AgentWideCase(
            task = "change shot 4B's lens to 50mm",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("4B", "50mm")
        )
    )
}
