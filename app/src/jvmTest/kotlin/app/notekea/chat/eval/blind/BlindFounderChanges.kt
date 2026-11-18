package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindFounderChanges {
    val all = listOf(
        AgentWideCase(
            task = "add a task to Sofia's note: send her the sales playbook before she starts",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Sofia Reyes", "sales playbook")
        ),
        AgentWideCase(
            task = "set the status on the term sheet note to signed",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Term Sheet", "signed")
        ),
        AgentWideCase(
            task = "tag every customer note that has an unpaid invoice with #collections",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf(
                "Lonestar Freight",
                "Pecan Street Distribution",
                "Brazos Supply",
                "collections"
            ),
            lacks = listOf("Hill Country Cold Chain")
        ),
        AgentWideCase(
            task = "make me a journal note for tomorrow with everything I've got on that day",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("date", "search", "read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Ben Achebe", "Pecan Street", "10:00", "14:00")
        ),
        AgentWideCase(
            task = "rename Pitch Deck Feedback to Deck Feedback v3",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Deck Feedback v3")
        ),
        AgentWideCase(
            task = "move the Legal note into the Fundraising folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Legal", "Fundraising")
        ),
        AgentWideCase(
            task = "add brazos' november invoice: INV-1075, $2,400, unpaid, due 2026-12-01",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Brazos Supply", "INV-1075")
        ),
        AgentWideCase(
            task = "kai thornton's onsite moved to 2026-11-18 at 10:00, update it",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Kai Thornton", "10:00")
        ),
        AgentWideCase(
            task = "write a new note November Investor Update with october MRR, churn " +
                "and cash in the bank",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("36480", "1.1", "842000")
        ),
        AgentWideCase(
            task = "tag owen pratt's note with #lead-investor",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Owen Pratt", "lead-investor")
        ),
        AgentWideCase(
            task = "elliot's phone screen got pushed to 17:00 same day, change it",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Elliot Shaw", "17:00")
        ),
        AgentWideCase(
            task = "add a task on the lonestar note: confirm EDI timeline with Marcus",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Lonestar Freight", "EDI timeline")
        )
    )
}
