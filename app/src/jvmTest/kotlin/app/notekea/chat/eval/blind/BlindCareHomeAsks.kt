package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests the care home vault cannot settle, then traps whose words do settle them. */
internal object BlindCareHomeAsks {
    val all = listOf(
        AgentWideCase(
            task = "push the family meeting back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Kaye", "Booth")
        ),
        AgentWideCase(
            task = "add to Margaret's note that she's joined the choir",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Booth", "Kaye")
        ),
        AgentWideCase(
            task = "add £18 for refreshments to the budget",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("activities", "outings")
        ),
        AgentWideCase(
            task = "log 3 more hours for Tom this month",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Ashby", "Reyes")
        ),
        AgentWideCase(
            task = "mark the pending order as delivered",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("PlayWell", "Greenleaf")
        ),
        AgentWideCase(
            task = "move the music booking to 3pm",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Ukuleles", "Delia")
        ),
        AgentWideCase(
            task = "set the minibus booking to confirmed",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("garden centre", "christmas market")
        ),
        AgentWideCase(
            task = "rename the budget note to Budget 2026-27",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("activities", "outings")
        ),
        AgentWideCase(
            task = "how much is left in the activities budget?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("2635")
        ),
        AgentWideCase(
            task = "what do both Margarets like doing?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("knitting", "gardening")
        ),
        AgentWideCase(
            task = "mark the Brightcraft order that's still pending as delivered",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("SO-105", "delivered")
        ),
        AgentWideCase(
            task = "move Margaret Kaye's family meeting to 14:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Kaye", "14:00")
        ),
        AgentWideCase(
            task = "add a task to both Margarets' notes: follow up on the flu jab",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Margaret Booth", "Margaret Kaye")
        )
    )
}
