package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the care home vault, each one clear from the words and the notes. */
internal object BlindCareHomeChanges {
    val all = listOf(
        AgentWideCase(
            task = "add a task to the christmas market trip note: confirm the wheelchair space " +
                "with Moorlink",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Christmas Market Trip", "wheelchair")
        ),
        AgentWideCase(
            task = "set Stanley's status to settled",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Stanley Moss", "settled")
        ),
        AgentWideCase(
            task = "tag Ruth with volunteer-lead",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Ruth Okafor", "volunteer-lead")
        ),
        AgentWideCase(
            task = "add to yesterday's log that Stanley joined craft club for the first time",
            kind = Kind.DOCUMENT,
            tools = listOf("date", "read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-11-10", "Stanley")
        ),
        AgentWideCase(
            task = "make a new note listing everyone going on the christmas market trip " +
                "with their room numbers",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "search", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Arthur Lyle", "Stanley Moss", "Margaret Kaye")
        ),
        AgentWideCase(
            task = "move volunteer induction into the volunteers folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Volunteer Induction", "Volunteers")
        ),
        AgentWideCase(
            task = "rename the canal boat trip note to Canal Boat Trip - Cancelled",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Canal Boat Trip - Cancelled")
        ),
        AgentWideCase(
            task = "move Delia Frost's booking to 15:00",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Delia Frost", "15:00")
        ),
        AgentWideCase(
            task = "tag every resident who has a family meeting coming up with family-review",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Margaret Kaye", "Stanley Moss", "Margaret Booth"),
            lacks = listOf("Arthur Lyle")
        ),
        AgentWideCase(
            task = "add a task to renew the DBS on every volunteer whose DBS runs out " +
                "before march 2027",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Tom Ashby", "Ruth Okafor"),
            lacks = listOf("Gwen Hollis")
        ),
        AgentWideCase(
            task = "add an order to supplies orders: SO-108, 2026-11-11, PlayWell Games, " +
                "christmas crackers, £27.60, pending",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("SO-108", "27.60")
        ),
        AgentWideCase(
            task = "give seaside day a rating of 5",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Seaside Day", "rating")
        )
    )
}
