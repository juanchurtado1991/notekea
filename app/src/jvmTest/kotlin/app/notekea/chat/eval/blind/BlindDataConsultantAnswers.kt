package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions on the Blind Data Consultant vault; three of them the notes do not answer. */
internal object BlindDataConsultantAnswers {
    val all = listOf(
        answer(
            task = "how much do my clients owe me right now in total?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "calculate", "answer"),
            computed = listOf("12460")
        ),
        answer(
            task = "how many hours did I do for Müller Kliniken in weeks 43 to 45?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "read", "calculate", "answer"),
            computed = listOf("30.5")
        ),
        answer(
            task = "how many days until the M3 milestone for Müller Logistik?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            holds = listOf("Production pipeline"),
            computed = listOf("23 days")
        ),
        answer(
            task = "how many days left until my Copenhagen talk?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("99 days")
        ),
        answer(
            task = "where am I allowed to process the Kliniken patient data?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            holds = listOf("KLX-04")
        ),
        answer(
            task = "what mileage rate do I get if I drive to the Hamburg depot?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("0.30")
        ),
        answer(
            task = "what time is my talk slot?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("11:40")
        ),
        answer(
            task = "who's my contact at Nordlicht from December?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "links", "read", "answer"),
            holds = listOf("Ilka Brandauer")
        ),
        answer(
            task = "what did I actually spend on business expenses in october?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("370"),
            lacks = listOf("434.5")
        ),
        answer(
            task = "how much income tax prepayment have I paid so far this year?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("7050"),
            lacks = listOf("9400")
        ),
        answer(
            task = "how much has Müller Logistik paid me so far?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("17280"),
            lacks = listOf("21600")
        ),
        answer(
            task = "which dataset am I using for the demo in my talk?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Rostock")
        ),
        answer(
            task = "which book did I rate highest?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            holds = listOf("Pipelines Without Tears")
        ),
        answer(
            task = "how much did I bill Nordlicht for week 45, before VAT?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            computed = listOf("760"),
            lacks = listOf("904.4")
        ),
        answer(
            task = "how many days did Logistik take to pay ML-2026-08?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            computed = listOf("12 days")
        ),
        answer(
            task = "what's Hannah's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("23846610", "55210874")
        ),
        answer(
            task = "when did Müller Kliniken pay MK-2026-03?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            lacks = listOf("paid on 2026-09-30")
        ),
        answer(
            task = "how many people came to my first talk rehearsal?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("41 people")
        )
    )
}
