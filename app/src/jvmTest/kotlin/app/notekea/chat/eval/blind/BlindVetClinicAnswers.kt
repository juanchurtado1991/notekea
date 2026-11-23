package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Vet Clinic vault, three of them not answered by it. */
internal object BlindVetClinicAnswers {
    val all = listOf(
        answer(
            task = "how much is still unpaid on my invoices?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("804"),
            lacks = listOf("899")
        ),
        answer(
            task = "how much have I actually collected? just the paid invoices",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("756"),
            lacks = listOf("851")
        ),
        answer(
            task = "which surgeries are still left this month?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            holds = listOf("Biscuit", "Rook")
        ),
        answer(
            task = "days from Biscuit's dental surgery to the first day of the summit?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "date", "answer"),
            computed = listOf("16 days")
        ),
        answer(
            task = "how many days until I drive to Portland?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("21 days")
        ),
        answer(
            task = "what's the average weight of my dog patients?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "calculate", "answer"),
            computed = listOf("17.5")
        ),
        answer(
            task = "what's the hotel confirmation number for the summit?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "search", "read", "answer"),
            holds = listOf("HX-48213")
        ),
        answer(
            task = "is Max Hollins allergic to anything?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            holds = listOf("amoxicillin")
        ),
        answer(
            task = "who's our autoclave tech again?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Dale Kirby")
        ),
        answer(
            task = "what room is the feline dentistry workshop in?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Harbor Room")
        ),
        answer(
            task = "who has spent more at the clinic, Ruth or Ellis?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            holds = listOf("Ruth"),
            computed = listOf("546")
        ),
        answer(
            task = "how many rabies doses do we have in stock right now?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("53"),
            lacks = listOf("73")
        ),
        answer(
            task = "what are the clinic's monthly costs not counting payroll?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("13300"),
            lacks = listOf("34700")
        ),
        answer(
            task = "who's covering the clinic while I'm at the summit?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "links", "read", "answer"),
            holds = listOf("Priya")
        ),
        answer(
            task = "what's still left on my summit to-do list?",
            kind = Kind.DOCUMENT,
            tools = listOf("tasks", "answer"),
            holds = listOf("certificate", "loupes")
        ),
        answer(
            task = "what did the X-ray generator repair cost?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            lacks = listOf("1180", "1450", "640")
        ),
        answer(
            task = "what's Clover's microchip number?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            lacks = listOf("985112004567321", "985112009904410")
        ),
        answer(
            task = "how much was my summit registration?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("189", "567")
        )
    )
}
