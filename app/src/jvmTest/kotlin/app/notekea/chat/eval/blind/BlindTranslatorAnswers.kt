package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions the Blind Translator vault answers, and three it does not. */
internal object BlindTranslatorAnswers {
    val all = listOf(
        answer(
            task = "how much am I still owed in total? unpaid + overdue",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("4235")
        ),
        answer(
            task = "what's the total of my overdue invoices?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("2660")
        ),
        answer(
            task = "how many days overdue is the Northlight invoice?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("12 days")
        ),
        answer(
            task = "what's the average amount of my paid invoices?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("748")
        ),
        answer(
            task = "how much will the leaflet job pay me?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            computed = listOf("378")
        ),
        answer(
            task = "how many days until part 2 of the museum brochure is due?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("23 days")
        ),
        answer(
            task = "how many days left before my Tradora licence renews?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("95 days")
        ),
        answer(
            task = "how much did I spend on software this year?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("424")
        ),
        answer(
            task = "what are my total business expenses so far this year?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("1497")
        ),
        answer(
            task = "who do I send the Verba Global invoices to?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("Martin Kessler")
        ),
        answer(
            task = "what's the character limit for buttons in the UI strings job?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("18 characters")
        ),
        answer(
            task = "what's the penalty in the Northlight NDA if I breach it?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("20000")
        ),
        answer(
            task = "which court handles disputes under my NDA with Northlight?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Rotterdam")
        ),
        answer(
            task = "what kind of Portuguese does Old Fenwick speak in the game?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Alentejo")
        ),
        answer(
            task = "who pays more per word, Verba Translations or Verba Global?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Verba Global", "0.11")
        ),
        answer(
            task = "how much have I invoiced Okafor Studio this year?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("1922")
        ),
        answer(
            task = "what's hannah's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("900418")
        ),
        answer(
            task = "what does Verba Global pay me for proofreading?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("verba global pays eur 25", "verba global pays 25")
        ),
        answer(
            task = "what time is my call with Sofia?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("call with sofia is at")
        )
    )
}
