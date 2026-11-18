package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindGameDevAsks {
    val all = listOf(
        // Asks: the words name one thing and the vault has several that fit.
        AgentWideCase(
            task = "mark sam's last invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Okafor", "Delisle")
        ),
        AgentWideCase(
            task = "add a task to send sam the new palette",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Okafor", "Delisle")
        ),
        AgentWideCase(
            task = "push sam's contract end date to 2027-03-31",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Okafor", "Delisle")
        ),
        AgentWideCase(
            task = "add the tag #finance to my budget note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("budget")
        ),
        AgentWideCase(
            task = "rename my ideas note to Brainstorm",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("marketing")
        ),
        AgentWideCase(
            task = "move the appointment to friday at 2pm",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Brightkite", "bank")
        ),
        AgentWideCase(
            task = "cancel the pending order",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("monitor", "capture card", "pen display")
        ),
        AgentWideCase(
            task = "tag the playtest note #archived",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("playtest", "14")
        ),
        AgentWideCase(
            task = "mark the critical bug as fixed",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("save", "boss"),
            lacks = listOf("B-06")
        ),
        // Traps: they look ambiguous, but the words settle them.
        AgentWideCase(
            task = "how much do i still owe the sam who does the character sprites?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            holds = listOf("Okafor"),
            computed = listOf("1254"),
            lacks = listOf("1904")
        ),
        AgentWideCase(
            task = "mark sam delisle's lighthouse invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Sam Delisle Contract", "SD-04")
        ),
        AgentWideCase(
            task = "add the tag #finance to the tidewell budget",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Game/Budget", "finance"),
            lacks = listOf("Business/Budget")
        ),
        AgentWideCase(
            task = "add 'devlog video every month' to the marketing ideas note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Marketing/Ideas", "devlog video every month"),
            lacks = listOf("Game/Ideas")
        ),
        AgentWideCase(
            task = "how much are all my pending hardware orders together?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1208.49"),
            lacks = listOf("1298.47")
        )
    )
}
