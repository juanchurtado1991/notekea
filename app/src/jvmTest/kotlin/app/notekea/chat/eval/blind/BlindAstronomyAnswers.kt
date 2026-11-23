package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindAstronomyAnswers {
    val all = listOf(
        answer(
            task = "how much in dues have we actually collected this year?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("295")
        ),
        answer(
            task = "who still hasn't paid their dues?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            holds = listOf("Marisol Vega", "Carla Benitez", "Felix Moreno")
        ),
        answer(
            task = "how much has the club paid out this year, not counting what the grant covered?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("1218")
        ),
        answer(
            task = "what did the meeting room cost us in total this year?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("480")
        ),
        answer(
            task = "which line of the club budget are we over on so far?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            holds = listOf("Insurance")
        ),
        answer(
            task = "how much of my own gear budget have I spent so far?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("216")
        ),
        answer(
            task = "how many days since I last observed at Ortiz Ranch?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "date", "answer"),
            computed = listOf("18 days")
        ),
        answer(
            task = "how many days until the club insurance renews?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("96 days")
        ),
        answer(
            task = "what's the gate combo at the ranch now?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            holds = listOf("4471"),
            lacks = listOf("combo is 3316", "combination is 3316")
        ),
        answer(
            task = "what's the part number for the altitude bearing pads on my dob?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("CD-PTFE-22")
        ),
        answer(
            task = "how long should I let my scope cool down before observing?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("45")
        ),
        answer(
            task = "which objects did I miss in the messier marathon?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            holds = listOf("M74", "M30")
        ),
        answer(
            task = "what's my average seeing across my observing logs since september?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "read", "read", "read", "calculate", "answer"),
            computed = listOf("3.6")
        ),
        answer(
            task = "how many public visitors did the two fall star parties get altogether?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            computed = listOf("127")
        ),
        answer(
            task = "how many days after buying the dob did I get first light?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            computed = listOf("14 days")
        ),
        answer(
            task = "what's the focal length of Dave Lindqvist's refractor?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer")
        ),
        answer(
            task = "what was the SQM reading at the october star party?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "answer"),
            lacks = listOf("star party was 21.8", "star party was 19.4", "star party was 21.6")
        ),
        answer(
            task = "what's Priya's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            lacks = listOf("is 520-555")
        )
    )
}
