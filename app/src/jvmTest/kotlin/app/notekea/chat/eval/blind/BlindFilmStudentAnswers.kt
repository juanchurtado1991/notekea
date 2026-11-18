package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions the Blind Film Student notes answer, or do not. */
internal object BlindFilmStudentAnswers {
    val all = listOf(
        AgentWideCase(
            task = "how much do I still owe on the rentals?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("335")
        ),
        AgentWideCase(
            task = "total rental cost if I don't count the dolly since it got refunded?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1625")
        ),
        AgentWideCase(
            task = "am I over the rentals line in the production budget? by how much",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("125")
        ),
        AgentWideCase(
            task = "how much am I paying in entry fees for the festivals I'm actually entering?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("130")
        ),
        AgentWideCase(
            task = "how many days do I have left until the grant deadline?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("34 days")
        ),
        AgentWideCase(
            task = "how many days from our first shoot day to the Night Owl deadline?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("56 days")
        ),
        AgentWideCase(
            task = "where does the crew park on the diner days?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("church lot")
        ),
        AgentWideCase(
            task = "how much is crew parking at the diner going to cost me in total?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("144")
        ),
        AgentWideCase(
            task = "does Bea have any food restrictions?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("vegan")
        ),
        AgentWideCase(
            task = "what's the radio station's call sign in draft 3?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("KSLT")
        ),
        AgentWideCase(
            task = "where did I say I'd do the free community screening in the grant app?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Westport Public Library")
        ),
        AgentWideCase(
            task = "who's my fiscal sponsor on the grant?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Harborlight Arts Collective")
        ),
        AgentWideCase(
            task = "what's the average day rate of my DP, sound mixer and gaffer?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("210")
        ),
        AgentWideCase(
            task = "what's still open on my wrap checklist?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "tasks", "answer"),
            ends = End.ANSWER,
            holds = listOf("Sound Cave", "thank-you cards", "diner keys")
        ),
        AgentWideCase(
            task = "how much is Dev charging for the edit?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("750")
        ),
        AgentWideCase(
            task = "what's Taylor Brooks' email?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("@")
        ),
        AgentWideCase(
            task = "what's Mrs. Liang's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("555-0")
        ),
        AgentWideCase(
            task = "what's Lumen's late fee if we bring the gear back late?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("lumen charges $25", "lumen's late fee is $25")
        )
    )
}
