package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindTouringBandChanges {
    val all = listOf(
        change(
            task = "tag every venue note with an unpaid invoice #chase-payment",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "propose"),
            holds = listOf("Lamplight Rooms", "Tyne Arches", "chase-payment"),
            lacks = listOf("Brickyard Social", "Northgate Hall")
        ),
        change(
            task = "set brickyard social's rating to 5",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Brickyard Social", "rating")
        ),
        change(
            task = "write a new note Leg Two Sheet with the dates, venues and who's on drums",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "write_note", "propose"),
            holds = listOf(
                "Leg Two Sheet",
                "Signal Box",
                "Coal Exchange",
                "Hollow Arch",
                "Ollie Sato"
            ),
            lacks = listOf("Tyne Arches")
        ),
        change(
            task = "add a line to my 10 nov journal: Brickyard paid INV-043",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "append", "propose"),
            holds = listOf("2026-11-10", "INV-043")
        ),
        change(
            task = "move the saltmarsh offer into the Tour folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            holds = listOf("Saltmarsh Fest Offer", "Tour")
        ),
        change(
            task = "rename song ideas to Demos 2027",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Song Ideas", "Demos 2027")
        ),
        change(
            task = "change the newcastle fuel entry to £79, I misread the receipt",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Fuel Log", "79")
        ),
        change(
            task = "add a task to mags's note to send her the chord charts",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Mags Ferreira", "chord charts")
        ),
        change(
            task = "add a task to the tour note: send stage times to the hollow arch",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("UK Autumn Tour 2026", "stage times")
        )
    )
}
