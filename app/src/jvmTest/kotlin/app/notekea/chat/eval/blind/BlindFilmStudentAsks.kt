package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests to ask back, then traps whose words settle them. */
internal object BlindFilmStudentAsks {
    val all = listOf(
        ask(
            task = "add a task in Taylor's note to confirm the Day 1 call time",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Brooks", "Kim")
        ),
        ask(
            task = "set Jordan's status to confirmed",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Ellis", "Pierce")
        ),
        ask(
            task = "move the meeting to 8pm",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("walkthrough", "camera test", "final prep")
        ),
        ask(
            task = "mark the pending permit as approved",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Pier", "Laundromat")
        ),
        ask(
            task = "set the budget note's status to final",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Production", "Grant")
        ),
        ask(
            task = "add a task to my schedule note: send it to Prof. Achebe",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("festival")
        ),
        ask(
            task = "push the pickup on the 19th to 15:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Lumen", "Sound Cave")
        ),
        ask(
            task = "change the lens on the scene 4 close-up to 85mm",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("phone", "coin")
        ),
        answer(
            task = "what's the DP Taylor's call time on Day 1?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("6:30")
        ),
        change(
            task = "set Jordan Pierce's status to confirmed",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "set_property", "propose"),
            holds = listOf("Jordan Pierce", "confirmed")
        ),
        change(
            task = "move the camera test to 11:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Camera test", "11:00")
        ),
        answer(
            task = "what's the total on the grant budget?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("4,800"),
            lacks = listOf("6155")
        ),
        change(
            task = "mark both pending permits as approved",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Westport Pier", "Laundromat")
        ),
        change(
            task = "push the Sound Cave pickup to 17:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Sound Cave", "17:00")
        )
    )
}
