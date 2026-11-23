package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindHistoryThesisAnswers {
    val all = listOf(
        answer(
            task = "what's the call number for the kirkcaldy flax letters?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("RH15/88/3")
        ),
        answer(
            task = "how many words have I got across all my chapters so far?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "calculate", "answer"),
            computed = listOf("41360")
        ),
        answer(
            task = "how many days until the Utrecht conference?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("64 days")
        ),
        answer(
            task = "what did my supervisors say chapter 2 has to be cut to?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            holds = listOf("16,000")
        ),
        answer(
            task = "in Merchants of the Northern Sea, what year did Ramsay & Ord go bankrupt?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("1847")
        ),
        answer(
            task = "which ship had the biggest timber cargo in my Leith ledger transcriptions?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Janet")
        ),
        answer(
            task = "who's chairing my annual review panel this year?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Euan Fraser")
        ),
        answer(
            task = "how much am I still waiting to get back from my expense claims?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("365"),
            lacks = listOf("383")
        ),
        answer(
            task = "average words per session in my writing log for november?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("772")
        ),
        answer(
            task = "how many words over its thesis plan target is chapter 2 right now?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            computed = listOf("4450")
        ),
        answer(
            task = "which of the books I've taken notes on did I rate highest?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Ports and Paperwork")
        ),
        answer(
            task = "how many days did Helen have my chapter 3 draft before she sent comments back?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("38 days")
        ),
        answer(
            task = "what time's my thursday tutorial?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("15:10")
        ),
        answer(
            task = "how much travel funding have I actually been awarded in total?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("825")
        ),
        answer(
            task = "who wrote the main secondary source for chapter 4?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "links", "read", "answer"),
            holds = listOf("Ellwood")
        ),
        answer(
            task = "which of my students has an extension on the first essay?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Callum Reid")
        ),
        answer(
            task = "what's Helen Grant's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf(
                "her number is 0131",
                "helen's number is 0131",
                "her phone number is 0131"
            )
        ),
        answer(
            task = "what's the ISBN of The Flax Frontier?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("isbn is 978")
        ),
        answer(
            task = "how much is my hotel in Utrecht costing?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf(
                "utrecht hotel costs £336",
                "utrecht hotel was £336",
                "hotel in utrecht costs £"
            )
        )
    )
}
