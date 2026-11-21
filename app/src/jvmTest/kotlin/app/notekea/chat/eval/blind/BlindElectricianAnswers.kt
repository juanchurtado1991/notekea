package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Electrician vault, three of them not answered by the notes. */
internal object BlindElectricianAnswers {
    val all = listOf(
        AgentWideCase(
            task = "what's the gate code at Kelly Nguyen's?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("4471")
        ),
        AgentWideCase(
            task = "what did the Nguyen kitchen materials cost, not counting the returned dimmer?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1136")
        ),
        AgentWideCase(
            task = "how much is still to come on the Nguyen kitchen after the deposit?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("2436")
        ),
        AgentWideCase(
            task = "how much am I owed in unpaid invoices?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("4830")
        ),
        AgentWideCase(
            task = "who owes me the most right now?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Morrison Strata"),
            lacks = listOf("greg holt owes the most")
        ),
        AgentWideCase(
            task = "who's taking the asbestos board out at the Fraser job?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("ClearAir")
        ),
        AgentWideCase(
            task = "who's got the meter box key at Fraser's?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Barry")
        ),
        AgentWideCase(
            task = "what colour are the RCD test stickers I put on the boards?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("orange")
        ),
        AgentWideCase(
            task = "how many days till my public liability renews?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("59 days")
        ),
        AgentWideCase(
            task = "what have I actually spent on tools this year, refunds taken off?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1108")
        ),
        AgentWideCase(
            task = "what's the van cost me out of pocket this year, fuel aside?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("2206")
        ),
        AgentWideCase(
            task = "how many km left before the van's next service?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("6580")
        ),
        AgentWideCase(
            task = "what did Tayla earn on site the week of 2 Nov?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("871")
        ),
        AgentWideCase(
            task = "what's my average fill-up cost in the fuel log?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("109")
        ),
        AgentWideCase(
            task = "how many days till Tayla's next TAFE block starts?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("89 days")
        ),
        AgentWideCase(
            task = "who's the strata manager on the stairwell job?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Denise Kowalski")
        ),
        AgentWideCase(
            task = "what's Greg Holt's email?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("@")
        ),
        AgentWideCase(
            task = "how much is my public liability premium?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("premium is $", "premium was $", "premium is 20")
        ),
        AgentWideCase(
            task = "what's the gate code at Kelly Fraser's?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("fraser's gate code is 4471", "fraser's code is 4471")
        )
    )
}
