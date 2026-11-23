package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindTeacherAnswers {
    val all = listOf(
        answer(
            task = "whats the average essay score in period 1?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("41.5")
        ),
        answer(
            task = "who in period 1 got under 15 on quiz 1?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            holds = listOf("Jayden Morales", "Kevin Osei", "Tyrell Banks"),
            lacks = listOf("Diego Ramirez")
        ),
        answer(
            task = "how much trip money have I actually collected from families so far, " +
                "after refunds?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("102")
        ),
        answer(
            task = "what's the total cost of the museum trip?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("594")
        ),
        answer(
            task = "how many days until the field trip?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            computed = listOf("23 days")
        ),
        answer(
            task = "how many days left till the APUSH exam?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("177 days")
        ),
        answer(
            task = "what time do the kids eat lunch at the museum?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("11:15")
        ),
        answer(
            task = "which period 3 kids still haven't retaken the unit 2 test?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            holds = listOf("Nia Robinson", "Isabella Cruz", "Samir Patel")
        ),
        answer(
            task = "how many more PD hours do I need if only the approved ones count?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("17.5")
        ),
        answer(
            task = "who still hasn't turned in a permission slip?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            holds = listOf("Kevin Osei", "Tyrell Banks", "Priya Raman")
        ),
        answer(
            task = "how many service learning hours do my civics kids need?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("12 hours")
        ),
        answer(
            task = "when is Jayden Price's DBQ actually due now?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("18")
        ),
        answer(
            task = "which of my classes has the most students?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "read", "read", "answer"),
            holds = listOf("World History")
        ),
        answer(
            task = "who are the chaperones for the museum trip?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "search", "read", "answer"),
            holds = listOf("Victor Chen", "Marcus Price")
        ),
        answer(
            task = "what's the max late penalty the curriculum guide allows?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            holds = listOf("25")
        ),
        answer(
            task = "what's the museum's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("555")
        ),
        answer(
            task = "what did Aaliyah get on her unit 2 retake?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("78")
        ),
        answer(
            task = "what time does the museum close?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("closes at")
        )
    )
}
