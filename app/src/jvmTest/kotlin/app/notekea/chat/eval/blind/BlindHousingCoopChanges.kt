package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Housing Coop vault, each clear from the words and the notes. */
internal object BlindHousingCoopChanges {
    val all = listOf(
        AgentWideCase(
            task = "add to my secretary tasks: send the December agenda to all members",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Secretary tasks", "December agenda")
        ),
        AgentWideCase(
            task = "set the boiler note's status to approved",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Boiler", "approved")
        ),
        AgentWideCase(
            task = "tag the facade project note #priority",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Facade project", "priority")
        ),
        AgentWideCase(
            task = "add to yesterday's log that Petra signed the Dachwerk contract",
            kind = Kind.DOCUMENT,
            tools = listOf("date", "search", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-11-10", "signed")
        ),
        AgentWideCase(
            task = "make a note called Open invoices listing every unpaid contractor invoice " +
                "with its amount",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf(
                "Open invoices",
                "Dachwerk",
                "Lift-Tec",
                "Grünraum",
                "Kaminski",
                "2860",
                "685",
                "420",
                "1115"
            )
        ),
        AgentWideCase(
            task = "move the Insurance note into the Finance folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Insurance", "Finance")
        ),
        AgentWideCase(
            task = "rename Courtyard garden to Courtyard and garden committee",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Courtyard garden", "Courtyard and garden committee")
        ),
        AgentWideCase(
            task = "Kaminski gave us until 2026-12-15 for the stairwell lights invoice, " +
                "update it",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Kaminski", "2026-12-15")
        ),
        AgentWideCase(
            task = "tag every resident who still owes fees with #arrears",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "add_tag", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Okafor", "Stastny", "Eder", "arrears")
        ),
        AgentWideCase(
            task = "write a short reminder for Rudi Stastny with what he owes, " +
                "call the note Reminder 5B",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Reminder 5B", "397", "October")
        ),
        AgentWideCase(
            task = "add a task to the boiler note: get a second quote for a heat pump",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Boiler", "second quote")
        ),
        AgentWideCase(
            task = "link Jonas Eder's note to the Arrears note",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "link", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Jonas Eder", "Arrears")
        )
    )
}
