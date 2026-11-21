package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindPodcastAsks {
    val all = listOf(
        AgentWideCase(
            task = "add to Priya's note that she's away until January",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Nair", "Desai")
        ),
        AgentWideCase(
            task = "push the recording back an hour",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Lucia", "Dev", "Marguerite")
        ),
        AgentWideCase(
            task = "tick off the first item on my checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "mark Sam Whitford's invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("142", "143")
        ),
        AgentWideCase(
            task = "add a task to call Sam on Friday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Whitford", "Osei")
        ),
        AgentWideCase(
            task = "mark the Northfield read as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("141", "142")
        ),
        AgentWideCase(
            task = "mark the guest who never replied as declined",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Felix", "Rosalind")
        ),
        AgentWideCase(
            task = "add 'review these in January' to my rates note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("sponsor")
        ),
        AgentWideCase(
            task = "tag Priya Nair's guest note #returning-guest",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Priya Nair", "returning-guest")
        ),
        AgentWideCase(
            task = "move Dev's recording to 11:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Dev Raman", "11:00")
        ),
        AgentWideCase(
            task = "add a task to Sam Osei's note: confirm Studio B for the 27th",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Sam Osei", "Studio B")
        ),
        AgentWideCase(
            task = "mark both unpaid Northfield reads as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Northfield", "141", "142")
        ),
        AgentWideCase(
            task = "how much do I owe Sam Whitford on invoices he has already sent?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("560")
        )
    )
}
