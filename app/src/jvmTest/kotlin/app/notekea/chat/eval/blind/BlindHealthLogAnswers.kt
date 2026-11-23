package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindHealthLogAnswers {
    val all = listOf(
        answer(
            task = "how many miles did I run in october?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("37.5")
        ),
        answer(
            task = "until when am I banned from hills?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("November 16")
        ),
        answer(
            task = "how many days passed between the injury and my first run back?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            computed = listOf("64")
        ),
        answer(
            task = "how many days until the front range marathon?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("186")
        ),
        answer(
            task = "how much do I still owe the physio clinic?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("150"),
            lacks = listOf("420")
        ),
        answer(
            task = "between the two blood tests, how much did my ferritin and my vitamin D " +
                "go up together, in ng/mL?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("39")
        ),
        answer(
            task = "what do my supplements cost per month? only the ones I'm actually taking",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("88"),
            lacks = listOf("110")
        ),
        answer(
            task = "what's the total of my Purchases 2026 note, leaving out the refunded stuff?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("863"),
            lacks = listOf("943")
        ),
        answer(
            task = "what's my average sleep so far this month?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("7.2")
        ),
        answer(
            task = "what pace am I supposed to run the front range marathon at?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("8:45")
        ),
        answer(
            task = "am I allergic to any meds?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("amoxicillin")
        ),
        answer(
            task = "by when do I have to decide on the PRP injection?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("2027-02-01")
        ),
        answer(
            task = "what was my CRP on the latest labs?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("1.2")
        ),
        answer(
            task = "how much protein am I supposed to eat a day?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("140")
        ),
        answer(
            task = "which shoes in my rotation are past the 400 mile limit?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Ridgeline")
        ),
        answer(
            task = "what was my cholesterol on the last bloodwork?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("mg/dl")
        ),
        answer(
            task = "what time does the platte river half start?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("06:30")
        ),
        answer(
            task = "what's mike osei's marathon PR?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("4:05:33")
        )
    )
}
