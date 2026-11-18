package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests that must be asked back, and traps whose words settle them. */
internal object BlindTattooStudioAsks {
    val all = listOf(
        AgentWideCase(
            task = "move jonas's session to next thursday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Becker", "Wolf")
        ),
        AgentWideCase(
            task = "add a task to jonas's note: bring the sketch revisions",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Becker", "Wolf")
        ),
        AgentWideCase(
            task = "move jonas becker's session to 13:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "add to my orders note: label printer, €74, pending",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("orders")
        ),
        AgentWideCase(
            task = "tick off restock gloves in the checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("gloves")
        ),
        AgentWideCase(
            task = "mark the pending supply order as received",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("gloves", "Ocean")
        ),
        AgentWideCase(
            task = "add a task to the jonas design: print the stencil",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Koi", "Botanical")
        ),
        AgentWideCase(
            task = "add a task to the guest artist's note: send the contract",
            kind = Kind.AMBIGUOUS,
            tools = listOf("list", "ask_user"),
            ends = End.ASK,
            holds = listOf("Rosa", "Kenji")
        ),
        AgentWideCase(
            task = "move the aftercare check-in to friday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Aylin", "Felix")
        ),
        AgentWideCase(
            task = "mark the gloves order in supplies orders as received",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Gloves", "received")
        ),
        AgentWideCase(
            task = "tick restock gloves in both checklists",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Restock gloves", "Supplies", "Studio")
        ),
        AgentWideCase(
            task = "add 'send aftercare sheet' to both jonases",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Jonas Becker", "Jonas Wolf", "send aftercare sheet")
        ),
        AgentWideCase(
            task = "how much deposit did the jonas with the koi pay?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("200")
        ),
        AgentWideCase(
            task = "move jonas becker's last session to 2027-01-16",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Jonas Becker", "2027-01-16", "background")
        )
    )
}
