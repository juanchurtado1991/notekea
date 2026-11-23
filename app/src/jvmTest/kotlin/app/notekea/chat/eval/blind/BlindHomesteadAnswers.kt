package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Homestead notes, three they do not answer. */
internal object BlindHomesteadAnswers {
    val all = listOf(
        answer(
            task = "how many eggs did we get in october, counting the cracked ones?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("284")
        ),
        answer(
            task = "what's my average eggs per logged day so far this month?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("6.2")
        ),
        answer(
            task = "how much have I spent on the vet this year?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("505"),
            lacks = listOf("575")
        ),
        answer(
            task = "how much do I owe for feed right now?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("517")
        ),
        answer(
            task = "what did my own stuff bring in at the october markets, after the refund?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("549.5")
        ),
        answer(
            task = "what do my pending seed orders add up to?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("158.45")
        ),
        answer(
            task = "which buck did Clover get bred to?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("Jasper")
        ),
        answer(
            task = "what was the nitrate level in the well test?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("3.1")
        ),
        answer(
            task = "what was the soil pH in bed 9?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("5.8")
        ),
        answer(
            task = "how many days until the holiday market?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("31 days")
        ),
        answer(
            task = "how many days between Daisy Alpine's mastitis visit and the recheck?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("49 days")
        ),
        answer(
            task = "how much milk did Clover give the first ten days of november?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("32.5")
        ),
        answer(
            task = "who's covering Eli's chores over thanksgiving?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("June")
        ),
        answer(
            task = "how deep is the well?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("212")
        ),
        answer(
            task = "how many hens do I have now?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("13")
        ),
        answer(
            task = "how much did Eli earn in october?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("216")
        ),
        answer(
            task = "did the well test show any lead?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("lead was 0.08", "lead: 0.08", "lead at 0.08")
        ),
        answer(
            task = "when did I last deworm the goats?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("dewormed on 2026-06-15", "dewormed on june 15", "were dewormed on")
        ),
        answer(
            task = "what's Dr. Harlan's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("931-555-0187")
        )
    )
}
