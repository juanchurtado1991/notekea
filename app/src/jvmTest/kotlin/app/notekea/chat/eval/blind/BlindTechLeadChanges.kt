package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindTechLeadChanges {
    val all = listOf(
        change(
            task = "add a task to sprint 47: load test the payouts scheduler",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Sprint 47", "load test")
        ),
        change(
            task = "mark ADR-015 as accepted",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            holds = listOf("ADR-015", "accepted")
        ),
        change(
            task = "tag The Calm Pager with #oncall",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            holds = listOf("The Calm Pager", "oncall")
        ),
        change(
            task = "Amira accepted the offer today, add that to her note",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "append", "propose"),
            holds = listOf("Amira Selby", "accepted")
        ),
        change(
            task = "new note Q4 incidents: each incident this quarter and its duration",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "write_note", "propose"),
            holds = listOf(
                "Q4 incidents",
                "Webhook delays",
                "Card auth timeout",
                "47 min",
                "18 min"
            ),
            lacks = listOf("Duplicate payouts")
        ),
        change(
            task = "make an Archive folder and move the fraud rules engine note into it",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "create_folder", "move", "propose"),
            holds = listOf("Fraud Rules Engine", "Archive")
        ),
        change(
            task = "rename sprint 47 to Sprint 47 - Payouts beta prep",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Payouts beta prep")
        ),
        change(
            task = "move Leo Ferraz's phone screen to 15:00",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Leo Ferraz", "15:00")
        ),
        change(
            task = "tag the note of every candidate still in the process with #active",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "read", "add_tag", "propose"),
            holds = listOf("Leo Brandt", "Leo Ferraz", "Amira Selby", "active"),
            lacks = listOf("Tomas Reyes")
        ),
        change(
            task = "add a 'schedule review' task to every ADR that's still proposed",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "add_task", "propose"),
            holds = listOf("ADR-015", "ADR-016"),
            lacks = listOf("ADR-014")
        ),
        change(
            task = "mark the Lintora invoice as paid in the team budget",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Lintora", "paid")
        ),
        change(
            task = "link the duplicate payouts postmortem to ADR-013",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "link", "propose"),
            holds = listOf("Duplicate payouts", "ADR-013")
        )
    )
}
