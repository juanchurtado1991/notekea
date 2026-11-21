package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindApplicantAnswers {
    val all = listOf(
        AgentWideCase(
            task = "how much have i paid in app fees so far",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("195")
        ),
        AgentWideCase(
            task = "how much do I still owe in application fees?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("185")
        ),
        AgentWideCase(
            task = "what's my SAT superscore?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1440")
        ),
        AgentWideCase(
            task = "what's my average AP score",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("4.2")
        ),
        AgentWideCase(
            task = "what would sound valley actually cost me per year after grants and " +
                "scholarships, not counting loans or work-study?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("30900")
        ),
        AgentWideCase(
            task = "which school has the lowest estimated net price?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Rainier State")
        ),
        AgentWideCase(
            task = "how many days between when I submitted the FAFSA and when the Sound Valley " +
                "offer came?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("34 days")
        ),
        AgentWideCase(
            task = "how many days left until the washington futures deadline?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("81 days")
        ),
        AgentWideCase(
            task = "what's the word limit for the rainier state honors essay?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("350")
        ),
        AgentWideCase(
            task = "what did mr ortiz want from me for his rec letter?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("brag sheet", "build log")
        ),
        AgentWideCase(
            task = "how many words is the latest draft of my personal statement?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("641")
        ),
        AgentWideCase(
            task = "what's our SAI?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("8,200")
        ),
        AgentWideCase(
            task = "who's interviewing me for northbridge?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Colin Marsh")
        ),
        AgentWideCase(
            task = "how much have the campus visits actually cost so far, not counting refunds?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("313")
        ),
        AgentWideCase(
            task = "which tour did I rate higher, cascade pacific or rainier state?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Cascade Pacific")
        ),
        AgentWideCase(
            task = "how much is the greenlake award worth?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("2,500")
        ),
        AgentWideCase(
            task = "what was my PSAT score?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("your psat score was")
        ),
        AgentWideCase(
            task = "when does cascade pacific release ED decisions?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER
        ),
        AgentWideCase(
            task = "what's hannah lee's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("her number is 206-555-0143")
        )
    )
}
