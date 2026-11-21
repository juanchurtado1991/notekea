package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests the Blind Homeschool vault cannot settle, and traps whose words do settle them. */
internal object BlindHomeschoolAsks {
    val all = listOf(
        AgentWideCase(
            task = "add to Hannah's note that she's away the first week of December",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Ngata", "Sorensen")
        ),
        AgentWideCase(
            task = "add 'return library books Friday' to the weekly plan",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Isla", "Finn", "Ruby")
        ),
        AgentWideCase(
            task = "push the co-op meeting back an hour",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "set reviewed: yes on my budget note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Curriculum")
        ),
        AgentWideCase(
            task = "mark Hannah's October invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Ngata", "Sorensen")
        ),
        AgentWideCase(
            task = "push the trip back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("list", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Observatory", "Tui")
        ),
        AgentWideCase(
            task = "set the progress note's status to reviewed",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Isla", "Finn")
        ),
        AgentWideCase(
            task = "move the dentist appointment to 2 December",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Isla", "Ruby")
        ),
        AgentWideCase(
            task = "mark Hannah Sorensen's October invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Hannah Sorensen", "paid")
        ),
        AgentWideCase(
            task = "add 'return library books Friday' to Ruby's weekly plan",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Ruby", "return library books")
        ),
        AgentWideCase(
            task = "what time is Isla's dentist appointment?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("15:45")
        ),
        AgentWideCase(
            task = "how much is left in the curriculum budget?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1227")
        ),
        AgentWideCase(
            task = "move the 25 Nov co-op meeting to 11:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("11:00", "Meetings 2026")
        )
    )
}
