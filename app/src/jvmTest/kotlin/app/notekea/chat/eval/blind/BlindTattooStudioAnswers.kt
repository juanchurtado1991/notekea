package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the tattoo studio vault, three the notes cannot answer. */
internal object BlindTattooStudioAnswers {
    val all = listOf(
        answer(
            task = "when's jonas becker's next session?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("10:30")
        ),
        answer(
            task = "how much deposit money am i still waiting on?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("320")
        ),
        answer(
            task = "what do the deposits marked paid add up to? refunds don't count",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("505")
        ),
        answer(
            task = "how many days from today until the re-inspection?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("69 days")
        ),
        answer(
            task = "how many days between aylin's session and her aftercare check-in?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            computed = listOf("25 days")
        ),
        answer(
            task = "how much does rosa take home from nadia's moth piece?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            computed = listOf("540")
        ),
        answer(
            task = "what split did i agree with kenji for his december spot?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "search", "read", "answer"),
            holds = listOf("55")
        ),
        answer(
            task = "who still needs an aftercare check-in?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "tasks", "answer"),
            holds = listOf("Aylin", "Priya", "Felix")
        ),
        answer(
            task = "which inks are below their reorder level?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            holds = listOf("Grey Wash", "Lumen White")
        ),
        answer(
            task = "what's the average hourly rate of rosa kenji and dario?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "read", "calculate", "answer"),
            computed = listOf("145")
        ),
        answer(
            task = "how long before a session can a client move it without losing the deposit?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            holds = listOf("72 hours")
        ),
        answer(
            task = "what did the inspector flag this year?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            holds = listOf("sharps container")
        ),
        answer(
            task = "what did i spend on needles since september, not counting the box i sent back?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("105")
        ),
        answer(
            task = "anything i need to know about clara's skin before the cover-up?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "search", "read", "answer"),
            holds = listOf("red pigment")
        ),
        answer(
            task = "how many days till rosa's guest spot starts?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            computed = listOf("12 days")
        ),
        answer(
            task = "what's jonas wolf's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            lacks = listOf("his number is", "his phone number is")
        ),
        answer(
            task = "how much is the studio rent each month?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            lacks = listOf("rent is €", "the rent is €")
        ),
        answer(
            task = "which needle did i use for aylin's script?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "answer"),
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
