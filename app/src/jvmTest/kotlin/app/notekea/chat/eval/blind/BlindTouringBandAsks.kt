package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindTouringBandAsks {
    val all = listOf(
        ask(
            task = "add a line for strings, £40, to my budget",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Tour", "Studio")
        ),
        ask(
            task = "tick off spare strings on my checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Tour", "Studio")
        ),
        ask(
            task = "update ollie's phone number to 07700 900877",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Brennan", "Sato")
        ),
        ask(
            task = "move the van pick-up to 10:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user")
        ),
        ask(
            task = "push the rehearsal back to 20:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user")
        ),
        ask(
            task = "mark the invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user")
        ),
        ask(
            task = "change the opener in the setlist to Glass Ferry",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Headline")
        ),
        ask(
            task = "add a task to email jess about the guest list",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Jess")
        ),
        change(
            task = "set ollie brennan's status to injured",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Ollie Brennan", "injured"),
            lacks = listOf("Ollie Sato")
        ),
        change(
            task = "mark the glasgow invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Invoices"),
            lacks = listOf("INV-045", "Tyne Arches", "Newcastle")
        ),
        change(
            task = "add pack DI boxes to the studio checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Studio", "pack DI boxes")
        ),
        change(
            task = "change the opener of both sets to Glass Ferry",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Setlists", "Glass Ferry")
        ),
        answer(
            task = "what's the total on the studio budget?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("3020")
        ),
        answer(
            task = "what's jess harlow's email?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("jess@coalexchange.example"),
            lacks = listOf("jess.odili@signalbox.example")
        )
    )
}
