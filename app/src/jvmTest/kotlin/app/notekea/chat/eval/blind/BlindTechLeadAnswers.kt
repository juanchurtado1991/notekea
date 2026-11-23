package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindTechLeadAnswers {
    val all = listOf(
        answer(
            task = "how much have we actually paid for conferences this year?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("3940"),
            lacks = listOf("4890", "5530")
        ),
        answer(
            task = "how much of the team budget is left if I only count what's been paid?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("15020"),
            lacks = listOf("10220")
        ),
        answer(
            task = "how many points did we complete in total over the last 3 finished sprints?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "calculate", "answer"),
            computed = listOf("93"),
            lacks = listOf("84")
        ),
        answer(
            task = "how many days till payouts v2 goes GA?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("96")
        ),
        answer(
            task = "how many days from today to the ledger cutover?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("76"),
            lacks = listOf("62")
        ),
        answer(
            task = "how many duplicate payouts did we end up with in the september incident?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("412")
        ),
        answer(
            task = "what's the rollback window for the ledger cutover?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("72 hours")
        ),
        answer(
            task = "what TTL did we settle on for idempotency keys?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("7 days")
        ),
        answer(
            task = "why is the fraud rules engine paused and when do we look at it again?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("Sentinax", "2027-01-15")
        ),
        answer(
            task = "total incident time this quarter, in minutes?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "calculate", "answer"),
            computed = listOf("65"),
            lacks = listOf("220")
        ),
        answer(
            task = "which postmortem action items are still open across all incidents?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "tasks", "answer"),
            holds = listOf("TTL", "detector", "replay", "queue depth", "jitter", "latency"),
            lacks = listOf("purge")
        ),
        answer(
            task = "which of the books I finished did I rate higher?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Ledgers at Scale")
        ),
        answer(
            task = "when would Amira start if she takes the offer?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("2027-01-11")
        ),
        answer(
            task = "what got carried over from sprint 46?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("PAY-388", "LED-201")
        ),
        answer(
            task = "when does Elena want the headcount case?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("2026-11-21")
        ),
        answer(
            task = "what signing bonus did we offer Amira?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("21300")
        ),
        answer(
            task = "how much did we pay Swarmline in 2025?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "read", "answer"),
            lacks = listOf("4100")
        ),
        answer(
            task = "what caused the card auth timeout?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("connection pool")
        )
    )
}
