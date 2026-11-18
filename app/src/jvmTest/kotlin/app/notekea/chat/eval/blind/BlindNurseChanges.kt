package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindNurseChanges {
    val all = listOf(
        AgentWideCase(
            task = "add 'return the flat keys' to my move checklist",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Move Checklist", "return the flat keys")
        ),
        AgentWideCase(
            task = "I've gone with Swiftvan, set chosen: Swiftvan on the removal quotes note",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Removal Quotes", "Swiftvan")
        ),
        AgentWideCase(
            task = "tag my critical care module note #revalidation",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Critical Care Module", "revalidation")
        ),
        AgentWideCase(
            task = "log today in my overtime log: stayed 1.5 h late after handover, " +
                "26.10 an hour, not claimed yet",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Overtime Log", "1.5", "not claimed yet")
        ),
        AgentWideCase(
            task = "make a new note called Move Week with the completion date, moving day " +
                "and key pick-up time from my house move note",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Move Week", "13:00")
        ),
        AgentWideCase(
            task = "AK has stepped down, move her handover into a Handover/Archive folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "create_folder", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Bed 2 AK", "Archive")
        ),
        AgentWideCase(
            task = "rename my study days note to Study Days 2026-27",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Study Days 2026-27")
        ),
        AgentWideCase(
            task = "change the GP appointment to 17:10",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Appointments", "17:10")
        ),
        AgentWideCase(
            task = "tag every handover note of a ventilated patient #ventilated",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "read", "read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Bed 3 JM", "Bed 9 LP"),
            lacks = listOf("Bed 5 RT", "Bed 7 JM")
        ),
        AgentWideCase(
            task = "paid all the unpaid bills this morning, mark them paid",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Bills", "Water", "Car insurance")
        ),
        AgentWideCase(
            task = "RT went up to the ward today, set his handover status to stepped down",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Bed 5 RT", "stepped down")
        ),
        AgentWideCase(
            task = "write a note called Cert Renewals listing the certificates that expire " +
                "in 2027 with their expiry dates",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Cert Renewals", "Manual handling", "Safeguarding", "IV therapy"),
            lacks = listOf("Fire safety")
        )
    )
}
