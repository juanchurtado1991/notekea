package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions the gym owner's notes answer, and three they do not. */
internal object BlindGymOwnerAnswers {
    val all = listOf(
        AgentWideCase(
            task = "what's my net revenue for october after refunds?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("14019")
        ),
        AgentWideCase(
            task = "what were my operating expenses in october?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("12506")
        ),
        AgentWideCase(
            task = "what was the gym's operating profit in october, using revenue after refunds?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1513")
        ),
        AgentWideCase(
            task = "how much have i actually spent on equipment purchases this year?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("7246")
        ),
        AgentWideCase(
            task = "who do i contact for a warranty claim on the air bikes?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Kyle Brenner")
        ),
        AgentWideCase(
            task = "which active members are on the unlimited plan?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Priya Nandakumar", "Leah Whitford", "Olivia Brandt", "Grace Liang")
        ),
        AgentWideCase(
            task = "how much a month do my active unlimited members bring in together?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("740")
        ),
        AgentWideCase(
            task = "how soon does a member have to report an injury under the waiver?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("48 hours")
        ),
        AgentWideCase(
            task = "how early do coaches have to be in before their first class?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("20 minutes")
        ),
        AgentWideCase(
            task = "what time does athlete check-in open at the throwdown?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("7:15")
        ),
        AgentWideCase(
            task = "how many days until the throwdown?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("24 days")
        ),
        AgentWideCase(
            task = "how many days after marcus's freeze starts does it end?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("77 days")
        ),
        AgentWideCase(
            task = "how much sponsor cash have we actually received for the throwdown?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("2100")
        ),
        AgentWideCase(
            task = "how much is still owed to me on invoices?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("2045")
        ),
        AgentWideCase(
            task = "what did the fire inspector want fixed?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("extinguisher", "chalk")
        ),
        AgentWideCase(
            task = "which coach worked the most hours in october?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Ryan Okafor")
        ),
        AgentWideCase(
            task = "how much was the october water bill?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("water bill was \$612", "water bill was 612")
        ),
        AgentWideCase(
            task = "when does ryan okafor's L2 cert expire?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("level 2 expires on 2028-01-10", "l2 expires on 2028-01-10")
        ),
        AgentWideCase(
            task = "how many athletes have registered for the throwdown so far?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("60 athletes have registered", "60 have registered")
        )
    )
}
