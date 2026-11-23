package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions the Blind Film Student notes answer, or do not. */
internal object BlindFilmStudentAnswers {
    val all = listOf(
        answer(
            task = "how much do I still owe on the rentals?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("335")
        ),
        answer(
            task = "total rental cost if I don't count the dolly since it got refunded?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("1625")
        ),
        answer(
            task = "am I over the rentals line in the production budget? by how much",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            computed = listOf("125")
        ),
        answer(
            task = "how much am I paying in entry fees for the festivals I'm actually entering?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("130")
        ),
        answer(
            task = "how many days do I have left until the grant deadline?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("34 days")
        ),
        answer(
            task = "how many days from our first shoot day to the Night Owl deadline?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            computed = listOf("56 days")
        ),
        answer(
            task = "where does the crew park on the diner days?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("church lot")
        ),
        answer(
            task = "how much is crew parking at the diner going to cost me in total?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            computed = listOf("144")
        ),
        answer(
            task = "does Bea have any food restrictions?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            holds = listOf("vegan")
        ),
        answer(
            task = "what's the radio station's call sign in draft 3?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("KSLT")
        ),
        answer(
            task = "where did I say I'd do the free community screening in the grant app?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Westport Public Library")
        ),
        answer(
            task = "who's my fiscal sponsor on the grant?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Harborlight Arts Collective")
        ),
        answer(
            task = "what's the average day rate of my DP, sound mixer and gaffer?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "calculate", "answer"),
            computed = listOf("210")
        ),
        answer(
            task = "what's still open on my wrap checklist?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "tasks", "answer"),
            holds = listOf("Sound Cave", "thank-you cards", "diner keys")
        ),
        answer(
            task = "how much is Dev charging for the edit?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("750")
        ),
        answer(
            task = "what's Taylor Brooks' email?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("@")
        ),
        answer(
            task = "what's Mrs. Liang's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("555-0")
        ),
        answer(
            task = "what's Lumen's late fee if we bring the gear back late?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("lumen charges $25", "lumen's late fee is $25")
        )
    )
}
