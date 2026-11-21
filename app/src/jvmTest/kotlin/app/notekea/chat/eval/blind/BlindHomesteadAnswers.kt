package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Homestead notes, three they do not answer. */
internal object BlindHomesteadAnswers {
    val all = listOf(
        AgentWideCase(
            task = "how many eggs did we get in october, counting the cracked ones?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("284")
        ),
        AgentWideCase(
            task = "what's my average eggs per logged day so far this month?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("6.2")
        ),
        AgentWideCase(
            task = "how much have I spent on the vet this year?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("505"),
            lacks = listOf("575")
        ),
        AgentWideCase(
            task = "how much do I owe for feed right now?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("517")
        ),
        AgentWideCase(
            task = "what did my own stuff bring in at the october markets, after the refund?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("549.5")
        ),
        AgentWideCase(
            task = "what do my pending seed orders add up to?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("158.45")
        ),
        AgentWideCase(
            task = "which buck did Clover get bred to?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Jasper")
        ),
        AgentWideCase(
            task = "what was the nitrate level in the well test?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("3.1")
        ),
        AgentWideCase(
            task = "what was the soil pH in bed 9?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("5.8")
        ),
        AgentWideCase(
            task = "how many days until the holiday market?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("31 days")
        ),
        AgentWideCase(
            task = "how many days between Daisy Alpine's mastitis visit and the recheck?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("49 days")
        ),
        AgentWideCase(
            task = "how much milk did Clover give the first ten days of november?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("32.5")
        ),
        AgentWideCase(
            task = "who's covering Eli's chores over thanksgiving?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("June")
        ),
        AgentWideCase(
            task = "how deep is the well?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("212")
        ),
        AgentWideCase(
            task = "how many hens do I have now?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("13")
        ),
        AgentWideCase(
            task = "how much did Eli earn in october?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("216")
        ),
        AgentWideCase(
            task = "did the well test show any lead?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("lead was 0.08", "lead: 0.08", "lead at 0.08")
        ),
        AgentWideCase(
            task = "when did I last deworm the goats?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("dewormed on 2026-06-15", "dewormed on june 15", "were dewormed on")
        ),
        AgentWideCase(
            task = "what's Dr. Harlan's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("931-555-0187")
        )
    )
}
