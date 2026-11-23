package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindPodcastAnswers {
    val all = listOf(
        answer(
            task = "how much in ad reads is still unpaid?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("1585")
        ),
        answer(
            task = "how much have sponsors actually paid me in the ad reads log, refund aside?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("2195")
        ),
        answer(
            task = "add up what I've billed Northfield in the ad reads log",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("1700")
        ),
        answer(
            task = "average 7-day downloads for eps 137 to 141?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("3260")
        ),
        answer(
            task = "what's the real 30-day number for ep 139?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("3,815")
        ),
        answer(
            task = "do I qualify for the Northfield download bonus? how much is it?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("1,250", "141")
        ),
        answer(
            task = "what's the late fee if Northfield pays late?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("1.5")
        ),
        answer(
            task = "what topic did listeners ask for most in the survey?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("food history")
        ),
        answer(
            task = "what timestamp did Tomas want cut from his episode?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("38:12")
        ),
        answer(
            task = "how long has mic 2 been at the repair shop?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("22 days")
        ),
        answer(
            task = "how many days between recording ep 143 and when it comes out?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("20 days")
        ),
        answer(
            task = "how much did my gear cost, not counting the acoustic panels?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("1723")
        ),
        answer(
            task = "which days of the week is Dev free to record?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Wednesday", "Friday")
        ),
        answer(
            task = "which guests are booked but not recorded yet?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Lucia Ferraro", "Dev Raman", "Marguerite Bell")
        ),
        answer(
            task = "is there any reason ep 144 can't go out earlier?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "search", "read", "answer"),
            holds = listOf("album")
        ),
        answer(
            task = "what's Priya Desai's email?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("scarboroughhoney")
        ),
        answer(
            task = "when did mic 2 come back from the shop?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("came back on")
        ),
        answer(
            task = "how many downloads did ep 142 get in its first 30 days?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("3391 downloads in its first 30 days")
        )
    )
}
