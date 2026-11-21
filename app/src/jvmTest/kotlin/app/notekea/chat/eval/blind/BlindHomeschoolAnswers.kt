package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Homeschool vault, three of them not answered by the notes. */
internal object BlindHomeschoolAnswers {
    val all = listOf(
        AgentWideCase(
            task = "what's Isla's average maths test score?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("78")
        ),
        AgentWideCase(
            task = "how much do I still owe the tutors?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("480")
        ),
        AgentWideCase(
            task = "what did the trips we've already been on cost altogether, after refunds?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("351")
        ),
        AgentWideCase(
            task = "what did the Harbourside trip end up costing us?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("131")
        ),
        AgentWideCase(
            task = "how many minutes did Ruby read from 2 to 6 Nov?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("105")
        ),
        AgentWideCase(
            task = "how many days till the observatory trip?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("16 days")
        ),
        AgentWideCase(
            task = "how many days between Isla's geometry test and her linear equations one?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("28 days")
        ),
        AgentWideCase(
            task = "what time does the observatory night viewing start?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("20:30")
        ),
        AgentWideCase(
            task = "what's Finn doing for the science fair?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("worm")
        ),
        AgentWideCase(
            task = "how much supervision allowance do we get for Ruby a year?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("520")
        ),
        AgentWideCase(
            task = "if we move house how long do I have to tell the ministry?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("30 days")
        ),
        AgentWideCase(
            task = "which Hannah teaches Ruby?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Sorensen"),
            lacks = listOf("Ngata teaches Ruby")
        ),
        AgentWideCase(
            task = "how much supervision allowance are we getting in total this year?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1920")
        ),
        AgentWideCase(
            task = "what's Isla reading next?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("The Glass Harbour")
        ),
        AgentWideCase(
            task = "how much have I spent on curriculum stuff just for Isla this year?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("154")
        ),
        AgentWideCase(
            task = "which online school is Isla doing chemistry with next year?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Rimu Online School")
        ),
        AgentWideCase(
            task = "what's Hannah Sorensen's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("number is 0", "number is +64", "call her on 0")
        ),
        AgentWideCase(
            task = "what did Finn get on his last maths test?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("he scored 89", "finn scored 89", "finn got 89")
        ),
        AgentWideCase(
            task = "who's hosting the 9 Dec co-op meeting?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("moana reid is hosting", "priya anand is hosting")
        )
    )
}
