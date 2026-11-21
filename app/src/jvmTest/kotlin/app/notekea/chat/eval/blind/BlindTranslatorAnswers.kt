package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions the Blind Translator vault answers, and three it does not. */
internal object BlindTranslatorAnswers {
    val all = listOf(
        AgentWideCase(
            task = "how much am I still owed in total? unpaid + overdue",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("4235")
        ),
        AgentWideCase(
            task = "what's the total of my overdue invoices?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("2660")
        ),
        AgentWideCase(
            task = "how many days overdue is the Northlight invoice?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("12 days")
        ),
        AgentWideCase(
            task = "what's the average amount of my paid invoices?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("748")
        ),
        AgentWideCase(
            task = "how much will the leaflet job pay me?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("378")
        ),
        AgentWideCase(
            task = "how many days until part 2 of the museum brochure is due?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("23 days")
        ),
        AgentWideCase(
            task = "how many days left before my Tradora licence renews?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("95 days")
        ),
        AgentWideCase(
            task = "how much did I spend on software this year?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("424")
        ),
        AgentWideCase(
            task = "what are my total business expenses so far this year?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1497")
        ),
        AgentWideCase(
            task = "who do I send the Verba Global invoices to?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Martin Kessler")
        ),
        AgentWideCase(
            task = "what's the character limit for buttons in the UI strings job?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("18 characters")
        ),
        AgentWideCase(
            task = "what's the penalty in the Northlight NDA if I breach it?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("20000")
        ),
        AgentWideCase(
            task = "which court handles disputes under my NDA with Northlight?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Rotterdam")
        ),
        AgentWideCase(
            task = "what kind of Portuguese does Old Fenwick speak in the game?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Alentejo")
        ),
        AgentWideCase(
            task = "who pays more per word, Verba Translations or Verba Global?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Verba Global", "0.11")
        ),
        AgentWideCase(
            task = "how much have I invoiced Okafor Studio this year?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1922")
        ),
        AgentWideCase(
            task = "what's hannah's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("900418")
        ),
        AgentWideCase(
            task = "what does Verba Global pay me for proofreading?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("verba global pays eur 25", "verba global pays 25")
        ),
        AgentWideCase(
            task = "what time is my call with Sofia?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("call with sofia is at")
        )
    )
}
