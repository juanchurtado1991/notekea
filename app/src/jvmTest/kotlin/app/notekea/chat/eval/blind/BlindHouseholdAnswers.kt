package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindHouseholdAnswers {
    val all = listOf(
        answer(
            task = "how much have we spent so far this month in the household budget?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("1346.05")
        ),
        answer(
            task = "what's the total of the bills we still haven't paid this month?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("587.27")
        ),
        answer(
            task = "how much do our subscriptions cost a month now?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("94.97"),
            lacks = listOf("106.97")
        ),
        answer(
            task = "how many days until the mortgage renews?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("110 days")
        ),
        answer(
            task = "how much did we actually make at the garage sale?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("216"),
            lacks = listOf("406", "266")
        ),
        answer(
            task = "how much is left in the kitchen reno budget after what we've already paid?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("34938")
        ),
        answer(
            task = "how many days between the reno start and the scheduled finish?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("53")
        ),
        answer(
            task = "total of our receipted donations so far this year?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("545"),
            lacks = listOf("585")
        ),
        answer(
            task = "how much more is the contractor we picked than the cheapest quote?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            holds = listOf("Brennan"),
            computed = listOf("1700")
        ),
        answer(
            task = "how long is the warranty on the reno work?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Okafor", "2 year")
        ),
        answer(
            task = "what interest is the emergency fund earning?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("3.1")
        ),
        answer(
            task = "if we're at theo's parents over christmas is a frozen pipe still covered " +
                "by the home insurance?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("daily")
        ),
        answer(
            task = "do we have to tell the insurance company about the kitchen reno?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("30 days")
        ),
        answer(
            task = "is the car loan rate higher than the mortgage rate? by how much?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            holds = listOf("6.29", "4.39"),
            computed = listOf("1.9")
        ),
        answer(
            task = "how much more do we need to save for lisbon?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("2320")
        ),
        answer(
            task = "what's our house assessed at for property tax?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("780,000", "815,000")
        ),
        answer(
            task = "how much was biscuit's vet bill this month?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("92.30")
        ),
        answer(
            task = "what's the policy number on the life insurance?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("HX-448120", "CA-77310")
        )
    )
}
