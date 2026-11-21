package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests the Blind Choir vault cannot settle, then traps whose words do settle them. */
internal object BlindChoirAsks {
    val all = listOf(
        AgentWideCase(
            task = "set the budget to approved",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("winter")
        ),
        AgentWideCase(
            task = "Rhian wants to swap to alto, change her part",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Morgan", "Hughes")
        ),
        AgentWideCase(
            task = "push the venue booking back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Aidan", "Ely", "Penylan")
        ),
        AgentWideCase(
            task = "add a task to chase the pending sheet music order",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Calon Lân", "Suo Gân", "folders")
        ),
        AgentWideCase(
            task = "Dafydd paid his subs, tick him off",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Evans", "Price")
        ),
        AgentWideCase(
            task = "add Sian Rowlands 07700 900845 to my contacts",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("emergency")
        ),
        AgentWideCase(
            task = "move the sectional to Friday, same time",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("soprano", "tenor", "bass")
        ),
        AgentWideCase(
            task = "tick off the raffle prize, I picked it up this morning",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("voucher", "wine")
        ),
        AgentWideCase(
            task = "add lip trills to the warm-ups note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("library", "rotation")
        ),
        AgentWideCase(
            task = "Rhian's back from leave next week, set her status to active",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Rhian Hughes", "active"),
            lacks = listOf("Rhian Morgan")
        ),
        AgentWideCase(
            task = "who's Rhian Hughes's emergency contact?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Gethin Hughes"),
            lacks = listOf("Iwan Morgan")
        ),
        AgentWideCase(
            task = "set the winter concert budget to approved",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("budget", "approved")
        ),
        AgentWideCase(
            task = "both Dafydds paid their subs tonight, tick them off",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Dafydd Evans", "Dafydd Price")
        ),
        AgentWideCase(
            task = "the bass sectional's moved to Room 3, update it",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("bass", "Room 3")
        )
    )
}
