package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Blind Vet Clinic requests that must be asked back, and traps whose words settle them. */
internal object BlindVetClinicAsks {
    val all = listOf(
        AgentWideCase(
            task = "move Max's appointment to 2026-11-16 at 10:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Thibault", "Hollins")
        ),
        AgentWideCase(
            task = "set Dana's preferred contact to email",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Pruitt", "Whitcomb")
        ),
        AgentWideCase(
            task = "mark the schedule as final",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "add a task to my budget to review the insurance",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "push the surgery back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Biscuit", "Rook")
        ),
        AgentWideCase(
            task = "mark the vaccine order as received",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Lepto", "FVRCP")
        ),
        AgentWideCase(
            task = "cancel Clover's appointment",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("nail", "recheck")
        ),
        AgentWideCase(
            task = "mark the repair as fixed",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("X-ray", "compressor")
        ),
        AgentWideCase(
            task = "rename Max's note to add his breed",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Thibault", "Hollins")
        ),
        AgentWideCase(
            task = "move Max Hollins's appointment to 2026-11-16 at 10:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Appointments", "Hollins", "10:00")
        ),
        AgentWideCase(
            task = "set Dana Whitcomb's preferred contact to email",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Dana Whitcomb", "email"),
            lacks = listOf("Pruitt")
        ),
        AgentWideCase(
            task = "mark the conference schedule as final",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Schedule", "final"),
            lacks = listOf("Clinic/Schedule")
        ),
        AgentWideCase(
            task = "how much is in the clinic budget for CE?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("400"),
            lacks = listOf("120")
        ),
        AgentWideCase(
            task = "what breeds are both my Maxes?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("beagle", "Labrador")
        )
    )
}
