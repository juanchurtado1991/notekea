package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindNurseChanges {
    val all = listOf(
        change(
            task = "add 'return the flat keys' to my move checklist",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Move Checklist", "return the flat keys")
        ),
        change(
            task = "I've gone with Swiftvan, set chosen: Swiftvan on the removal quotes note",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Removal Quotes", "Swiftvan")
        ),
        change(
            task = "tag my critical care module note #revalidation",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            holds = listOf("Critical Care Module", "revalidation")
        ),
        change(
            task = "log today in my overtime log: stayed 1.5 h late after handover, " +
                "26.10 an hour, not claimed yet",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "append", "propose"),
            holds = listOf("Overtime Log", "1.5", "not claimed yet")
        ),
        change(
            task = "make a new note called Move Week with the completion date, moving day " +
                "and key pick-up time from my house move note",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "write_note", "propose"),
            holds = listOf("Move Week", "13:00")
        ),
        change(
            task = "AK has stepped down, move her handover into a Handover/Archive folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "create_folder", "move", "propose"),
            holds = listOf("Bed 2 AK", "Archive")
        ),
        change(
            task = "rename my study days note to Study Days 2026-27",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Study Days 2026-27")
        ),
        change(
            task = "change the GP appointment to 17:10",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Appointments", "17:10")
        ),
        change(
            task = "tag every handover note of a ventilated patient #ventilated",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "read", "read", "add_tag", "propose"),
            holds = listOf("Bed 3 JM", "Bed 9 LP"),
            lacks = listOf("Bed 5 RT", "Bed 7 JM")
        ),
        change(
            task = "paid all the unpaid bills this morning, mark them paid",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Bills", "Water", "Car insurance")
        ),
        change(
            task = "RT went up to the ward today, set his handover status to stepped down",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Bed 5 RT", "stepped down")
        ),
        change(
            task = "write a note called Cert Renewals listing the certificates that expire " +
                "in 2027 with their expiry dates",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "write_note", "propose"),
            holds = listOf("Cert Renewals", "Manual handling", "Safeguarding", "IV therapy"),
            lacks = listOf("Fire safety")
        )
    )
}
