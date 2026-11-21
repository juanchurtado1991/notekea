package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Data Consultant vault, each one clear from the words and the notes. */
internal object BlindDataConsultantChanges {
    val all = listOf(
        AgentWideCase(
            task = "tag every client that still has an unpaid invoice with #unpaid",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Müller Logistik", "Müller Kliniken", "unpaid")
        ),
        AgentWideCase(
            task = "add a task to my learning plan: finish chapter 6 of Notes on Calibration by " +
                "end of november",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Learning plan", "chapter 6")
        ),
        AgentWideCase(
            task = "set the Bayesian Workflows course to done",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Bayesian Workflows", "done")
        ),
        AgentWideCase(
            task = "add a line to yesterday's journal: Kliniken wants a training session " +
                "in December",
            kind = Kind.DOCUMENT,
            tools = listOf("date", "read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-11-10", "training session")
        ),
        AgentWideCase(
            task = "make a note called Open invoices with every unpaid invoice and its amount",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf(
                "Open invoices",
                "ML-2026-09",
                "MK-2026-04",
                "MK-2026-05",
                "4320",
                "6160",
                "1980"
            ),
            lacks = listOf("ML-2026-08", "NM-2026-12")
        ),
        AgentWideCase(
            task = "rename the Abstract note to Copenhagen abstract",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Copenhagen abstract")
        ),
        AgentWideCase(
            task = "the Logistik sprint demo moved to 10:30, update it",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("sprint demo", "10:30")
        ),
        AgentWideCase(
            task = "set Nordlicht Media to paused",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Nordlicht Media", "paused")
        ),
        AgentWideCase(
            task = "add a task to my tax note to pay the Q4 income tax before it's due",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Tax prepayments", "Q4")
        ),
        AgentWideCase(
            task = "log 4 hours for Nordlicht today, results deck prep",
            kind = Kind.DOCUMENT,
            tools = listOf("date", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-W46", "Nordlicht")
        )
    )
}
