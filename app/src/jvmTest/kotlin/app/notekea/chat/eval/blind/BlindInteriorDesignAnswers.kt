package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Interior Design vault. */
internal object BlindInteriorDesignAnswers {
    val all = listOf(
        AgentWideCase(
            task = "how much have I spent on hampstead so far, not counting my fee?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("24970")
        ),
        AgentWideCase(
            task = "total of whats still pending on the hampstead orders?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("6316")
        ),
        AgentWideCase(
            task = "which line is over budget on the islington flat and by how much",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            holds = listOf("Contractors"),
            computed = listOf("470")
        ),
        AgentWideCase(
            task = "how much am I owed in unpaid invoices right now",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("3905")
        ),
        AgentWideCase(
            task = "what did the business spend in october, leaving out the " +
                "refunded one and the personal one?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("981")
        ),
        AgentWideCase(
            task = "how many days till the hampstead sofa arrives?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("22 days")
        ),
        AgentWideCase(
            task = "whats the weekly storage charge in my T&Cs?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("£45")
        ),
        AgentWideCase(
            task = "if a client disputes a bill and we can't sort it ourselves, " +
                "what comes before court?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("mediation")
        ),
        AgentWideCase(
            task = "is ravi allergic to anything I should know about?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("feather")
        ),
        AgentWideCase(
            task = "what finish are the hampstead alcove shelves getting?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Smoked Sage")
        ),
        AgentWideCase(
            task = "how many days later than first promised is meera's fern fabric " +
                "now due?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("7 days")
        ),
        AgentWideCase(
            task = "which supplier gives me the best trade discount?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Fenwick", "20%")
        ),
        AgentWideCase(
            task = "whats the total value of pending orders across all three projects?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("10650")
        ),
        AgentWideCase(
            task = "average day rate of my contractors?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("277.5")
        ),
        AgentWideCase(
            task = "who's my next client meeting with and when?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Meera", "15:30")
        ),
        AgentWideCase(
            task = "whats tom ashby's email",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("tom@", "tom.ashby@", "ashby@")
        ),
        AgentWideCase(
            task = "what do I charge for weekend work?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("arrangement"),
            lacks = listOf("weekend rate is £", "weekends are £")
        ),
        AgentWideCase(
            task = "what budget did hannah lowe give for her kitchen?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("her budget is £40", "budget of £40", "budget is around £40")
        )
    )
}
