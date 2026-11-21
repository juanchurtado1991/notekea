package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Vet Clinic vault, three of them not answered by it. */
internal object BlindVetClinicAnswers {
    val all = listOf(
        AgentWideCase(
            task = "how much is still unpaid on my invoices?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("804"),
            lacks = listOf("899")
        ),
        AgentWideCase(
            task = "how much have I actually collected? just the paid invoices",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("756"),
            lacks = listOf("851")
        ),
        AgentWideCase(
            task = "which surgeries are still left this month?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            ends = End.ANSWER,
            holds = listOf("Biscuit", "Rook")
        ),
        AgentWideCase(
            task = "days from Biscuit's dental surgery to the first day of the summit?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("16 days")
        ),
        AgentWideCase(
            task = "how many days until I drive to Portland?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("21 days")
        ),
        AgentWideCase(
            task = "what's the average weight of my dog patients?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("17.5")
        ),
        AgentWideCase(
            task = "what's the hotel confirmation number for the summit?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("HX-48213")
        ),
        AgentWideCase(
            task = "is Max Hollins allergic to anything?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("amoxicillin")
        ),
        AgentWideCase(
            task = "who's our autoclave tech again?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Dale Kirby")
        ),
        AgentWideCase(
            task = "what room is the feline dentistry workshop in?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Harbor Room")
        ),
        AgentWideCase(
            task = "who has spent more at the clinic, Ruth or Ellis?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            holds = listOf("Ruth"),
            computed = listOf("546")
        ),
        AgentWideCase(
            task = "how many rabies doses do we have in stock right now?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("53"),
            lacks = listOf("73")
        ),
        AgentWideCase(
            task = "what are the clinic's monthly costs not counting payroll?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("13300"),
            lacks = listOf("34700")
        ),
        AgentWideCase(
            task = "who's covering the clinic while I'm at the summit?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Priya")
        ),
        AgentWideCase(
            task = "what's still left on my summit to-do list?",
            kind = Kind.DOCUMENT,
            tools = listOf("tasks", "answer"),
            ends = End.ANSWER,
            holds = listOf("certificate", "loupes")
        ),
        AgentWideCase(
            task = "what did the X-ray generator repair cost?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("1180", "1450", "640")
        ),
        AgentWideCase(
            task = "what's Clover's microchip number?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("985112004567321", "985112009904410")
        ),
        AgentWideCase(
            task = "how much was my summit registration?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("189", "567")
        )
    )
}
