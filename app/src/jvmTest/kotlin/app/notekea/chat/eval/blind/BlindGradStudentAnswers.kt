package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions on the Blind Grad Student vault; three of them the notes do not answer. */
internal object BlindGradStudentAnswers {
    val all = listOf(
        AgentWideCase(
            task = "what LT50 did Kowalski get for the four-arm plutei?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("25.8")
        ),
        AgentWideCase(
            task = "in the Arden paper how long did kelp take to recover where urchins stayed low?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("4 years")
        ),
        AgentWideCase(
            task = "how many degrees is my four-arm LT50 below Kowalski's prism-stage LT50?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            holds = listOf("24.3", "27.1"),
            computed = listOf("2.8")
        ),
        AgentWideCase(
            task = "how much of my lab budget have I actually paid so far?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1555"),
            lacks = listOf("1765", "1990")
        ),
        AgentWideCase(
            task = "what do my travel budget fieldwork lines add up to, minus the cancelled van?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1240"),
            lacks = listOf("1500")
        ),
        AgentWideCase(
            task = "avg O2 consumption at 20 degrees in my respirometry run?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("6.3")
        ),
        AgentWideCase(
            task = "avg kelp eaten per urchin in the grazing trial? leave out the one that died",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("7.75"),
            lacks = listOf("6.7")
        ),
        AgentWideCase(
            task = "what's my average fertilisation rate across all the spawnings?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("87.5")
        ),
        AgentWideCase(
            task = "how many days until my quals?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("28 days")
        ),
        AgentWideCase(
            task = "how many days from today until fieldwork starts?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("125")
        ),
        AgentWideCase(
            task = "how many days are left until the Hobart abstract deadline?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("24 days")
        ),
        AgentWideCase(
            task = "when does Marco go on sabbatical?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("2027-02-01", "Bergen")
        ),
        AgentWideCase(
            task = "what's the collection permit number for the field trip?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("CA-SR-2027-044")
        ),
        AgentWideCase(
            task = "what's the total target word count for the whole thesis?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("33000")
        ),
        AgentWideCase(
            task = "average lab report 2 grade in section 04? leave out Daniel, not submitted yet",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("74.5"),
            lacks = listOf("83.2")
        ),
        AgentWideCase(
            task = "what was the fertilisation rate on the 2026-10-26 spawning?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("76")
        ),
        AgentWideCase(
            task = "what's Priya's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("555 0142", "555 0187")
        ),
        AgentWideCase(
            task = "what LT50 did I get for the prism stage in my own thermal ramp?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("27.1")
        )
    )
}
