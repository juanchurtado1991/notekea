package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Data Consultant vault, each one clear from the words and the notes. */
internal object BlindDataConsultantChanges {
    val all = listOf(
        change(
            task = "tag every client that still has an unpaid invoice with #unpaid",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "add_tag", "propose"),
            holds = listOf("Müller Logistik", "Müller Kliniken", "unpaid")
        ),
        change(
            task = "add a task to my learning plan: finish chapter 6 of Notes on Calibration by " +
                "end of november",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            holds = listOf("Learning plan", "chapter 6")
        ),
        change(
            task = "set the Bayesian Workflows course to done",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            holds = listOf("Bayesian Workflows", "done")
        ),
        change(
            task = "add a line to yesterday's journal: Kliniken wants a training session " +
                "in December",
            kind = Kind.DOCUMENT,
            tools = listOf("date", "read", "append", "propose"),
            holds = listOf("2026-11-10", "training session")
        ),
        change(
            task = "make a note called Open invoices with every unpaid invoice and its amount",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "write_note", "propose"),
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
        change(
            task = "rename the Abstract note to Copenhagen abstract",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Copenhagen abstract")
        ),
        change(
            task = "the Logistik sprint demo moved to 10:30, update it",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("sprint demo", "10:30")
        ),
        change(
            task = "set Nordlicht Media to paused",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            holds = listOf("Nordlicht Media", "paused")
        ),
        change(
            task = "add a task to my tax note to pay the Q4 income tax before it's due",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            holds = listOf("Tax prepayments", "Q4")
        ),
        change(
            task = "log 4 hours for Nordlicht today, results deck prep",
            kind = Kind.DOCUMENT,
            tools = listOf("date", "read", "edit_note", "propose"),
            holds = listOf("2026-W46", "Nordlicht")
        )
    )
}
