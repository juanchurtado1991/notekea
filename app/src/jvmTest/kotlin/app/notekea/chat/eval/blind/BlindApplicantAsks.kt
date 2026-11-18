package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindApplicantAsks {
    val all = listOf(
        AgentWideCase(
            task = "add 'ask Dad to proofread' to my checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Application", "Scholarship")
        ),
        AgentWideCase(
            task = "add a \$60 line for train tickets to my budget",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Visit", "Financial Aid")
        ),
        AgentWideCase(
            task = "delete the parking line from my budget",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Visit", "Financial Aid")
        ),
        AgentWideCase(
            task = "move my meeting with Ms. Lee to 15:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Grace Lee", "Hannah Lee")
        ),
        AgentWideCase(
            task = "add to Ms. Lee's note that she's out sick next week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Grace Lee", "Hannah Lee")
        ),
        AgentWideCase(
            task = "push the interview to 17:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Northbridge", "Harbor Bay")
        ),
        AgentWideCase(
            task = "change the campus visit time to 10:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Evergreen Ridge", "Olympic Coast", "Sound Valley")
        ),
        AgentWideCase(
            task = "mark the why-us supplement as second draft",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Olympic Coast", "Evergreen Ridge")
        ),
        AgentWideCase(
            task = "add 'request final transcript' to my applications checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Application", "request final transcript")
        ),
        AgentWideCase(
            task = "move the olympic coast interview to 19:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Olympic Coast", "19:00")
        ),
        AgentWideCase(
            task = "how much is parking in both my budgets together?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("252")
        ),
        AgentWideCase(
            task = "move my session with hannah lee to 18:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Hannah Lee", "18:00"),
            lacks = listOf("grace lee")
        ),
        AgentWideCase(
            task = "what did ms lee, my counselor at lakeview, say about ED?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("net price calculator"),
            lacks = listOf("41%")
        )
    )
}
