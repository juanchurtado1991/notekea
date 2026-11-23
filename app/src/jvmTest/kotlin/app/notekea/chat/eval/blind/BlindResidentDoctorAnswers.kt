package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions for the resident doctor vault, answered from the notes. */
internal object BlindResidentDoctorAnswers {
    val all = listOf(
        answer(
            task = "how many days till step 3 day 1?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("71 days")
        ),
        answer(
            task = "whats the average of my timed practice forms",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("66"),
            lacks = listOf("average is 68")
        ),
        answer(
            task = "when do I present my abstract?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("14:30")
        ),
        answer(
            task = "how much do I owe on my federal loans in total right now?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("204295"),
            lacks = listOf("202580")
        ),
        answer(
            task = "how many more qualifying payments do I need for PSLF?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("89")
        ),
        answer(
            task = "what time is weekend sign-out on CCU?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("06:30"),
            lacks = listOf("weekend sign-out is at 07:00")
        ),
        answer(
            task = "how many hours of call do I have left this month?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "calculate", "answer"),
            computed = listOf("72")
        ),
        answer(
            task = "how many days from when I submitted the abstract to my poster session?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            computed = listOf("81 days")
        ),
        answer(
            task = "what do my monthly expenses add up to, not counting loan payments?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("2339"),
            lacks = listOf("2836")
        ),
        answer(
            task = "what did Daniel Kim say I need to work on?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "links", "read", "answer"),
            holds = listOf("commit")
        ),
        answer(
            task = "how many percentile points did I go up on the ITE vs last year",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("35")
        ),
        answer(
            task = "how much moonlighting money am I still owed?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("2420")
        ),
        answer(
            task = "what grew in the blood cultures in my endocarditis case?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("faecalis")
        ),
        answer(
            task = "how much can I get from the travel grant?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("1500")
        ),
        answer(
            task = "how many CCS cases was I aiming to do before the exam?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("36")
        ),
        answer(
            task = "what was my step 3 score?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("your step 3 score was", "you passed step 3")
        ),
        answer(
            task = "what time is my return flight from chicago?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("return flight is at 08:15", "return flight leaves at 08:15")
        ),
        answer(
            task = "what was the patient's name in the DKA case?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("the patient's name is", "the patient's name was")
        )
    )
}
