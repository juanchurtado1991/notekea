package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindFounderAnswers {
    val all = listOf(
        AgentWideCase(
            task = "how much is marked Committed in the investor pipeline, not counting caliche?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("550")
        ),
        AgentWideCase(
            task = "by how many dollars did MRR grow from june to october?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("10180")
        ),
        AgentWideCase(
            task = "what was our average monthly MRR churn in Q3?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1.5")
        ),
        AgentWideCase(
            task = "what was gross burn in october if we leave out the one-off legal?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("123")
        ),
        AgentWideCase(
            task = "how many days between the october board meeting and the term sheet deadline?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("36 days")
        ),
        AgentWideCase(
            task = "how many days left until our target close for the seed?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("37 days")
        ),
        AgentWideCase(
            task = "what does lonestar need from us before they'll renew?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("EDI")
        ),
        AgentWideCase(
            task = "how big will the option pool be after the round?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("12%")
        ),
        AgentWideCase(
            task = "how long is the no-shop in the caliche term sheet?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("45")
        ),
        AgentWideCase(
            task = "what per-site price did we settle on at the august board meeting?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("1150")
        ),
        AgentWideCase(
            task = "what's the salary band for the senior backend hire?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("165", "185")
        ),
        AgentWideCase(
            task = "which customer pays us the most per month?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Hill Country Cold Chain")
        ),
        AgentWideCase(
            task = "which customer invoices are overdue right now?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "date", "answer"),
            ends = End.ANSWER,
            holds = listOf("INV-1059")
        ),
        AgentWideCase(
            task = "how much are customers still owing us in total?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("17900")
        ),
        AgentWideCase(
            task = "when is the CSM supposed to start?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Q2 2027")
        ),
        AgentWideCase(
            task = "what pre-money did mesquite propose?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER
        ),
        AgentWideCase(
            task = "how many trucks does pecan street run?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER
        ),
        AgentWideCase(
            task = "what salary did ben achebe ask for?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER
        )
    )
}
