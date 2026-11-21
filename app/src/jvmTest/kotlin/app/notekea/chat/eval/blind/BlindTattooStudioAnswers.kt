package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the tattoo studio vault, three the notes cannot answer. */
internal object BlindTattooStudioAnswers {
    val all = listOf(
        AgentWideCase(
            task = "when's jonas becker's next session?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("10:30")
        ),
        AgentWideCase(
            task = "how much deposit money am i still waiting on?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("320")
        ),
        AgentWideCase(
            task = "what do the deposits marked paid add up to? refunds don't count",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("505")
        ),
        AgentWideCase(
            task = "how many days from today until the re-inspection?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("69 days")
        ),
        AgentWideCase(
            task = "how many days between aylin's session and her aftercare check-in?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("25 days")
        ),
        AgentWideCase(
            task = "how much does rosa take home from nadia's moth piece?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("540")
        ),
        AgentWideCase(
            task = "what split did i agree with kenji for his december spot?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("55")
        ),
        AgentWideCase(
            task = "who still needs an aftercare check-in?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "tasks", "answer"),
            ends = End.ANSWER,
            holds = listOf("Aylin", "Priya", "Felix")
        ),
        AgentWideCase(
            task = "which inks are below their reorder level?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Grey Wash", "Lumen White")
        ),
        AgentWideCase(
            task = "what's the average hourly rate of rosa kenji and dario?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("145")
        ),
        AgentWideCase(
            task = "how long before a session can a client move it without losing the deposit?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("72 hours")
        ),
        AgentWideCase(
            task = "what did the inspector flag this year?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("sharps container")
        ),
        AgentWideCase(
            task = "what did i spend on needles since september, not counting the box i sent back?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("105")
        ),
        AgentWideCase(
            task = "anything i need to know about clara's skin before the cover-up?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("red pigment")
        ),
        AgentWideCase(
            task = "how many days till rosa's guest spot starts?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("12 days")
        ),
        AgentWideCase(
            task = "what's jonas wolf's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("his number is", "his phone number is")
        ),
        AgentWideCase(
            task = "how much is the studio rent each month?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("rent is €", "the rent is €")
        ),
        AgentWideCase(
            task = "which needle did i use for aylin's script?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf(
                "used a 1203",
                "used the 1203",
                "used a 1005",
                "used the 1005",
                "used a 1207",
                "used the 1207"
            )
        )
    )
}
