package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindYogaStudioAnswers {
    val all = listOf(
        AgentWideCase(
            task = "what was the average turnout at saturday slow flow in october?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("20.8")
        ),
        AgentWideCase(
            task = "how much did the studio spend on equipment in october after the strap refund?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1396"),
            lacks = listOf("1450")
        ),
        AgentWideCase(
            task = "what did the studio spend in october, not counting rent, net of the refund?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("4086"),
            lacks = listOf("4181", "4140", "4235")
        ),
        AgentWideCase(
            task = "how much have I got pending with suppliers?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1966")
        ),
        AgentWideCase(
            task = "how much in retreat deposits am I actually holding right now?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1700"),
            lacks = listOf("1950")
        ),
        AgentWideCase(
            task = "how many days till the retreat?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("121 days")
        ),
        AgentWideCase(
            task = "what will the rent go up to at the next review?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("6855.84")
        ),
        AgentWideCase(
            task = "who do I contact about the lease day to day?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Ingrid Vasquez")
        ),
        AgentWideCase(
            task = "can I hire the studio out to other teachers on weekends without asking?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("6 days")
        ),
        AgentWideCase(
            task = "how much did I pay the lodge as a deposit?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("2,400")
        ),
        AgentWideCase(
            task = "who's covering Priya's sunday class this weekend?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Jasper")
        ),
        AgentWideCase(
            task = "what's Mia Okafor's cut of the inversions workshop money paid so far?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("182"),
            lacks = listOf("234")
        ),
        AgentWideCase(
            task = "how much rent have I paid from august to november?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("26496")
        ),
        AgentWideCase(
            task = "how many days between the floor polishing and the inversions workshop?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("9 days")
        ),
        AgentWideCase(
            task = "which company is doing the minibus for the retreat?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Ridgeline Coaches")
        ),
        AgentWideCase(
            task = "what did the plumber charge me?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("280")
        ),
        AgentWideCase(
            task = "does Sophie have any dietary needs for the retreat?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("vegetarian", "gluten")
        ),
        AgentWideCase(
            task = "what's the excess on my studio insurance?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER
        )
    )
}
