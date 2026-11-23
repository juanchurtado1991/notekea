package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindTeacherAsks {
    val all = listOf(
        ask(
            task = "add a $45 line for printing to the budget",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("field trip", "department")
        ),
        ask(
            task = "mark Jayden as paid on the permission slips",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Morales", "Price")
        ),
        ask(
            task = "push the period 1 quiz back a day",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Amendments", "Freedmen")
        ),
        ask(
            task = "move the department meeting to Thursday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Curriculum", "Budget")
        ),
        ask(
            task = "move my meeting with Jayden's parent to 4:30",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Morales", "Price")
        ),
        ask(
            task = "add a line to Jayden's note: great participation today",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Morales", "Price")
        ),
        ask(
            task = "move the parent meeting to Friday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Morales", "Price", "Grant")
        ),
        ask(
            task = "move the unit test to the 25th",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "ask_user"),
            holds = listOf("Reconstruction", "Industrial")
        ),
        // Traps: they look ambiguous, but the words settle them.
        answer(
            task = "how much have I actually spent out of the department budget so far?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("1114"),
            lacks = listOf("1209", "1264")
        ),
        answer(
            task = "what's the biggest expense in the field trip budget?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "answer"),
            holds = listOf("340"),
            lacks = listOf("540")
        ),
        change(
            task = "add a todo to Jayden Price's note: finish his recommendation letter",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "add_task", "propose"),
            holds = listOf("Jayden Price", "recommendation letter"),
            lacks = listOf("Jayden Morales")
        ),
        change(
            task = "move the meeting with Marcus Price to 16:30",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Parent Meetings", "Marcus Price", "16:30")
        ),
        change(
            task = "push the Freedmen's Bureau quiz to the 19th",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Freedmen", "19")
        )
    )
}
