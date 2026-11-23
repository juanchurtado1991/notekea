package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Electrician vault, each one settled by the words and the notes. */
internal object BlindElectricianChanges {
    val all = listOf(
        change(
            task = "add a task to the stairwell lighting job: swap the faulty sensor on level 3",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Strata stairwell lighting", "level 3")
        ),
        change(
            task = "Holt shed job's finished, set its status to complete",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Holt shed power", "complete")
        ),
        change(
            task = "tag the Patel solar prep note #ev-charger",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            holds = listOf("Patel solar prep", "ev-charger")
        ),
        change(
            task = "add to yesterday's journal: Fraser board passed inspection",
            kind = Kind.DOCUMENT,
            tools = listOf("date", "search", "append", "propose"),
            holds = listOf("2026-11-10", "passed inspection")
        ),
        change(
            task = "new note called Unpaid invoices listing what's still owed, with amounts",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "write_note", "propose"),
            holds = listOf("Unpaid invoices", "INV-1043", "INV-1045", "INV-1046"),
            lacks = listOf("INV-1047", "INV-1044")
        ),
        change(
            task = "move the wiring rules notes into a new Reference folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "create_folder", "move", "propose"),
            holds = listOf("Wiring rules notes", "Reference")
        ),
        change(
            task = "rename Holt shed power to Holt shed sub-board",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Holt shed power", "Holt shed sub-board")
        ),
        change(
            task = "tag every customer with an unpaid invoice #owes",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "list", "add_tag", "propose"),
            holds = listOf("Raj Patel", "Greg Holt", "Morrison Strata", "owes"),
            lacks = listOf("Kelly Nguyen", "Kelly Fraser")
        ),
        change(
            task = "got the label printer, tick it off my wishlist",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "tasks", "edit_note", "propose"),
            holds = listOf("Wishlist", "label printer")
        ),
        change(
            task = "set the Fraser job's status to waiting on asbestos",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Fraser switchboard upgrade", "waiting on asbestos")
        ),
        change(
            task = "log Tayla's hours for the week of 2026-11-09: 31 on site, no TAFE",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Hours", "2026-11-09", "31")
        ),
        change(
            task = "add ESC-2026-130 to the certificate log for the Nguyen kitchen, issued today",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "date", "edit_note", "propose"),
            holds = listOf("Safety certificates", "ESC-2026-130", "Nguyen")
        )
    )
}
