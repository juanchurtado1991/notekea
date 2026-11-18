package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindAstronomyAnswers {
    val all = listOf(
        AgentWideCase(
            task = "how much in dues have we actually collected this year?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("295")
        ),
        AgentWideCase(
            task = "who still hasn't paid their dues?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Marisol Vega", "Carla Benitez", "Felix Moreno")
        ),
        AgentWideCase(
            task = "how much has the club paid out this year, not counting what the grant covered?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1218")
        ),
        AgentWideCase(
            task = "what did the meeting room cost us in total this year?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("480")
        ),
        AgentWideCase(
            task = "which line of the club budget are we over on so far?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            holds = listOf("Insurance")
        ),
        AgentWideCase(
            task = "how much of my own gear budget have I spent so far?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("216")
        ),
        AgentWideCase(
            task = "how many days since I last observed at Ortiz Ranch?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("18 days")
        ),
        AgentWideCase(
            task = "how many days until the club insurance renews?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("96 days")
        ),
        AgentWideCase(
            task = "what's the gate combo at the ranch now?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("4471"),
            lacks = listOf("combo is 3316", "combination is 3316")
        ),
        AgentWideCase(
            task = "what's the part number for the altitude bearing pads on my dob?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("CD-PTFE-22")
        ),
        AgentWideCase(
            task = "how long should I let my scope cool down before observing?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("45")
        ),
        AgentWideCase(
            task = "which objects did I miss in the messier marathon?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("M74", "M30")
        ),
        AgentWideCase(
            task = "what's my average seeing across my observing logs since september?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "read", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("3.6")
        ),
        AgentWideCase(
            task = "how many public visitors did the two fall star parties get altogether?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("127")
        ),
        AgentWideCase(
            task = "how many days after buying the dob did I get first light?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("14 days")
        ),
        AgentWideCase(
            task = "what's the focal length of Dave Lindqvist's refractor?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER
        ),
        AgentWideCase(
            task = "what was the SQM reading at the october star party?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("star party was 21.8", "star party was 19.4", "star party was 21.6")
        ),
        AgentWideCase(
            task = "what's Priya's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("is 520-555")
        )
    )
}
