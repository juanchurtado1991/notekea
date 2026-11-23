package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Choir vault, three of them not answered by the notes. */
internal object BlindChoirAnswers {
    val all = listOf(
        answer(
            task = "how much have we collected in subs this term, after the refund?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("690")
        ),
        answer(
            task = "how much subs is still owed?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("165")
        ),
        answer(
            task = "what's our average attendance this term so far?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("19.75")
        ),
        answer(
            task = "how much ticket money have we taken for the winter concert?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("1140")
        ),
        answer(
            task = "what are the total costs in the winter concert budget?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("685")
        ),
        answer(
            task = "what do the pending sheet music orders come to?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("168")
        ),
        answer(
            task = "what have we actually spent on sheet music that's arrived, after the refund?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("234.5")
        ),
        answer(
            task = "how many days till the winter concert?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("31 days")
        ),
        answer(
            task = "how many days has Rhian Hughes been off on leave now?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("42 days")
        ),
        answer(
            task = "who's playing piano at rehearsal on the 25th?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Lisa Moffat")
        ),
        answer(
            task = "what's the dress code for the winter concert?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("red scarves")
        ),
        answer(
            task = "how much notice do we have to give for the AGM?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("21")
        ),
        answer(
            task = "how many days till the AGM?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("77 days")
        ),
        answer(
            task = "how long is the winter concert programme, not counting the interval?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("35 min")
        ),
        answer(
            task = "which venue has the biggest deposit we still haven't paid?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Penylan")
        ),
        answer(
            task = "what's Dafydd Price's email?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("dafydd.price@")
        ),
        answer(
            task = "how many tickets did we sell for last year's winter concert?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("last year we sold", "last year you sold")
        ),
        answer(
            task = "what time does the winter concert finish?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("finishes at", "ends at")
        )
    )
}
