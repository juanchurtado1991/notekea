package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions the gym owner's notes answer, and three they do not. */
internal object BlindGymOwnerAnswers {
    val all = listOf(
        answer(
            task = "what's my net revenue for october after refunds?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("14019")
        ),
        answer(
            task = "what were my operating expenses in october?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("12506")
        ),
        answer(
            task = "what was the gym's operating profit in october, using revenue after refunds?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            computed = listOf("1513")
        ),
        answer(
            task = "how much have i actually spent on equipment purchases this year?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("7246")
        ),
        answer(
            task = "who do i contact for a warranty claim on the air bikes?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Kyle Brenner")
        ),
        answer(
            task = "which active members are on the unlimited plan?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            holds = listOf("Priya Nandakumar", "Leah Whitford", "Olivia Brandt", "Grace Liang")
        ),
        answer(
            task = "how much a month do my active unlimited members bring in together?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            computed = listOf("740")
        ),
        answer(
            task = "how soon does a member have to report an injury under the waiver?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            holds = listOf("48 hours")
        ),
        answer(
            task = "how early do coaches have to be in before their first class?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            holds = listOf("20 minutes")
        ),
        answer(
            task = "what time does athlete check-in open at the throwdown?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            holds = listOf("7:15")
        ),
        answer(
            task = "how many days until the throwdown?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            computed = listOf("24 days")
        ),
        answer(
            task = "how many days after marcus's freeze starts does it end?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            computed = listOf("77 days")
        ),
        answer(
            task = "how much sponsor cash have we actually received for the throwdown?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("2100")
        ),
        answer(
            task = "how much is still owed to me on invoices?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("2045")
        ),
        answer(
            task = "what did the fire inspector want fixed?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("extinguisher", "chalk")
        ),
        answer(
            task = "which coach worked the most hours in october?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            holds = listOf("Ryan Okafor")
        ),
        answer(
            task = "how much was the october water bill?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            lacks = listOf("water bill was \$612", "water bill was 612")
        ),
        answer(
            task = "when does ryan okafor's L2 cert expire?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "answer"),
            lacks = listOf("level 2 expires on 2028-01-10", "l2 expires on 2028-01-10")
        ),
        answer(
            task = "how many athletes have registered for the throwdown so far?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("60 athletes have registered", "60 have registered")
        )
    )
}
