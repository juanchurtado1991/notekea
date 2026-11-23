package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindFounderChanges {
    val all = listOf(
        change(
            task = "add a task to Sofia's note: send her the sales playbook before she starts",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            holds = listOf("Sofia Reyes", "sales playbook")
        ),
        change(
            task = "set the status on the term sheet note to signed",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            holds = listOf("Term Sheet", "signed")
        ),
        change(
            task = "tag every customer note that has an unpaid invoice with #collections",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "read", "add_tag", "propose"),
            holds = listOf(
                "Lonestar Freight",
                "Pecan Street Distribution",
                "Brazos Supply",
                "collections"
            ),
            lacks = listOf("Hill Country Cold Chain")
        ),
        change(
            task = "make me a journal note for tomorrow with everything I've got on that day",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("date", "search", "read", "read", "write_note", "propose"),
            holds = listOf("Ben Achebe", "Pecan Street", "10:00", "14:00")
        ),
        change(
            task = "rename Pitch Deck Feedback to Deck Feedback v3",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Deck Feedback v3")
        ),
        change(
            task = "move the Legal note into the Fundraising folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            holds = listOf("Legal", "Fundraising")
        ),
        change(
            task = "add brazos' november invoice: INV-1075, $2,400, unpaid, due 2026-12-01",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Brazos Supply", "INV-1075")
        ),
        change(
            task = "kai thornton's onsite moved to 2026-11-18 at 10:00, update it",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Kai Thornton", "10:00")
        ),
        change(
            task = "write a new note November Investor Update with october MRR, churn " +
                "and cash in the bank",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "write_note", "propose"),
            holds = listOf("36480", "1.1", "842000")
        ),
        change(
            task = "tag owen pratt's note with #lead-investor",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_tag", "propose"),
            holds = listOf("Owen Pratt", "lead-investor")
        ),
        change(
            task = "elliot's phone screen got pushed to 17:00 same day, change it",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Elliot Shaw", "17:00")
        ),
        change(
            task = "add a task on the lonestar note: confirm EDI timeline with Marcus",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            holds = listOf("Lonestar Freight", "EDI timeline")
        )
    )
}
