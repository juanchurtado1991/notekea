package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Interior Design vault. */
internal object BlindInteriorDesignAnswers {
    val all = listOf(
        answer(
            task = "how much have I spent on hampstead so far, not counting my fee?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("24970")
        ),
        answer(
            task = "total of whats still pending on the hampstead orders?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("6316")
        ),
        answer(
            task = "which line is over budget on the islington flat and by how much",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            holds = listOf("Contractors"),
            computed = listOf("470")
        ),
        answer(
            task = "how much am I owed in unpaid invoices right now",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("3905")
        ),
        answer(
            task = "what did the business spend in october, leaving out the " +
                "refunded one and the personal one?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("981")
        ),
        answer(
            task = "how many days till the hampstead sofa arrives?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("22 days")
        ),
        answer(
            task = "whats the weekly storage charge in my T&Cs?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("£45")
        ),
        answer(
            task = "if a client disputes a bill and we can't sort it ourselves, " +
                "what comes before court?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("mediation")
        ),
        answer(
            task = "is ravi allergic to anything I should know about?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("feather")
        ),
        answer(
            task = "what finish are the hampstead alcove shelves getting?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Smoked Sage")
        ),
        answer(
            task = "how many days later than first promised is meera's fern fabric " +
                "now due?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            computed = listOf("7 days")
        ),
        answer(
            task = "which supplier gives me the best trade discount?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "answer"),
            holds = listOf("Fenwick", "20%")
        ),
        answer(
            task = "whats the total value of pending orders across all three projects?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "calculate", "answer"),
            computed = listOf("10650")
        ),
        answer(
            task = "average day rate of my contractors?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("277.5")
        ),
        answer(
            task = "who's my next client meeting with and when?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Meera", "15:30")
        ),
        answer(
            task = "whats tom ashby's email",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("tom@", "tom.ashby@", "ashby@")
        ),
        answer(
            task = "what do I charge for weekend work?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("arrangement"),
            lacks = listOf("weekend rate is £", "weekends are £")
        ),
        answer(
            task = "what budget did hannah lowe give for her kitchen?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("her budget is £40", "budget of £40", "budget is around £40")
        )
    )
}
