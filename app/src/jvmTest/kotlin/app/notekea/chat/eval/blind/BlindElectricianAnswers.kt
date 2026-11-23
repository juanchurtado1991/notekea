package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Electrician vault, three of them not answered by the notes. */
internal object BlindElectricianAnswers {
    val all = listOf(
        answer(
            task = "what's the gate code at Kelly Nguyen's?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            holds = listOf("4471")
        ),
        answer(
            task = "what did the Nguyen kitchen materials cost, not counting the returned dimmer?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("1136")
        ),
        answer(
            task = "how much is still to come on the Nguyen kitchen after the deposit?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            computed = listOf("2436")
        ),
        answer(
            task = "how much am I owed in unpaid invoices?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("4830")
        ),
        answer(
            task = "who owes me the most right now?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Morrison Strata"),
            lacks = listOf("greg holt owes the most")
        ),
        answer(
            task = "who's taking the asbestos board out at the Fraser job?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("ClearAir")
        ),
        answer(
            task = "who's got the meter box key at Fraser's?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Barry")
        ),
        answer(
            task = "what colour are the RCD test stickers I put on the boards?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("orange")
        ),
        answer(
            task = "how many days till my public liability renews?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("59 days")
        ),
        answer(
            task = "what have I actually spent on tools this year, refunds taken off?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("1108")
        ),
        answer(
            task = "what's the van cost me out of pocket this year, fuel aside?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("2206")
        ),
        answer(
            task = "how many km left before the van's next service?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "search", "read", "calculate", "answer"),
            computed = listOf("6580")
        ),
        answer(
            task = "what did Tayla earn on site the week of 2 Nov?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            computed = listOf("871")
        ),
        answer(
            task = "what's my average fill-up cost in the fuel log?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("109")
        ),
        answer(
            task = "how many days till Tayla's next TAFE block starts?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "date", "answer"),
            computed = listOf("89 days")
        ),
        answer(
            task = "who's the strata manager on the stairwell job?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("Denise Kowalski")
        ),
        answer(
            task = "what's Greg Holt's email?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("@")
        ),
        answer(
            task = "how much is my public liability premium?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("premium is $", "premium was $", "premium is 20")
        ),
        answer(
            task = "what's the gate code at Kelly Fraser's?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("fraser's gate code is 4471", "fraser's code is 4471")
        )
    )
}
