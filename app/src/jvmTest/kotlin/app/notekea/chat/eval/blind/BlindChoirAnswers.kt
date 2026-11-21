package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Choir vault, three of them not answered by the notes. */
internal object BlindChoirAnswers {
    val all = listOf(
        AgentWideCase(
            task = "how much have we collected in subs this term, after the refund?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("690")
        ),
        AgentWideCase(
            task = "how much subs is still owed?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("165")
        ),
        AgentWideCase(
            task = "what's our average attendance this term so far?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("19.75")
        ),
        AgentWideCase(
            task = "how much ticket money have we taken for the winter concert?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1140")
        ),
        AgentWideCase(
            task = "what are the total costs in the winter concert budget?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("685")
        ),
        AgentWideCase(
            task = "what do the pending sheet music orders come to?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("168")
        ),
        AgentWideCase(
            task = "what have we actually spent on sheet music that's arrived, after the refund?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("234.5")
        ),
        AgentWideCase(
            task = "how many days till the winter concert?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("31 days")
        ),
        AgentWideCase(
            task = "how many days has Rhian Hughes been off on leave now?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("42 days")
        ),
        AgentWideCase(
            task = "who's playing piano at rehearsal on the 25th?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Lisa Moffat")
        ),
        AgentWideCase(
            task = "what's the dress code for the winter concert?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("red scarves")
        ),
        AgentWideCase(
            task = "how much notice do we have to give for the AGM?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("21")
        ),
        AgentWideCase(
            task = "how many days till the AGM?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("77 days")
        ),
        AgentWideCase(
            task = "how long is the winter concert programme, not counting the interval?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("35 min")
        ),
        AgentWideCase(
            task = "which venue has the biggest deposit we still haven't paid?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Penylan")
        ),
        AgentWideCase(
            task = "what's Dafydd Price's email?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("dafydd.price@")
        ),
        AgentWideCase(
            task = "how many tickets did we sell for last year's winter concert?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("last year we sold", "last year you sold")
        ),
        AgentWideCase(
            task = "what time does the winter concert finish?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("finishes at", "ends at")
        )
    )
}
