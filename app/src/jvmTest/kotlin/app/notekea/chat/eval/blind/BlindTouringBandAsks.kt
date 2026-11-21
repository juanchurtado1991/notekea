package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindTouringBandAsks {
    val all = listOf(
        AgentWideCase(
            task = "add a line for strings, £40, to my budget",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Tour", "Studio")
        ),
        AgentWideCase(
            task = "tick off spare strings on my checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Tour", "Studio")
        ),
        AgentWideCase(
            task = "update ollie's phone number to 07700 900877",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Brennan", "Sato")
        ),
        AgentWideCase(
            task = "move the van pick-up to 10:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "push the rehearsal back to 20:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "mark the invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "change the opener in the setlist to Glass Ferry",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Headline")
        ),
        AgentWideCase(
            task = "add a task to email jess about the guest list",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Jess")
        ),
        AgentWideCase(
            task = "set ollie brennan's status to injured",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Ollie Brennan", "injured"),
            lacks = listOf("Ollie Sato")
        ),
        AgentWideCase(
            task = "mark the glasgow invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Invoices"),
            lacks = listOf("INV-045", "Tyne Arches", "Newcastle")
        ),
        AgentWideCase(
            task = "add pack DI boxes to the studio checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Studio", "pack DI boxes")
        ),
        AgentWideCase(
            task = "change the opener of both sets to Glass Ferry",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Setlists", "Glass Ferry")
        ),
        AgentWideCase(
            task = "what's the total on the studio budget?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("3020")
        ),
        AgentWideCase(
            task = "what's jess harlow's email?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("jess@coalexchange.example"),
            lacks = listOf("jess.odili@signalbox.example")
        )
    )
}
