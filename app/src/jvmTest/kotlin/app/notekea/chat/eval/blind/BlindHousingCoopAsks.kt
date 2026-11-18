package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests that must be asked back, and traps whose words settle them, in Blind Housing Coop. */
internal object BlindHousingCoopAsks {
    val all = listOf(
        AgentWideCase(
            task = "add a line Contingency 5000 to the budget",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("renovation")
        ),
        AgentWideCase(
            task = "add a task to give Anna back her ladder",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Berger", "Novak")
        ),
        AgentWideCase(
            task = "Anna has a new email, anna.k@posteo.at, update her note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Berger", "Novak")
        ),
        AgentWideCase(
            task = "move the elevator appointment to 2026-11-20, same time",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("inspection", "cable")
        ),
        AgentWideCase(
            task = "mark the open contractor invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Dachwerk", "Kaminski")
        ),
        AgentWideCase(
            task = "in the minutes change the stairwell windows vote to 13 in favour",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "the leak is fixed, close the request",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("sink", "attic")
        ),
        AgentWideCase(
            task = "set the insurance policy's renewal to 2027-12-31",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("building", "liability")
        ),
        AgentWideCase(
            task = "push the garden committee meeting to 19:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("planting", "tool shed")
        ),
        AgentWideCase(
            task = "how much is building insurance in next year's operating budget?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("6840")
        ),
        AgentWideCase(
            task = "which units are both Annas in?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Berger", "Novak", "3B", "5A")
        ),
        AgentWideCase(
            task = "the board approved Anna Novak's balcony screen, tag her note #approved",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Anna Novak", "approved")
        ),
        AgentWideCase(
            task = "the Lift-Tec invoice is paid, mark it",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Contractor invoices", "Lift-Tec")
        ),
        AgentWideCase(
            task = "the plumber fixed the leak in 1A, close that request",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Requests log", "R-31")
        )
    )
}
