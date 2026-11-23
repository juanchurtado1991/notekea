package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindYogaStudioChanges {
    val all = listOf(
        change(
            task = "tag every member who has an unpaid invoice with #unpaid",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "read", "add_tag", "propose"),
            holds = listOf("Grace Holloway", "Omar Haddad"),
            lacks = listOf("Lena Fraser", "Tom Becker")
        ),
        change(
            task = "add a task to the inversions workshop to chase the two people who haven't paid",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Inversions Workshop", "Ruby Santos", "Kai Lindgren")
        ),
        change(
            task = "set the retreat's status to confirmed",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            holds = listOf("Blue Mountains Retreat", "confirmed")
        ),
        change(
            task = "Nina just paid her retreat deposit today, update the bookings",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Nina", "Bookings")
        ),
        change(
            task = "make a new note in Retreat listing everyone still coming and their room type",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "write_note", "propose"),
            holds = listOf("Daniel Cho", "Amara Obi", "single")
        ),
        change(
            task = "log today's lunch express in november attendance, 11 people",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "append", "propose"),
            holds = listOf("Attendance November", "Lunch Express")
        ),
        change(
            task = "rename Workshop Ideas to Workshop Backlog",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Workshop Backlog")
        ),
        change(
            task = "move Suppliers into the Finance folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            holds = listOf("Suppliers", "Finance")
        ),
        change(
            task = "mark Grace Holloway's renewal invoice as paid in Renewals",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Grace Holloway", "Renewals")
        ),
        change(
            task = "write up a note for Jasper's sunday class idea from what's in my journal",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "write_note", "propose"),
            holds = listOf("Hatha Foundations", "08:30")
        ),
        change(
            task = "add a task to the pay log to pay Jasper what he's still owed",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "add_task", "propose"),
            holds = listOf("Pay Log November"),
            computed = listOf("160"),
            lacks = listOf("320")
        ),
        change(
            task = "add December's rent to the Rent note, same amount as November",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "append", "propose"),
            holds = listOf("Rent", "6,624")
        )
    )
}
