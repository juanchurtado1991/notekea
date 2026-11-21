package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindGameDevChanges {
    val all = listOf(
        AgentWideCase(
            task = "add 'translate the store description to French' to the store page checklist",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Store Page Launch Checklist", "French")
        ),
        AgentWideCase(
            task = "set the design doc's status to review",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Design Document", "review")
        ),
        AgentWideCase(
            task = "tag theo's note with #playtester",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Theo Marchand", "playtester")
        ),
        AgentWideCase(
            task = "add a line to the festival submission note: trailer exported on 2026-11-11",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Festival Submission", "trailer exported")
        ),
        AgentWideCase(
            task = "make a note called Unpaid Invoices listing every invoice i haven't paid yet",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Unpaid Invoices", "SO-03", "SO-04", "SD-04"),
            lacks = listOf("SO-01", "PN-01")
        ),
        AgentWideCase(
            task = "move hana's note into the Business folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Hana Brodeur", "Business")
        ),
        AgentWideCase(
            task = "rename Mossback Revenue to Mossback Revenue 2026",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Mossback Revenue 2026")
        ),
        AgentWideCase(
            task = "change B-05 severity to high",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("B-05", "high")
        ),
        AgentWideCase(
            task = "tag every contract that still has an unpaid invoice with #unpaid",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf(
                "search",
                "read",
                "read",
                "read",
                "read",
                "add_tag",
                "add_tag",
                "propose"
            ),
            ends = End.PROPOSE,
            holds = listOf("Sam Okafor Contract", "Sam Delisle Contract"),
            lacks = listOf("Lou Bastien", "Priya Nair Contract")
        ),
        AgentWideCase(
            task = "add a task to the backlog for each open critical bug, with the bug id",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "add_task", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Feature Backlog", "B-03", "B-09"),
            lacks = listOf("B-06")
        ),
        AgentWideCase(
            task = "set the festival submission status to submitted",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Festival Submission", "submitted")
        ),
        AgentWideCase(
            task = "add a task to my meetings note: bring the october receipts to the accountant",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Meetings", "receipts")
        )
    )
}
