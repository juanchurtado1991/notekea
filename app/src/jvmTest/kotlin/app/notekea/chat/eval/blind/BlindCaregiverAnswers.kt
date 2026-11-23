package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered (or honestly not answered) from the caregiver vault. Written blind. */
internal object BlindCaregiverAnswers {
    val all = listOf(
        answer(
            task = "what dose of metoprolol is dad on now?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("25 mg")
        ),
        answer(
            task = "how many days between mom's hip surgery and her december ortho check?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "date", "answer"),
            computed = listOf("110 days")
        ),
        answer(
            task = "how many days has it been since mom fell?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("84 days")
        ),
        answer(
            task = "what's the total billed on dad's pending insurance claims?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("2030")
        ),
        answer(
            task = "add up our share on mom's approved claims",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("565")
        ),
        answer(
            task = "whats the reference number for the lab claim appeal?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("R-77213")
        ),
        answer(
            task = "how much do I owe Gloria for november so far?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            computed = listOf("518")
        ),
        answer(
            task = "how much does dad's account owe me back for november so far?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("221")
        ),
        answer(
            task = "total of the bills paid in november, after the pharmacy refund?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("2186")
        ),
        answer(
            task = "how much have the Harlans put in since september, " +
                "counting Mike's hearing aid money?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("1100")
        ),
        answer(
            task = "what did the pharmacy copays in the bills notes cost us in " +
                "october and november together, counting the refund?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            computed = listOf("122")
        ),
        answer(
            task = "who has financial power of attorney for dad?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Theresa")
        ),
        answer(
            task = "which hospital should the ambulance take them to?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("St. Brendan")
        ),
        answer(
            task = "is mom allergic to any meds?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("sulfa")
        ),
        answer(
            task = "what's mom's average walking distance across her PT sessions?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("216")
        ),
        answer(
            task = "who stayed with mom the day Gloria was sick?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Denise")
        ),
        answer(
            task = "what's Dr. Okafor's office number?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("555-0150", "555-0167")
        ),
        answer(
            task = "how much is the stairlift going to cost?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("1890")
        ),
        answer(
            task = "what did mom score on her last MoCA?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("/30", "she scored a")
        )
    )
}
