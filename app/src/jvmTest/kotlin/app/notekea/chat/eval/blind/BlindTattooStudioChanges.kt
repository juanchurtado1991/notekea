package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the tattoo studio vault, each settled by the words and the notes. */
internal object BlindTattooStudioChanges {
    val all = listOf(
        AgentWideCase(
            task = "add a task to jonas wolf's note: send deposit reminder",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Jonas Wolf", "deposit reminder")
        ),
        AgentWideCase(
            task = "clara paid her deposit, update the deposits table",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Deposits", "Clara Hoffmann", "paid")
        ),
        AgentWideCase(
            task = "set the moth chest design to approved",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Moth chest", "approved")
        ),
        AgentWideCase(
            task = "tag every client with an unpaid deposit #deposit-due",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Jonas Wolf", "Clara Hoffmann", "Nadia Kowalski", "deposit-due"),
            lacks = listOf("Priya Raman", "Theo Brandt")
        ),
        AgentWideCase(
            task = "add to my nov 10 journal: called the inspector, re-check confirmed",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-11-10", "re-check confirmed")
        ),
        AgentWideCase(
            task =
            "make a note Re-inspection prep with what the inspector flagged and " +
                "when he comes back",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Re-inspection prep", "sharps container", "2027-01-19")
        ),
        AgentWideCase(
            task = "move the flash sheet into Designs/Archive",
            kind = Kind.DOCUMENT,
            tools = listOf("find", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Flash sheet November", "Archive")
        ),
        AgentWideCase(
            task = "rename Script Aylin to Aylin collarbone script",
            kind = Kind.DOCUMENT,
            tools = listOf("find", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Script Aylin", "Aylin collarbone script")
        ),
        AgentWideCase(
            task = "push jonas becker's december session to 2026-12-12, same time",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Jonas Becker", "2026-12-12", "12:00")
        ),
        AgentWideCase(
            task = "link jonas becker's note to his koi design",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "link", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Jonas Becker", "Koi back piece")
        ),
        AgentWideCase(
            task = "add to kenji's note: confirm flight for december",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Kenji Arata", "confirm flight")
        ),
        AgentWideCase(
            task = "tag the guest artists' notes #guest-artist",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Rosa Mendel", "Kenji Arata", "guest-artist"),
            lacks = listOf("Dario Conti")
        )
    )
}
