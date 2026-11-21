package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindHouseholdAnswers {
    val all = listOf(
        AgentWideCase(
            task = "how much have we spent so far this month in the household budget?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1346.05")
        ),
        AgentWideCase(
            task = "what's the total of the bills we still haven't paid this month?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("587.27")
        ),
        AgentWideCase(
            task = "how much do our subscriptions cost a month now?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("94.97"),
            lacks = listOf("106.97")
        ),
        AgentWideCase(
            task = "how many days until the mortgage renews?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("110 days")
        ),
        AgentWideCase(
            task = "how much did we actually make at the garage sale?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("216"),
            lacks = listOf("406", "266")
        ),
        AgentWideCase(
            task = "how much is left in the kitchen reno budget after what we've already paid?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("34938")
        ),
        AgentWideCase(
            task = "how many days between the reno start and the scheduled finish?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("53")
        ),
        AgentWideCase(
            task = "total of our receipted donations so far this year?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("545"),
            lacks = listOf("585")
        ),
        AgentWideCase(
            task = "how much more is the contractor we picked than the cheapest quote?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            holds = listOf("Brennan"),
            computed = listOf("1700")
        ),
        AgentWideCase(
            task = "how long is the warranty on the reno work?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Okafor", "2 year")
        ),
        AgentWideCase(
            task = "what interest is the emergency fund earning?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("3.1")
        ),
        AgentWideCase(
            task = "if we're at theo's parents over christmas is a frozen pipe still covered " +
                "by the home insurance?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("daily")
        ),
        AgentWideCase(
            task = "do we have to tell the insurance company about the kitchen reno?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("30 days")
        ),
        AgentWideCase(
            task = "is the car loan rate higher than the mortgage rate? by how much?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            holds = listOf("6.29", "4.39"),
            computed = listOf("1.9")
        ),
        AgentWideCase(
            task = "how much more do we need to save for lisbon?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("2320")
        ),
        AgentWideCase(
            task = "what's our house assessed at for property tax?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("780,000", "815,000")
        ),
        AgentWideCase(
            task = "how much was biscuit's vet bill this month?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("92.30")
        ),
        AgentWideCase(
            task = "what's the policy number on the life insurance?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("HX-448120", "CA-77310")
        )
    )
}
