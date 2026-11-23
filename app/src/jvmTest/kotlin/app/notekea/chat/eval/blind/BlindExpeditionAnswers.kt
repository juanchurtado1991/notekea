package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindExpeditionAnswers {
    val all = listOf(
        answer(
            task = "how many days until summit day?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("156 days")
        ),
        answer(
            task = "how much have I spent on gear so far?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("2333"),
            lacks = listOf("2398")
        ),
        answer(
            task = "total weight in grams of everything going in my duffel, counting what " +
                "I still have to buy?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("5740")
        ),
        answer(
            task = "how much do I still owe on permits and fees?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("120")
        ),
        answer(
            task = "add up everything I still have to pay: the unpaid lines in the expedition " +
                "budget plus the unpaid permits",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            computed = listOf("3270")
        ),
        answer(
            task = "how much is in the expedition fund right now?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("4050"),
            lacks = listOf("4400")
        ),
        answer(
            task = "what's my average elevation gain across the training climbs?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("3895")
        ),
        answer(
            task = "what was my SpO2 on top of Elbert?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("86")
        ),
        answer(
            task = "does my insurance cover me on the summit? whats the altitude limit",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "read", "answer"),
            holds = listOf("6500")
        ),
        answer(
            task = "what number do I call before a helicopter rescue?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            holds = listOf("0147")
        ),
        answer(
            task = "what time is our flight to Lukla?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            holds = listOf("6:15")
        ),
        answer(
            task = "how do I reach Pemba?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "links", "read", "answer"),
            holds = listOf("4417")
        ),
        answer(
            task = "is Priya allergic to anything?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "search", "read", "answer"),
            holds = listOf("ibuprofen"),
            lacks = listOf("penicillin")
        ),
        answer(
            task = "how many days between Longs Peak and Pikes Peak?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            computed = listOf("64 days")
        ),
        answer(
            task = "what's still not done in the documents part of my packing list?",
            kind = Kind.DOCUMENT,
            tools = listOf("tasks", "answer"),
            holds = listOf("Passport photos", "Insurance card", "Emergency contacts"),
            lacks = listOf("e-tickets")
        ),
        answer(
            task = "how much did Alex Moreno pay for his sleeping bag?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "read", "answer"),
            lacks = listOf("489")
        ),
        answer(
            task = "when does Priya land in Kathmandu?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "answer"),
            lacks = listOf("10:55")
        ),
        answer(
            task = "what was my time on Pikes Peak?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            lacks = listOf("6h40", "12h10")
        )
    )
}
