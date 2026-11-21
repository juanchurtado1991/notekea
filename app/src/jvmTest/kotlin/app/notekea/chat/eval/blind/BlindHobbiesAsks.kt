package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindHobbiesAsks {
    val all = listOf(
        AgentWideCase(
            task = "what time is the train on day 3 in my itinerary?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Kyoto 2025", "Scotland 2027")
        ),
        AgentWideCase(
            task = "when is rita's birthday?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Almeida", "Fonseca")
        ),
        AgentWideCase(
            task = "add a task to call rita on friday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Almeida", "Fonseca")
        ),
        AgentWideCase(
            task = "the sementes ribeira order came, mark it delivered",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("GS-2305", "GS-2318", "GS-2320")
        ),
        AgentWideCase(
            task = "move my food bank shift to 10:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("2026-11-14", "2026-11-21", "2026-11-28")
        ),
        AgentWideCase(
            task = "mark the scotland booking as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Grey Close Inn", "car hire")
        ),
        AgentWideCase(
            task = "rename the itinerary note to Day by day",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Kyoto 2025", "Scotland 2027")
        ),
        AgentWideCase(
            task = "change my japanese lesson to 19:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("2026-11-18", "2026-11-25", "2026-12-02")
        ),
        AgentWideCase(
            task = "what time is the train on day 3 of the scotland itinerary?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("09:33"),
            lacks = listOf("09:15")
        ),
        AgentWideCase(
            task = "when's rita fonseca's birthday?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("1961-12-02"),
            lacks = listOf("1958-03-22")
        ),
        AgentWideCase(
            task = "move both saturday morning food bank shifts to 10:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-11-14", "2026-11-21", "10:00")
        ),
        AgentWideCase(
            task = "whats the phone of rita from the book club?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("912 345 118"),
            lacks = listOf("914 202 671")
        ),
        AgentWideCase(
            task = "add 'book Edinburgh Castle tickets' to the to do in the itinerary of my " +
                "may trip",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Scotland 2027", "Edinburgh Castle")
        )
    )
}
