package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions for the resident doctor vault, answered from the notes. */
internal object BlindResidentDoctorAnswers {
    val all = listOf(
        AgentWideCase(
            task = "how many days till step 3 day 1?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("71 days")
        ),
        AgentWideCase(
            task = "whats the average of my timed practice forms",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("66"),
            lacks = listOf("average is 68")
        ),
        AgentWideCase(
            task = "when do I present my abstract?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("14:30")
        ),
        AgentWideCase(
            task = "how much do I owe on my federal loans in total right now?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("204295"),
            lacks = listOf("202580")
        ),
        AgentWideCase(
            task = "how many more qualifying payments do I need for PSLF?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("89")
        ),
        AgentWideCase(
            task = "what time is weekend sign-out on CCU?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("06:30"),
            lacks = listOf("weekend sign-out is at 07:00")
        ),
        AgentWideCase(
            task = "how many hours of call do I have left this month?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("72")
        ),
        AgentWideCase(
            task = "how many days from when I submitted the abstract to my poster session?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("81 days")
        ),
        AgentWideCase(
            task = "what do my monthly expenses add up to, not counting loan payments?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("2339"),
            lacks = listOf("2836")
        ),
        AgentWideCase(
            task = "what did Daniel Kim say I need to work on?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("commit")
        ),
        AgentWideCase(
            task = "how many percentile points did I go up on the ITE vs last year",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("35")
        ),
        AgentWideCase(
            task = "how much moonlighting money am I still owed?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("2420")
        ),
        AgentWideCase(
            task = "what grew in the blood cultures in my endocarditis case?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("faecalis")
        ),
        AgentWideCase(
            task = "how much can I get from the travel grant?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("1500")
        ),
        AgentWideCase(
            task = "how many CCS cases was I aiming to do before the exam?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("36")
        ),
        AgentWideCase(
            task = "what was my step 3 score?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("your step 3 score was", "you passed step 3")
        ),
        AgentWideCase(
            task = "what time is my return flight from chicago?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("return flight is at 08:15", "return flight leaves at 08:15")
        ),
        AgentWideCase(
            task = "what was the patient's name in the DKA case?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("the patient's name is", "the patient's name was")
        )
    )
}
