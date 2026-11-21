package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindTouringBandChanges {
    val all = listOf(
        AgentWideCase(
            task = "tag every venue note with an unpaid invoice #chase-payment",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Lamplight Rooms", "Tyne Arches", "chase-payment"),
            lacks = listOf("Brickyard Social", "Northgate Hall")
        ),
        AgentWideCase(
            task = "set brickyard social's rating to 5",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Brickyard Social", "rating")
        ),
        AgentWideCase(
            task = "write a new note Leg Two Sheet with the dates, venues and who's on drums",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf(
                "Leg Two Sheet",
                "Signal Box",
                "Coal Exchange",
                "Hollow Arch",
                "Ollie Sato"
            ),
            lacks = listOf("Tyne Arches")
        ),
        AgentWideCase(
            task = "add a line to my 10 nov journal: Brickyard paid INV-043",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-11-10", "INV-043")
        ),
        AgentWideCase(
            task = "move the saltmarsh offer into the Tour folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Saltmarsh Fest Offer", "Tour")
        ),
        AgentWideCase(
            task = "rename song ideas to Demos 2027",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Song Ideas", "Demos 2027")
        ),
        AgentWideCase(
            task = "change the newcastle fuel entry to £79, I misread the receipt",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Fuel Log", "79")
        ),
        AgentWideCase(
            task = "add a task to mags's note to send her the chord charts",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Mags Ferreira", "chord charts")
        ),
        AgentWideCase(
            task = "add a task to the tour note: send stage times to the hollow arch",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("UK Autumn Tour 2026", "stage times")
        )
    )
}
