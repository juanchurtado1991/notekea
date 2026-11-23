package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the care home vault, three of them not answered by the notes. */
internal object BlindCareHomeAnswers {
    val all = listOf(
        answer(
            task = "how much have craft materials cost us from the activities budget so far?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("240")
        ),
        answer(
            task = "what have the outings cost this year, not counting the refunded one?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("1079")
        ),
        answer(
            task = "whats the total of the pending supplies orders?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("169")
        ),
        answer(
            task = "how much did we spend on supplies orders in september?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("84.5")
        ),
        answer(
            task = "how many volunteer hours did we get in october altogether?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("54")
        ),
        answer(
            task = "how many hours has Ruth done from august to october?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("60 hours")
        ),
        answer(
            task = "what was the average chair yoga attendance in october?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("9.75")
        ),
        answer(
            task = "how many days until the christmas market trip?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("24 days")
        ),
        answer(
            task = "how many days from Stanley moving in to his first family review?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            computed = listOf("29 days")
        ),
        answer(
            task = "what time is craft club and who runs it?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("14:30", "Gwen Hollis")
        ),
        answer(
            task = "which volunteer plays chess with Harold?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "search", "read", "answer"),
            holds = listOf("Tom Reyes")
        ),
        answer(
            task = "what song does Edith always ask for?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "links", "read", "answer"),
            holds = listOf("Abide With Me")
        ),
        answer(
            task = "where's memory cafe going to be from january?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("conservatory")
        ),
        answer(
            task = "what consent form do I need before putting a resident's photo " +
                "in the newsletter?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("PC-2")
        ),
        answer(
            task = "how long have I got to write up a safeguarding concern, and on which form?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("SG-3", "24 hours")
        ),
        answer(
            task = "how much is the christmas market trip going to cost?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            lacks = listOf("will cost £310", "is going to cost £310", "trip costs £310")
        ),
        answer(
            task = "what's Tom Ashby's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("reach him on", "his number is 0")
        ),
        answer(
            task = "when's Arthur's birthday?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("birthday is on", "was born on")
        )
    )
}
