package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Blind Translator requests that must be asked back, and traps whose words settle them. */
internal object BlindTranslatorAsks {
    val all = listOf(
        AgentWideCase(
            task = "add 'double-check the dates' to my checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("admin")
        ),
        AgentWideCase(
            task = "move my call with Tom to Thursday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Hartley", "Okafor")
        ),
        AgentWideCase(
            task = "mark the Verba Translations invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("INV-2026-040", "INV-2026-042")
        ),
        AgentWideCase(
            task = "push the deadline of the Verba job to 2026-11-22",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("leaflet", "UI strings")
        ),
        AgentWideCase(
            task = "move my appointment to 17:00, same day",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("accountant", "dentist", "physio")
        ),
        AgentWideCase(
            task = "tag the glossary #reviewed",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Pharma", "Wine", "Software")
        ),
        AgentWideCase(
            task = "rename the Verba client note to Verba (old)",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Verba Translations", "Verba Global")
        ),
        AgentWideCase(
            task = "change the due date of the overdue invoice to 2026-11-30",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("INV-2026-036", "INV-2026-039", "INV-2026-040")
        ),
        AgentWideCase(
            task = "how much does Verba Translations owe me?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("693"),
            lacks = listOf("2145")
        ),
        AgentWideCase(
            task = "mark the Okafor invoice from 2026-10-20 as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("INV-2026-041", "paid")
        ),
        AgentWideCase(
            task = "move my call with Tom Okafor to 2026-11-18 at 16:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Tom Okafor", "2026-11-18"),
            lacks = listOf("Hartley")
        ),
        AgentWideCase(
            task = "add 'double-check the dates' to the Admin checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("double-check the dates", "Admin"),
            lacks = listOf("Jobs/Checklist")
        ),
        AgentWideCase(
            task = "tag all my glossaries #reference",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Pharma EN-PT", "Wine EN-PT", "Software UI EN-PT", "reference")
        )
    )
}
