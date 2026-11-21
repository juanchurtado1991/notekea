package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindHealthLogAsks {
    val all = listOf(
        // Asks: the words name one thing and the vault has several that fit equally.
        AgentWideCase(
            task = "add sunscreen to my checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Race", "Gear")
        ),
        AgentWideCase(
            task = "what's on my weekly plan for thursday?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Training", "Nutrition")
        ),
        AgentWideCase(
            task = "what time is my appointment with sarah next week?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Whitlock", "Delgado")
        ),
        AgentWideCase(
            task = "add a task to call mike about saturday's long run",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Brenner", "Osei")
        ),
        AgentWideCase(
            task = "what did mike say about my cadence?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Brenner", "Osei")
        ),
        AgentWideCase(
            task = "move my doctor appointment to friday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Delgado", "Petrov")
        ),
        AgentWideCase(
            task = "move my physio appointment to 8:00 instead of 7:30",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("2026-11-13", "2026-11-20")
        ),
        AgentWideCase(
            task = "cancel my pending order",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("4471", "4503")
        ),
        // Traps: they look ambiguous, but the words settle them.
        AgentWideCase(
            task = "add sunscreen to my race-day checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Race", "sunscreen")
        ),
        AgentWideCase(
            task = "what's dinner on thursday in my weekly plan?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("salmon")
        ),
        AgentWideCase(
            task = "when do I see sarah delgado next?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("9:15")
        ),
        AgentWideCase(
            task = "when's my next physio session?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("7:30", "13")
        ),
        AgentWideCase(
            task = "how much did I pay for my trail shoes?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("145")
        ),
        AgentWideCase(
            task = "add a task to both mikes' notes: send them the spring race calendar",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Brenner", "Osei", "race calendar")
        )
    )
}
