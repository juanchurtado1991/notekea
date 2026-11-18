package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindAstronomyChanges {
    val all = listOf(
        AgentWideCase(
            task = "add a task to my treasurer list: send dues reminders by nov 20",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Treasurer Tasks", "dues reminders")
        ),
        AgentWideCase(
            task = "mark my messier marathon log as done (status)",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Messier Marathon 2026", "done")
        ),
        AgentWideCase(
            task = "tag the oct 24 log #galaxies",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-10-24", "galaxies")
        ),
        AgentWideCase(
            task = "add to my nov 7 log: Uranus showed a tiny blue-green disk at 125x",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-11-07", "blue-green")
        ),
        AgentWideCase(
            task = "write a note summing up my fall Ortiz Ranch sessions, objects and conditions",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("NGC 891", "M13")
        ),
        AgentWideCase(
            task = "move Outreach into the Club folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Outreach", "Club")
        ),
        AgentWideCase(
            task = "rename Orders to Gear Orders",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Gear Orders")
        ),
        AgentWideCase(
            task = "push the Mesquite Middle School visit to 2026-12-04",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Mesquite", "2026-12-04")
        ),
        AgentWideCase(
            task = "tag every observing log with seeing 4 or better #steady",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "read", "read", "read", "read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-09-14", "2026-10-24", "2026-11-07", "steady")
        ),
        AgentWideCase(
            task = "add a task in Treasurer Tasks to chase each member who hasn't paid dues",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Marisol Vega", "Carla Benitez", "Felix Moreno")
        ),
        AgentWideCase(
            task = "sign Priya up for the Fall Finale star party",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Priya", "Fall Finale")
        )
    )
}
