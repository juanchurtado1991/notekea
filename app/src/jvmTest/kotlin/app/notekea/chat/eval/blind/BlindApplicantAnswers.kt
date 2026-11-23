package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindApplicantAnswers {
    val all = listOf(
        answer(
            task = "how much have i paid in app fees so far",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("195")
        ),
        answer(
            task = "how much do I still owe in application fees?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("185")
        ),
        answer(
            task = "what's my SAT superscore?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("1440")
        ),
        answer(
            task = "what's my average AP score",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("4.2")
        ),
        answer(
            task = "what would sound valley actually cost me per year after grants and " +
                "scholarships, not counting loans or work-study?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("30900")
        ),
        answer(
            task = "which school has the lowest estimated net price?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Rainier State")
        ),
        answer(
            task = "how many days between when I submitted the FAFSA and when the Sound Valley " +
                "offer came?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            computed = listOf("34 days")
        ),
        answer(
            task = "how many days left until the washington futures deadline?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("81 days")
        ),
        answer(
            task = "what's the word limit for the rainier state honors essay?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("350")
        ),
        answer(
            task = "what did mr ortiz want from me for his rec letter?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("brag sheet", "build log")
        ),
        answer(
            task = "how many words is the latest draft of my personal statement?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("641")
        ),
        answer(
            task = "what's our SAI?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("8,200")
        ),
        answer(
            task = "who's interviewing me for northbridge?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Colin Marsh")
        ),
        answer(
            task = "how much have the campus visits actually cost so far, not counting refunds?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("313")
        ),
        answer(
            task = "which tour did I rate higher, cascade pacific or rainier state?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Cascade Pacific")
        ),
        answer(
            task = "how much is the greenlake award worth?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("2,500")
        ),
        answer(
            task = "what was my PSAT score?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("your psat score was")
        ),
        answer(
            task = "when does cascade pacific release ED decisions?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer")
        ),
        answer(
            task = "what's hannah lee's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("her number is 206-555-0143")
        )
    )
}
