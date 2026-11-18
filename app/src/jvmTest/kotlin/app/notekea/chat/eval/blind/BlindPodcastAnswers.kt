package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindPodcastAnswers {
    val all = listOf(
        AgentWideCase(
            task = "how much in ad reads is still unpaid?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1585")
        ),
        AgentWideCase(
            task = "how much have sponsors actually paid me in the ad reads log, refund aside?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("2195")
        ),
        AgentWideCase(
            task = "add up what I've billed Northfield in the ad reads log",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1700")
        ),
        AgentWideCase(
            task = "average 7-day downloads for eps 137 to 141?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("3260")
        ),
        AgentWideCase(
            task = "what's the real 30-day number for ep 139?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("3,815")
        ),
        AgentWideCase(
            task = "do I qualify for the Northfield download bonus? how much is it?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("1,250", "141")
        ),
        AgentWideCase(
            task = "what's the late fee if Northfield pays late?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("1.5")
        ),
        AgentWideCase(
            task = "what topic did listeners ask for most in the survey?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("food history")
        ),
        AgentWideCase(
            task = "what timestamp did Tomas want cut from his episode?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("38:12")
        ),
        AgentWideCase(
            task = "how long has mic 2 been at the repair shop?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("22 days")
        ),
        AgentWideCase(
            task = "how many days between recording ep 143 and when it comes out?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("20 days")
        ),
        AgentWideCase(
            task = "how much did my gear cost, not counting the acoustic panels?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1723")
        ),
        AgentWideCase(
            task = "which days of the week is Dev free to record?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Wednesday", "Friday")
        ),
        AgentWideCase(
            task = "which guests are booked but not recorded yet?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Lucia Ferraro", "Dev Raman", "Marguerite Bell")
        ),
        AgentWideCase(
            task = "is there any reason ep 144 can't go out earlier?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("album")
        ),
        AgentWideCase(
            task = "what's Priya Desai's email?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("scarboroughhoney")
        ),
        AgentWideCase(
            task = "when did mic 2 come back from the shop?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("came back on")
        ),
        AgentWideCase(
            task = "how many downloads did ep 142 get in its first 30 days?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("3391 downloads in its first 30 days")
        )
    )
}
