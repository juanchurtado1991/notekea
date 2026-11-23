package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests that must be asked back, then traps whose words settle them. Written blind. */
internal object BlindCaregiverAsks {
    val all = listOf(
        ask(
            task = "add melatonin 3 mg at bedtime to the medications note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("dad", "mom")
        ),
        ask(
            task = "push dad's appointment back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user")
        ),
        ask(
            task = "put in Mike's note that he still has dad's spare house key",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Kowalski", "Harlan")
        ),
        ask(
            task = "cancel mom's appointment, she's got a cold",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user")
        ),
        ask(
            task = "add flu shot done 2026-11-10 to the health summary",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("dad", "mom")
        ),
        ask(
            task = "change the aide's rate to $30 an hour",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Gloria", "Yvonne")
        ),
        ask(
            task = "mark dad's pending claim as approved",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user")
        ),
        ask(
            task = "log $300 from Mike today in the contributions, it's for groceries",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Kowalski", "Harlan")
        ),
        change(
            task = "add a task to Mike Harlan's note: return the transport wheelchair",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Mike Harlan", "wheelchair")
        ),
        change(
            task = "change the aide's tuesday hours to 10 to 2",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Yvonne", "Home Aides")
        ),
        change(
            task = "add a task to both medication notes: ask Owen about flu shot timing",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "add_task", "propose"),
            holds = listOf("flu shot", "Medications")
        ),
        answer(
            task = "what time is mom's neurology appointment?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("15:45")
        ),
        answer(
            task = "how much did Mike Kowalski chip in for october?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("400")
        )
    )
}
