package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindGameDevAnswers {
    val all = listOf(
        AgentWideCase(
            task = "how much did mossback make in Q1 across all the stores?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("6730"),
            lacks = listOf("7030")
        ),
        AgentWideCase(
            task = "which month was mossback's best, all stores together?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            holds = listOf("March"),
            computed = listOf("3170")
        ),
        AgentWideCase(
            task = "how many hours has sam okafor billed me so far",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("87")
        ),
        AgentWideCase(
            task = "what do all my hardware orders add up to, leaving out the refunded one?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1298.47"),
            lacks = listOf("1947.47")
        ),
        AgentWideCase(
            task = "how many days until the bank meeting?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("21 days")
        ),
        AgentWideCase(
            task = "how long did the fishing softlock take to fix?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("16 days")
        ),
        AgentWideCase(
            task = "how many days left before the festival deadline?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("23 days")
        ),
        AgentWideCase(
            task = "did i pay the festival fee yet?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("60")
        ),
        AgentWideCase(
            task = "how do i pay sam delisle?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("e-transfer")
        ),
        AgentWideCase(
            task = "which tester found the save bug?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Ravi")
        ),
        AgentWideCase(
            task = "how much hp does the drowned keeper have?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("1800")
        ),
        AgentWideCase(
            task = "what's tidewell's internal resolution again",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("384", "216")
        ),
        AgentWideCase(
            task = "is photo mode going in the launch build?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("update")
        ),
        AgentWideCase(
            task = "what's the average fun score across my playtests?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("3.8")
        ),
        AgentWideCase(
            task = "which critical bugs are still open?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("B-03", "B-09"),
            lacks = listOf("B-06")
        ),
        AgentWideCase(
            task = "how many copies of mossback did i sell on the eshop in may?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("405 copies")
        ),
        AgentWideCase(
            task = "what's priya's hourly rate?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("38")
        ),
        AgentWideCase(
            task = "what did brightkite offer me for the deal?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("70/30")
        )
    )
}
