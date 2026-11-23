package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindNurseAsks {
    val all = listOf(
        ask(
            task = "add a task to the meeting notes: send my notes round to everyone",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("meeting")
        ),
        ask(
            task = "add to Chris's note that he's moving to the HDU side in january",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Doyle", "Okafor")
        ),
        ask(
            task = "add to JM's handover: chest x-ray booked for 06:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Bed 3", "Bed 7")
        ),
        ask(
            task = "move my appointment to the 20th, same time",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Dentist", "Optician")
        ),
        ask(
            task = "cancel the study day, I can't make it",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Haemofiltration", "Delirium")
        ),
        ask(
            task = "set status: reviewed on my budget",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("budget")
        ),
        ask(
            task = "the bank night got paid, update it in the overtime log",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("19", "31")
        ),
        ask(
            task = "put it in rota swaps: I'm doing Chris's night on 2026-11-27",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Doyle", "Okafor")
        ),
        // Traps: they look ambiguous, but their words settle them.
        answer(
            task = "what's JM in bed 7 allergic to?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("latex"),
            lacks = listOf("penicillin")
        ),
        change(
            task = "add to Chris Okafor's note: preceptorship midpoint review on 2026-12-04",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "append", "propose"),
            holds = listOf("Chris Okafor", "2026-12-04"),
            lacks = listOf("Doyle")
        ),
        answer(
            task = "what's my action from the ward meeting?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("pressure area")
        ),
        change(
            task = "push both my dentist and optician appointments back a week, same times",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "date", "edit_note", "propose"),
            holds = listOf("Appointments", "2026-11-21", "2026-12-03")
        ),
        change(
            task = "set status: reviewed on my home budget",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Personal/Budget", "reviewed"),
            lacks = listOf("Courses/Budget")
        )
    )
}
