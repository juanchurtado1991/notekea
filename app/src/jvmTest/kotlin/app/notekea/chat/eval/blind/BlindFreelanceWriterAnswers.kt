package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindFreelanceWriterAnswers {
    val all = listOf(
        AgentWideCase(
            task = "how much am I owed in overdue invoices?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1380"),
            lacks = listOf("2520")
        ),
        AgentWideCase(
            task = "what's my deductible expenses total so far?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("764"),
            lacks = listOf("898", "853", "809")
        ),
        AgentWideCase(
            task = "how many days are there between the canal swimmers deadline and the" +
                " Greenmoor deadline?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("56")
        ),
        AgentWideCase(
            task = "if it's still unpaid on 30 November, how many days overdue will the Portrane" +
                " invoice be?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("57")
        ),
        AgentWideCase(
            task = "which outlet have I published the most words with this year, and how many?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            holds = listOf("Northlight"),
            computed = listOf("5800")
        ),
        AgentWideCase(
            task = "add up how many days each of my pending pitches has been waiting since I sent" +
                " it: what's the total?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("44")
        ),
        AgentWideCase(
            task = "what PO number goes on the rent pressure zones invoice?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("PO-LED-2207"),
            lacks = listOf("PO-LED-2140")
        ),
        AgentWideCase(
            task = "who do I chase about the Portrane invoice?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Brid Kehoe")
        ),
        AgentWideCase(
            task = "what ammonia reading did Aoife get beside the farm?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("38 mg")
        ),
        AgentWideCase(
            task = "how much was Declan Moran paid for each night load?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("€40"),
            lacks = listOf("€25")
        ),
        AgentWideCase(
            task = "what's my working estimate of how much extra waste went in at Portrane?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("3,400")
        ),
        AgentWideCase(
            task = "how long is the full Rented Lives manuscript meant to be?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("85,000"),
            lacks = listOf("12,000")
        ),
        AgentWideCase(
            task = "which of my editors pays the best per word, and which pays the worst?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Siobhan Kerr", "Ravi Menon")
        ),
        AgentWideCase(
            task = "what will my current commissions pay altogether?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("7700")
        ),
        AgentWideCase(
            task = "what's the average amount of my paid invoices?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1115")
        ),
        AgentWideCase(
            task = "what's Siobhan's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("086 550 2914", "01 677 3021", "087 214 6630")
        ),
        AgentWideCase(
            task = "what time is Maeve calling me?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("14:30", "10:00", "09:30")
        ),
        AgentWideCase(
            task = "what did Atlantic Current pay me for the Kildare rents piece?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("285")
        )
    )
}
