package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindTeacherAsks {
    val all = listOf(
        AgentWideCase(
            task = "add a $45 line for printing to the budget",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("field trip", "department")
        ),
        AgentWideCase(
            task = "mark Jayden as paid on the permission slips",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Morales", "Price")
        ),
        AgentWideCase(
            task = "push the period 1 quiz back a day",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Amendments", "Freedmen")
        ),
        AgentWideCase(
            task = "move the department meeting to Thursday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Curriculum", "Budget")
        ),
        AgentWideCase(
            task = "move my meeting with Jayden's parent to 4:30",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Morales", "Price")
        ),
        AgentWideCase(
            task = "add a line to Jayden's note: great participation today",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Morales", "Price")
        ),
        AgentWideCase(
            task = "move the parent meeting to Friday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Morales", "Price", "Grant")
        ),
        AgentWideCase(
            task = "move the unit test to the 25th",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Reconstruction", "Industrial")
        ),
        // Traps: they look ambiguous, but the words settle them.
        AgentWideCase(
            task = "how much have I actually spent out of the department budget so far?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1114"),
            lacks = listOf("1209", "1264")
        ),
        AgentWideCase(
            task = "what's the biggest expense in the field trip budget?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("340"),
            lacks = listOf("540")
        ),
        AgentWideCase(
            task = "add a todo to Jayden Price's note: finish his recommendation letter",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Jayden Price", "recommendation letter"),
            lacks = listOf("Jayden Morales")
        ),
        AgentWideCase(
            task = "move the meeting with Marcus Price to 16:30",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Parent Meetings", "Marcus Price", "16:30")
        ),
        AgentWideCase(
            task = "push the Freedmen's Bureau quiz to the 19th",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Freedmen", "19")
        )
    )
}
