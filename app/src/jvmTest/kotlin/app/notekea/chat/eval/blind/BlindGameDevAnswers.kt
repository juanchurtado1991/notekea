package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindGameDevAnswers {
    val all = listOf(
        answer(
            task = "how much did mossback make in Q1 across all the stores?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("6730"),
            lacks = listOf("7030")
        ),
        answer(
            task = "which month was mossback's best, all stores together?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            holds = listOf("March"),
            computed = listOf("3170")
        ),
        answer(
            task = "how many hours has sam okafor billed me so far",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("87")
        ),
        answer(
            task = "what do all my hardware orders add up to, leaving out the refunded one?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("1298.47"),
            lacks = listOf("1947.47")
        ),
        answer(
            task = "how many days until the bank meeting?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("21 days")
        ),
        answer(
            task = "how long did the fishing softlock take to fix?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("16 days")
        ),
        answer(
            task = "how many days left before the festival deadline?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("23 days")
        ),
        answer(
            task = "did i pay the festival fee yet?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("60")
        ),
        answer(
            task = "how do i pay sam delisle?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("e-transfer")
        ),
        answer(
            task = "which tester found the save bug?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Ravi")
        ),
        answer(
            task = "how much hp does the drowned keeper have?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("1800")
        ),
        answer(
            task = "what's tidewell's internal resolution again",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("384", "216")
        ),
        answer(
            task = "is photo mode going in the launch build?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("update")
        ),
        answer(
            task = "what's the average fun score across my playtests?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "calculate", "answer"),
            computed = listOf("3.8")
        ),
        answer(
            task = "which critical bugs are still open?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("B-03", "B-09"),
            lacks = listOf("B-06")
        ),
        answer(
            task = "how many copies of mossback did i sell on the eshop in may?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("405 copies")
        ),
        answer(
            task = "what's priya's hourly rate?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            lacks = listOf("38")
        ),
        answer(
            task = "what did brightkite offer me for the deal?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            lacks = listOf("70/30")
        )
    )
}
