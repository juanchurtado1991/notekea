package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Blind Vet Clinic requests that must be asked back, and traps whose words settle them. */
internal object BlindVetClinicAsks {
    val all = listOf(
        ask(
            task = "move Max's appointment to 2026-11-16 at 10:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Thibault", "Hollins")
        ),
        ask(
            task = "set Dana's preferred contact to email",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Pruitt", "Whitcomb")
        ),
        ask(
            task = "mark the schedule as final",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user")
        ),
        ask(
            task = "add a task to my budget to review the insurance",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user")
        ),
        ask(
            task = "push the surgery back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Biscuit", "Rook")
        ),
        ask(
            task = "mark the vaccine order as received",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Lepto", "FVRCP")
        ),
        ask(
            task = "cancel Clover's appointment",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("nail", "recheck")
        ),
        ask(
            task = "mark the repair as fixed",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("X-ray", "compressor")
        ),
        ask(
            task = "rename Max's note to add his breed",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Thibault", "Hollins")
        ),
        change(
            task = "move Max Hollins's appointment to 2026-11-16 at 10:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Appointments", "Hollins", "10:00")
        ),
        change(
            task = "set Dana Whitcomb's preferred contact to email",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "set_property", "propose"),
            holds = listOf("Dana Whitcomb", "email"),
            lacks = listOf("Pruitt")
        ),
        change(
            task = "mark the conference schedule as final",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "set_property", "propose"),
            holds = listOf("Schedule", "final"),
            lacks = listOf("Clinic/Schedule")
        ),
        answer(
            task = "how much is in the clinic budget for CE?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "answer"),
            holds = listOf("400"),
            lacks = listOf("120")
        ),
        answer(
            task = "what breeds are both my Maxes?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "read", "answer"),
            holds = listOf("beagle", "Labrador")
        )
    )
}
