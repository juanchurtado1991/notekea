package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindTeacherAnswers {
    val all = listOf(
        AgentWideCase(
            task = "whats the average essay score in period 1?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("41.5")
        ),
        AgentWideCase(
            task = "who in period 1 got under 15 on quiz 1?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Jayden Morales", "Kevin Osei", "Tyrell Banks"),
            lacks = listOf("Diego Ramirez")
        ),
        AgentWideCase(
            task = "how much trip money have I actually collected from families so far, " +
                "after refunds?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("102")
        ),
        AgentWideCase(
            task = "what's the total cost of the museum trip?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("594")
        ),
        AgentWideCase(
            task = "how many days until the field trip?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("23 days")
        ),
        AgentWideCase(
            task = "how many days left till the APUSH exam?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("177 days")
        ),
        AgentWideCase(
            task = "what time do the kids eat lunch at the museum?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("11:15")
        ),
        AgentWideCase(
            task = "which period 3 kids still haven't retaken the unit 2 test?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Nia Robinson", "Isabella Cruz", "Samir Patel")
        ),
        AgentWideCase(
            task = "how many more PD hours do I need if only the approved ones count?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("17.5")
        ),
        AgentWideCase(
            task = "who still hasn't turned in a permission slip?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Kevin Osei", "Tyrell Banks", "Priya Raman")
        ),
        AgentWideCase(
            task = "how many service learning hours do my civics kids need?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("12 hours")
        ),
        AgentWideCase(
            task = "when is Jayden Price's DBQ actually due now?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("18")
        ),
        AgentWideCase(
            task = "which of my classes has the most students?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("World History")
        ),
        AgentWideCase(
            task = "who are the chaperones for the museum trip?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Victor Chen", "Marcus Price")
        ),
        AgentWideCase(
            task = "what's the max late penalty the curriculum guide allows?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("25")
        ),
        AgentWideCase(
            task = "what's the museum's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("555")
        ),
        AgentWideCase(
            task = "what did Aaliyah get on her unit 2 retake?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("78")
        ),
        AgentWideCase(
            task = "what time does the museum close?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("closes at")
        )
    )
}
