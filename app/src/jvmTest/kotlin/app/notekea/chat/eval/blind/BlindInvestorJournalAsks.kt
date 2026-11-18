package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests the Blind Investor Journal vault cannot settle, and traps whose words settle them. */
internal object BlindInvestorJournalAsks {
    val all = listOf(
        AgentWideCase(
            task = "add a task to the Super note to check the binding nomination",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Ruth")
        ),
        AgentWideCase(
            task = "update the balance on the Super note to 190,000",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Ruth")
        ),
        AgentWideCase(
            task = "move my meeting with Martin to 16:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Hale", "Osei")
        ),
        AgentWideCase(
            task = "cancel the pending order in my brokerage note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("SAU", "NGI", "BDX")
        ),
        AgentWideCase(
            task = "add #reviewed to the October journal",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "list", "ask_user"),
            ends = End.ASK,
            holds = listOf("2026-10-03", "2026-10-20")
        ),
        AgentWideCase(
            task = "set last_contacted on Martin's note to today",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Hale", "Osei")
        ),
        AgentWideCase(
            task = "push the appointment in Meetings back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Hale", "Osei", "Corella")
        ),
        AgentWideCase(
            task = "set the target for the index fund to 45%",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("SAU", "NGI", "BDX")
        ),
        AgentWideCase(
            task = "change the Martin Osei call to 15:30",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Martin Osei", "15:30")
        ),
        AgentWideCase(
            task = "set Ruth's super balance to 158,400",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Ruth", "158400")
        ),
        AgentWideCase(
            task = "what's Martin Hale's phone number?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("03 9387 2214"),
            lacks = listOf("1300 448 902")
        ),
        AgentWideCase(
            task = "tag the 2026-10-20 journal with #rebalance",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-10-20", "rebalance"),
            lacks = listOf("2026-10-03")
        ),
        AgentWideCase(
            task = "what's the balance in my super?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("184320")
        ),
        AgentWideCase(
            task = "add a task to both Super notes: check the binding nomination",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Ruth", "nomination")
        )
    )
}
