package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindApplicantAsks {
    val all = listOf(
        ask(
            task = "add 'ask Dad to proofread' to my checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Application", "Scholarship")
        ),
        ask(
            task = "add a \$60 line for train tickets to my budget",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "ask_user"),
            holds = listOf("Visit", "Financial Aid")
        ),
        ask(
            task = "delete the parking line from my budget",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "ask_user"),
            holds = listOf("Visit", "Financial Aid")
        ),
        ask(
            task = "move my meeting with Ms. Lee to 15:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "ask_user"),
            holds = listOf("Grace Lee", "Hannah Lee")
        ),
        ask(
            task = "add to Ms. Lee's note that she's out sick next week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Grace Lee", "Hannah Lee")
        ),
        ask(
            task = "push the interview to 17:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Northbridge", "Harbor Bay")
        ),
        ask(
            task = "change the campus visit time to 10:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Evergreen Ridge", "Olympic Coast", "Sound Valley")
        ),
        ask(
            task = "mark the why-us supplement as second draft",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Olympic Coast", "Evergreen Ridge")
        ),
        change(
            task = "add 'request final transcript' to my applications checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Application", "request final transcript")
        ),
        change(
            task = "move the olympic coast interview to 19:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Olympic Coast", "19:00")
        ),
        answer(
            task = "how much is parking in both my budgets together?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            computed = listOf("252")
        ),
        change(
            task = "move my session with hannah lee to 18:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Hannah Lee", "18:00"),
            lacks = listOf("grace lee")
        ),
        answer(
            task = "what did ms lee, my counselor at lakeview, say about ED?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("net price calculator"),
            lacks = listOf("41%")
        )
    )
}
