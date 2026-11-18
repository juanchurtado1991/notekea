package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered (or honestly not answered) from the caregiver vault. Written blind. */
internal object BlindCaregiverAnswers {
    val all = listOf(
        AgentWideCase(
            task = "what dose of metoprolol is dad on now?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("25 mg")
        ),
        AgentWideCase(
            task = "how many days between mom's hip surgery and her december ortho check?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("110 days")
        ),
        AgentWideCase(
            task = "how many days has it been since mom fell?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("84 days")
        ),
        AgentWideCase(
            task = "what's the total billed on dad's pending insurance claims?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("2030")
        ),
        AgentWideCase(
            task = "add up our share on mom's approved claims",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("565")
        ),
        AgentWideCase(
            task = "whats the reference number for the lab claim appeal?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("R-77213")
        ),
        AgentWideCase(
            task = "how much do I owe Gloria for november so far?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("518")
        ),
        AgentWideCase(
            task = "how much does dad's account owe me back for november so far?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("221")
        ),
        AgentWideCase(
            task = "total of the bills paid in november, after the pharmacy refund?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("2186")
        ),
        AgentWideCase(
            task = "how much have the Harlans put in since september, " +
                "counting Mike's hearing aid money?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1100")
        ),
        AgentWideCase(
            task = "what did the pharmacy copays in the bills notes cost us in " +
                "october and november together, counting the refund?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("122")
        ),
        AgentWideCase(
            task = "who has financial power of attorney for dad?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Theresa")
        ),
        AgentWideCase(
            task = "which hospital should the ambulance take them to?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("St. Brendan")
        ),
        AgentWideCase(
            task = "is mom allergic to any meds?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("sulfa")
        ),
        AgentWideCase(
            task = "what's mom's average walking distance across her PT sessions?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("216")
        ),
        AgentWideCase(
            task = "who stayed with mom the day Gloria was sick?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Denise")
        ),
        AgentWideCase(
            task = "what's Dr. Okafor's office number?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("555-0150", "555-0167")
        ),
        AgentWideCase(
            task = "how much is the stairlift going to cost?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("1890")
        ),
        AgentWideCase(
            task = "what did mom score on her last MoCA?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("/30", "she scored a")
        )
    )
}
