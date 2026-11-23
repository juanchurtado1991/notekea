package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Blind Translator requests that must be asked back, and traps whose words settle them. */
internal object BlindTranslatorAsks {
    val all = listOf(
        ask(
            task = "add 'double-check the dates' to my checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("admin")
        ),
        ask(
            task = "move my call with Tom to Thursday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Hartley", "Okafor")
        ),
        ask(
            task = "mark the Verba Translations invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("INV-2026-040", "INV-2026-042")
        ),
        ask(
            task = "push the deadline of the Verba job to 2026-11-22",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("leaflet", "UI strings")
        ),
        ask(
            task = "move my appointment to 17:00, same day",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("accountant", "dentist", "physio")
        ),
        ask(
            task = "tag the glossary #reviewed",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Pharma", "Wine", "Software")
        ),
        ask(
            task = "rename the Verba client note to Verba (old)",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Verba Translations", "Verba Global")
        ),
        ask(
            task = "change the due date of the overdue invoice to 2026-11-30",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("INV-2026-036", "INV-2026-039", "INV-2026-040")
        ),
        answer(
            task = "how much does Verba Translations owe me?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("693"),
            lacks = listOf("2145")
        ),
        change(
            task = "mark the Okafor invoice from 2026-10-20 as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("INV-2026-041", "paid")
        ),
        change(
            task = "move my call with Tom Okafor to 2026-11-18 at 16:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Tom Okafor", "2026-11-18"),
            lacks = listOf("Hartley")
        ),
        change(
            task = "add 'double-check the dates' to the Admin checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("double-check the dates", "Admin"),
            lacks = listOf("Jobs/Checklist")
        ),
        change(
            task = "tag all my glossaries #reference",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_tag", "propose"),
            holds = listOf("Pharma EN-PT", "Wine EN-PT", "Software UI EN-PT", "reference")
        )
    )
}
