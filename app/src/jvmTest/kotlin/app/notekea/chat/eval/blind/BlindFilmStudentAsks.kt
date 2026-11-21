package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests to ask back, then traps whose words settle them. */
internal object BlindFilmStudentAsks {
    val all = listOf(
        AgentWideCase(
            task = "add a task in Taylor's note to confirm the Day 1 call time",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Brooks", "Kim")
        ),
        AgentWideCase(
            task = "set Jordan's status to confirmed",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Ellis", "Pierce")
        ),
        AgentWideCase(
            task = "move the meeting to 8pm",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("walkthrough", "camera test", "final prep")
        ),
        AgentWideCase(
            task = "mark the pending permit as approved",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Pier", "Laundromat")
        ),
        AgentWideCase(
            task = "set the budget note's status to final",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Production", "Grant")
        ),
        AgentWideCase(
            task = "add a task to my schedule note: send it to Prof. Achebe",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("festival")
        ),
        AgentWideCase(
            task = "push the pickup on the 19th to 15:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Lumen", "Sound Cave")
        ),
        AgentWideCase(
            task = "change the lens on the scene 4 close-up to 85mm",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("phone", "coin")
        ),
        AgentWideCase(
            task = "what's the DP Taylor's call time on Day 1?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("6:30")
        ),
        AgentWideCase(
            task = "set Jordan Pierce's status to confirmed",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Jordan Pierce", "confirmed")
        ),
        AgentWideCase(
            task = "move the camera test to 11:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Camera test", "11:00")
        ),
        AgentWideCase(
            task = "what's the total on the grant budget?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("4,800"),
            lacks = listOf("6155")
        ),
        AgentWideCase(
            task = "mark both pending permits as approved",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Westport Pier", "Laundromat")
        ),
        AgentWideCase(
            task = "push the Sound Cave pickup to 17:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Sound Cave", "17:00")
        )
    )
}
