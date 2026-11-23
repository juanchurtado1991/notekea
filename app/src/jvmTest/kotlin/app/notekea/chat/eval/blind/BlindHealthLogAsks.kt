package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindHealthLogAsks {
    val all = listOf(
        // Asks: the words name one thing and the vault has several that fit equally.
        ask(
            task = "add sunscreen to my checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Race", "Gear")
        ),
        ask(
            task = "what's on my weekly plan for thursday?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Training", "Nutrition")
        ),
        ask(
            task = "what time is my appointment with sarah next week?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "ask_user"),
            holds = listOf("Whitlock", "Delgado")
        ),
        ask(
            task = "add a task to call mike about saturday's long run",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Brenner", "Osei")
        ),
        ask(
            task = "what did mike say about my cadence?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "ask_user"),
            holds = listOf("Brenner", "Osei")
        ),
        ask(
            task = "move my doctor appointment to friday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Delgado", "Petrov")
        ),
        ask(
            task = "move my physio appointment to 8:00 instead of 7:30",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("2026-11-13", "2026-11-20")
        ),
        ask(
            task = "cancel my pending order",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("4471", "4503")
        ),
        // Traps: they look ambiguous, but the words settle them.
        change(
            task = "add sunscreen to my race-day checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Race", "sunscreen")
        ),
        answer(
            task = "what's dinner on thursday in my weekly plan?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("salmon")
        ),
        answer(
            task = "when do I see sarah delgado next?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("9:15")
        ),
        answer(
            task = "when's my next physio session?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("7:30", "13")
        ),
        answer(
            task = "how much did I pay for my trail shoes?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("145")
        ),
        change(
            task = "add a task to both mikes' notes: send them the spring race calendar",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "add_task", "propose"),
            holds = listOf("Brenner", "Osei", "race calendar")
        )
    )
}
