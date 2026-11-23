package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindNurseAnswers {
    val all = listOf(
        answer(
            task = "how much overtime money is claimed but not paid yet?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("653.2")
        ),
        answer(
            task = "what's the total of my october actuals in the home budget, " +
                "leaving out savings?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("1887")
        ),
        answer(
            task = "how much do I still owe on my bills?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("451.4")
        ),
        answer(
            task = "how many participatory CPD hours have I logged so far?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("17.5")
        ),
        answer(
            task = "how many days left until the union ballot closes?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("26 days")
        ),
        answer(
            task = "what time can I pick up the keys for the new house?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("13:00", "Parkgate Homes")
        ),
        answer(
            task = "how many days from today until completion on the house?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("30 days")
        ),
        answer(
            task = "is anyone lending me a van for the move?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Aoife")
        ),
        answer(
            task = "which removal quote is cheapest once packing materials are added?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            holds = listOf("Swiftvan")
        ),
        answer(
            task = "what's the average price of the three removal quotes, " +
                "not counting any packing extras?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("645")
        ),
        answer(
            task = "which of my certificates run out before the end of this year?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Fire safety", "ALS"),
            lacks = listOf("Manual handling")
        ),
        answer(
            task = "how many days before my ALS cert expires is the recert course?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            computed = listOf("8 days")
        ),
        answer(
            task = "who's covering my long day on the 21st?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Chris Doyle"),
            lacks = listOf("Okafor")
        ),
        answer(
            task = "how many hours of nights am I rostered for in november?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("80.5")
        ),
        answer(
            task = "how many leave days have I got left, counting the december week " +
                "but not the february request?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("13 days")
        ),
        answer(
            task = "what did Tom want me to talk about at the branch meeting?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            holds = listOf("staffing")
        ),
        answer(
            task = "how much did the survey on the new house cost?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("1180")
        ),
        answer(
            task = "what room is the delirium study day in?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("B12")
        ),
        answer(
            task = "when's the union branch meeting after tomorrow's one?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("2026-12-02", "2 december")
        )
    )
}
