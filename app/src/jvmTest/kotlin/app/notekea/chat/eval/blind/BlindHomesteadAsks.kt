package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests to ask back about, then traps that only look ambiguous. */
internal object BlindHomesteadAsks {
    val all = listOf(
        ask(
            task = "set Daisy's status to dry",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("nubian", "alpine")
        ),
        ask(
            task = "add to Tom's note that he prefers texts after 6pm",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("harlan", "briggs")
        ),
        ask(
            task = "add zip ties to my supplies list",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("animal", "garden")
        ),
        ask(
            task = "move the vet appointment to the 20th",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("rufus")
        ),
        ask(
            task = "mark the seed order as received",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("ridgeline", "bluegrass")
        ),
        ask(
            task = "add a task to Daisy's note: trim her hooves saturday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("nubian", "alpine")
        ),
        ask(
            task = "mark the IOU as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("carla", "pete")
        ),
        ask(
            task = "mark the hay delivery paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user")
        ),
        answer(
            task = "how much do I owe Tom for hay?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("490")
        ),
        change(
            task = "move Rufus's vet appointment to December 4, same time",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Rufus", "10:15")
        ),
        change(
            task = "add zip ties to the garden supplies list",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("zip ties", "garden")
        ),
        change(
            task = "the bluegrass seed order came today, mark it received",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("bluegrass", "received")
        ),
        answer(
            task = "how much milk did both Daisys give together in october?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("275.5")
        )
    )
}
