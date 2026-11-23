package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Homeschool vault, three of them not answered by the notes. */
internal object BlindHomeschoolAnswers {
    val all = listOf(
        answer(
            task = "what's Isla's average maths test score?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("78")
        ),
        answer(
            task = "how much do I still owe the tutors?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("480")
        ),
        answer(
            task = "what did the trips we've already been on cost altogether, after refunds?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "calculate", "answer"),
            computed = listOf("351")
        ),
        answer(
            task = "what did the Harbourside trip end up costing us?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("131")
        ),
        answer(
            task = "how many minutes did Ruby read from 2 to 6 Nov?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("105")
        ),
        answer(
            task = "how many days till the observatory trip?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("16 days")
        ),
        answer(
            task = "how many days between Isla's geometry test and her linear equations one?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("28 days")
        ),
        answer(
            task = "what time does the observatory night viewing start?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("20:30")
        ),
        answer(
            task = "what's Finn doing for the science fair?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            holds = listOf("worm")
        ),
        answer(
            task = "how much supervision allowance do we get for Ruby a year?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("520")
        ),
        answer(
            task = "if we move house how long do I have to tell the ministry?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("30 days")
        ),
        answer(
            task = "which Hannah teaches Ruby?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Sorensen"),
            lacks = listOf("Ngata teaches Ruby")
        ),
        answer(
            task = "how much supervision allowance are we getting in total this year?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("1920")
        ),
        answer(
            task = "what's Isla reading next?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("The Glass Harbour")
        ),
        answer(
            task = "how much have I spent on curriculum stuff just for Isla this year?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("154")
        ),
        answer(
            task = "which online school is Isla doing chemistry with next year?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Rimu Online School")
        ),
        answer(
            task = "what's Hannah Sorensen's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("number is 0", "number is +64", "call her on 0")
        ),
        answer(
            task = "what did Finn get on his last maths test?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            lacks = listOf("he scored 89", "finn scored 89", "finn got 89")
        ),
        answer(
            task = "who's hosting the 9 Dec co-op meeting?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("moana reid is hosting", "priya anand is hosting")
        )
    )
}
