package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindHealthLogAnswers {
    val all = listOf(
        AgentWideCase(
            task = "how many miles did I run in october?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("37.5")
        ),
        AgentWideCase(
            task = "until when am I banned from hills?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("November 16")
        ),
        AgentWideCase(
            task = "how many days passed between the injury and my first run back?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("64")
        ),
        AgentWideCase(
            task = "how many days until the front range marathon?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("186")
        ),
        AgentWideCase(
            task = "how much do I still owe the physio clinic?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("150"),
            lacks = listOf("420")
        ),
        AgentWideCase(
            task = "between the two blood tests, how much did my ferritin and my vitamin D " +
                "go up together, in ng/mL?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("39")
        ),
        AgentWideCase(
            task = "what do my supplements cost per month? only the ones I'm actually taking",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("88"),
            lacks = listOf("110")
        ),
        AgentWideCase(
            task = "what's the total of my Purchases 2026 note, leaving out the refunded stuff?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("863"),
            lacks = listOf("943")
        ),
        AgentWideCase(
            task = "what's my average sleep so far this month?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("7.2")
        ),
        AgentWideCase(
            task = "what pace am I supposed to run the front range marathon at?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("8:45")
        ),
        AgentWideCase(
            task = "am I allergic to any meds?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("amoxicillin")
        ),
        AgentWideCase(
            task = "by when do I have to decide on the PRP injection?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("2027-02-01")
        ),
        AgentWideCase(
            task = "what was my CRP on the latest labs?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("1.2")
        ),
        AgentWideCase(
            task = "how much protein am I supposed to eat a day?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("140")
        ),
        AgentWideCase(
            task = "which shoes in my rotation are past the 400 mile limit?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Ridgeline")
        ),
        AgentWideCase(
            task = "what was my cholesterol on the last bloodwork?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("mg/dl")
        ),
        AgentWideCase(
            task = "what time does the platte river half start?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("06:30")
        ),
        AgentWideCase(
            task = "what's mike osei's marathon PR?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("4:05:33")
        )
    )
}
