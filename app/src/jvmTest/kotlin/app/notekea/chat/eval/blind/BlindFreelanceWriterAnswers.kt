package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindFreelanceWriterAnswers {
    val all = listOf(
        answer(
            task = "how much am I owed in overdue invoices?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("1380"),
            lacks = listOf("2520")
        ),
        answer(
            task = "what's my deductible expenses total so far?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("764"),
            lacks = listOf("898", "853", "809")
        ),
        answer(
            task = "how many days are there between the canal swimmers deadline and the" +
                " Greenmoor deadline?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            computed = listOf("56")
        ),
        answer(
            task = "if it's still unpaid on 30 November, how many days overdue will the Portrane" +
                " invoice be?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            computed = listOf("57")
        ),
        answer(
            task = "which outlet have I published the most words with this year, and how many?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            holds = listOf("Northlight"),
            computed = listOf("5800")
        ),
        answer(
            task = "add up how many days each of my pending pitches has been waiting since I sent" +
                " it: what's the total?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "calculate", "answer"),
            computed = listOf("44")
        ),
        answer(
            task = "what PO number goes on the rent pressure zones invoice?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            holds = listOf("PO-LED-2207"),
            lacks = listOf("PO-LED-2140")
        ),
        answer(
            task = "who do I chase about the Portrane invoice?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "search", "read", "answer"),
            holds = listOf("Brid Kehoe")
        ),
        answer(
            task = "what ammonia reading did Aoife get beside the farm?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("38 mg")
        ),
        answer(
            task = "how much was Declan Moran paid for each night load?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("€40"),
            lacks = listOf("€25")
        ),
        answer(
            task = "what's my working estimate of how much extra waste went in at Portrane?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("3,400")
        ),
        answer(
            task = "how long is the full Rented Lives manuscript meant to be?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("85,000"),
            lacks = listOf("12,000")
        ),
        answer(
            task = "which of my editors pays the best per word, and which pays the worst?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "answer"),
            holds = listOf("Siobhan Kerr", "Ravi Menon")
        ),
        answer(
            task = "what will my current commissions pay altogether?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("7700")
        ),
        answer(
            task = "what's the average amount of my paid invoices?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("1115")
        ),
        answer(
            task = "what's Siobhan's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("086 550 2914", "01 677 3021", "087 214 6630")
        ),
        answer(
            task = "what time is Maeve calling me?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("14:30", "10:00", "09:30")
        ),
        answer(
            task = "what did Atlantic Current pay me for the Kildare rents piece?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("285")
        )
    )
}
