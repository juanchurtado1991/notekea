package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Requests to ask back, and traps that only look ambiguous. */
internal object BlindInteriorDesignAsks {
    val all = listOf(
        AgentWideCase(
            task = "add a line for a £600 rug to the budget",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Hampstead", "Islington", "Clapham")
        ),
        AgentWideCase(
            task = "move my meeting with ravi to 15:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("10:30", "11:00", "10:00")
        ),
        AgentWideCase(
            task = "add a task to chase Patel about the unpaid invoice",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Ravi", "Meera")
        ),
        AgentWideCase(
            task = "move dan's clapham slot to 2026-11-27",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Whelan", "Okafor")
        ),
        AgentWideCase(
            task = "change the due date of the fenwick order for hampstead to 2026-12-01",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Orbit", "Arc")
        ),
        AgentWideCase(
            task = "mark the pending linden order as delivered",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Oat", "Fern", "Teal")
        ),
        AgentWideCase(
            task = "set dan's day rate to £340",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Okafor", "Whelan")
        ),
        AgentWideCase(
            task = "add 3 m of piping cord from linden to the orders note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Hampstead", "Islington", "Clapham")
        ),
        AgentWideCase(
            task = "how much have I invoiced Ravi Patel this year?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("5930")
        ),
        AgentWideCase(
            task = "when's dan whelan coming to the islington flat?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("wardrobe")
        ),
        AgentWideCase(
            task = "move both pending fenwick orders on hampstead to 2026-12-01",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Orbit", "wall lights", "2026-12-01")
        ),
        AgentWideCase(
            task = "add a task to the islington budget: review the contractor overspend",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Islington", "contractor overspend")
        ),
        AgentWideCase(
            task = "move my 2026-11-28 meeting with ravi to 15:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("15:00", "Hampstead")
        )
    )
}
