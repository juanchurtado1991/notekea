package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Investor Journal vault that the words and the notes settle. */
internal object BlindInvestorJournalChanges {
    val all = listOf(
        AgentWideCase(
            task = "add a task to my brokerage note to update the holdings table " +
                "after the 14 Nov NGI buy",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Brokerage", "holdings")
        ),
        AgentWideCase(
            task = "set the house deposit goal to paused",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("House deposit", "paused")
        ),
        AgentWideCase(
            task = "tag the northgate PDS with #to-reread",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Northgate", "to-reread")
        ),
        AgentWideCase(
            task = "log a 400 savings deposit for today in the november contributions",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-11", "400")
        ),
        AgentWideCase(
            task = "make a Contacts note in Plans with the numbers for " +
                "Martin Hale, Martin Osei and Ruth",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Contacts", "03 9387 2214", "1300 448 902", "0412 655 038")
        ),
        AgentWideCase(
            task = "move the 2026-09-14 journal entry into Journal/Archive",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "create_folder", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-09-14", "Archive")
        ),
        AgentWideCase(
            task = "rename Reinvestment plan to DRP settings",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Reinvestment plan", "DRP settings")
        ),
        AgentWideCase(
            task = "tag every contributions month that had a failed debit with #failed-debit",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "read", "read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-08", "2026-10", "failed-debit"),
            lacks = listOf("2026-07", "2026-09")
        ),
        AgentWideCase(
            task = "add to my goals: review insurance cover before 2027-03-01",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Goals", "review insurance cover")
        ),
        AgentWideCase(
            task = "link my Savings note to the Emergency fund note",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "link", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Savings", "Emergency fund")
        ),
        AgentWideCase(
            task = "add the brokerage for the 3 Nov SAU buy to my fees log",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Fees", "5.50", "SAU")
        ),
        AgentWideCase(
            task = "set next_review on my brokerage note to 2027-01-15",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Brokerage", "next_review", "2027-01-15")
        )
    )
}
