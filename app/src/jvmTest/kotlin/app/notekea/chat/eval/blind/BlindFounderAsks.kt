package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindFounderAsks {
    val all = listOf(
        AgentWideCase(
            task = "move my meeting with rachel to next thursday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Kim", "Duarte")
        ),
        AgentWideCase(
            task = "set rachel's status to committed",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Kim", "Duarte")
        ),
        AgentWideCase(
            task = "add a task to my pipeline note: chase everyone who went quiet",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "tick off the first item on my checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "push the onsite to friday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Kai", "Nadia")
        ),
        AgentWideCase(
            task = "move the pecan street call to monday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "append 'minutes sent to the board' to the board meeting note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "mark the pecan street invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK
        ),
        // Traps: they look ambiguous, but the words settle them.
        AgentWideCase(
            task = "when's my next call with rachel kim?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("15:30"),
            lacks = listOf("10:00")
        ),
        AgentWideCase(
            task = "how much are the two rachels putting in together?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("750")
        ),
        AgentWideCase(
            task = "add a task to my fundraising checklist: send the data room link to rachel kim",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("data room link")
        ),
        AgentWideCase(
            task = "mark both unpaid pecan street invoices as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("INV-1059", "INV-1074")
        )
    )
}
