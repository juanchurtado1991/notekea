package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests that must be asked back, and traps whose words settle them. */
internal object BlindTattooStudioAsks {
    val all = listOf(
        ask(
            task = "move jonas's session to next thursday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Becker", "Wolf")
        ),
        ask(
            task = "add a task to jonas's note: bring the sketch revisions",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Becker", "Wolf")
        ),
        ask(
            task = "move jonas becker's session to 13:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user")
        ),
        ask(
            task = "add to my orders note: label printer, €74, pending",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("orders")
        ),
        ask(
            task = "tick off restock gloves in the checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("gloves")
        ),
        ask(
            task = "mark the pending supply order as received",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("gloves", "Ocean")
        ),
        ask(
            task = "add a task to the jonas design: print the stencil",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Koi", "Botanical")
        ),
        ask(
            task = "add a task to the guest artist's note: send the contract",
            kind = Kind.AMBIGUOUS,
            tools = listOf("list", "ask_user"),
            holds = listOf("Rosa", "Kenji")
        ),
        ask(
            task = "move the aftercare check-in to friday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Aylin", "Felix")
        ),
        change(
            task = "mark the gloves order in supplies orders as received",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Gloves", "received")
        ),
        change(
            task = "tick restock gloves in both checklists",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "read", "edit_note", "propose"),
            holds = listOf("Restock gloves", "Supplies", "Studio")
        ),
        change(
            task = "add 'send aftercare sheet' to both jonases",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Jonas Becker", "Jonas Wolf", "send aftercare sheet")
        ),
        answer(
            task = "how much deposit did the jonas with the koi pay?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("200")
        ),
        change(
            task = "move jonas becker's last session to 2027-01-16",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Jonas Becker", "2027-01-16", "background")
        )
    )
}
