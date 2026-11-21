package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindPhotographerAsks {
    val all = listOf(
        AgentWideCase(
            task = "add a task to my insurance note: send the renewal form",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Business", "Gear")
        ),
        AgentWideCase(
            task = "change the mileage rate in my rates note to \$0.60",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Business", "Team")
        ),
        AgentWideCase(
            task = "add a task to jordan's note to send the contract addendum",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Ellis", "Pike")
        ),
        AgentWideCase(
            task = "change jordan's start time to 7pm on the schedule",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Ellis", "Pike")
        ),
        AgentWideCase(
            task = "update the repair ETA to next friday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("70-200", "Body B")
        ),
        AgentWideCase(
            task = "take the lens off my wishlist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("28-70", "135")
        ),
        AgentWideCase(
            task = "push the gallery deadline back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Hollis", "Sandoval", "Westbrook")
        ),
        AgentWideCase(
            task = "set the location for teresa's session to Grant Park",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("engagement", "bridal")
        ),
        // Traps: they look ambiguous, but the words settle them.
        AgentWideCase(
            task = "how many days till my gear insurance renews?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("34 days")
        ),
        AgentWideCase(
            task = "add a task to Jordan Pike's note: send the gala shot list",
            kind = Kind.AMBIGUOUS,
            tools = listOf("add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Jordan Pike", "gala shot list"),
            lacks = listOf("Jordan Ellis")
        ),
        AgentWideCase(
            task = "update the mileage rate in Team/Rates to \$0.55",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Team", "0.55")
        ),
        AgentWideCase(
            task = "mark the flash repair as done",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Speedlight"),
            lacks = listOf("70-200")
        ),
        AgentWideCase(
            task = "how many days until the sandoval gallery is due?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("24 days")
        ),
        AgentWideCase(
            task = "add 'send the timeline questionnaire' to both jordans' notes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("add_task", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Jordan Ellis", "Jordan Pike", "timeline questionnaire")
        )
    )
}
