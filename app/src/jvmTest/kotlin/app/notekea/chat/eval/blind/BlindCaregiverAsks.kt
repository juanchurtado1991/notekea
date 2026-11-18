package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests that must be asked back, then traps whose words settle them. Written blind. */
internal object BlindCaregiverAsks {
    val all = listOf(
        AgentWideCase(
            task = "add melatonin 3 mg at bedtime to the medications note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("dad", "mom")
        ),
        AgentWideCase(
            task = "push dad's appointment back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "put in Mike's note that he still has dad's spare house key",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Kowalski", "Harlan")
        ),
        AgentWideCase(
            task = "cancel mom's appointment, she's got a cold",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "add flu shot done 2026-11-10 to the health summary",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("dad", "mom")
        ),
        AgentWideCase(
            task = "change the aide's rate to $30 an hour",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Gloria", "Yvonne")
        ),
        AgentWideCase(
            task = "mark dad's pending claim as approved",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "log $300 from Mike today in the contributions, it's for groceries",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Kowalski", "Harlan")
        ),
        AgentWideCase(
            task = "add a task to Mike Harlan's note: return the transport wheelchair",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Mike Harlan", "wheelchair")
        ),
        AgentWideCase(
            task = "change the aide's tuesday hours to 10 to 2",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Yvonne", "Home Aides")
        ),
        AgentWideCase(
            task = "add a task to both medication notes: ask Owen about flu shot timing",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("flu shot", "Medications")
        ),
        AgentWideCase(
            task = "what time is mom's neurology appointment?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("15:45")
        ),
        AgentWideCase(
            task = "how much did Mike Kowalski chip in for october?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("400")
        )
    )
}
