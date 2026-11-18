package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests to ask back about, then traps that only look ambiguous. */
internal object BlindHomesteadAsks {
    val all = listOf(
        AgentWideCase(
            task = "set Daisy's status to dry",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("nubian", "alpine")
        ),
        AgentWideCase(
            task = "add to Tom's note that he prefers texts after 6pm",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("harlan", "briggs")
        ),
        AgentWideCase(
            task = "add zip ties to my supplies list",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("animal", "garden")
        ),
        AgentWideCase(
            task = "move the vet appointment to the 20th",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("rufus")
        ),
        AgentWideCase(
            task = "mark the seed order as received",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("ridgeline", "bluegrass")
        ),
        AgentWideCase(
            task = "add a task to Daisy's note: trim her hooves saturday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("nubian", "alpine")
        ),
        AgentWideCase(
            task = "mark the IOU as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("carla", "pete")
        ),
        AgentWideCase(
            task = "mark the hay delivery paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "how much do I owe Tom for hay?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("490")
        ),
        AgentWideCase(
            task = "move Rufus's vet appointment to December 4, same time",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Rufus", "10:15")
        ),
        AgentWideCase(
            task = "add zip ties to the garden supplies list",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("zip ties", "garden")
        ),
        AgentWideCase(
            task = "the bluegrass seed order came today, mark it received",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("bluegrass", "received")
        ),
        AgentWideCase(
            task = "how much milk did both Daisys give together in october?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("275.5")
        )
    )
}
