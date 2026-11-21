package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the Blind Homestead notes, each settled by the words and the notes. */
internal object BlindHomesteadChanges {
    val all = listOf(
        AgentWideCase(
            task = "add a task to Rufus's note: trim his hooves this weekend",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Rufus", "hooves")
        ),
        AgentWideCase(
            task = "Clover's bred now, set her status to bred",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Clover", "bred")
        ),
        AgentWideCase(
            task = "tag the well water report #followup",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Well Water Report", "followup")
        ),
        AgentWideCase(
            task = "add to the compost note: turned pile 2 today, 140°F",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Compost", "140")
        ),
        AgentWideCase(
            task = "make a note called Goat Herd Summary with each goat's breed and birth date",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Goat Herd Summary", "LaMancha", "Alpine", "Rufus", "Clover")
        ),
        AgentWideCase(
            task = "move both journal notes into a new Journal folder",
            kind = Kind.DOCUMENT,
            tools = listOf("list", "create_folder", "move", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Journal 2026-11-03", "Journal 2026-11-09")
        ),
        AgentWideCase(
            task = "rename the Compost note to Compost Bins",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Compost Bins")
        ),
        AgentWideCase(
            task = "the CDT booster appointment is moving to 15:00, same day",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("CDT", "15:00")
        ),
        AgentWideCase(
            task = "tag every goat that's due to kid before April with #due-spring",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Daisy (Nubian)", "Clover", "due-spring")
        ),
        AgentWideCase(
            task = "add a task to the chores rota: clean the heat lamp covers",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Chores Rota", "heat lamp covers")
        ),
        AgentWideCase(
            task = "write a note Unpaid Bills listing every unpaid feed purchase with its amount",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Unpaid Bills", "320", "170", "27")
        ),
        AgentWideCase(
            task = "Carla paid her IOU, tick it off",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Carla", "Market Sales - November 2026")
        )
    )
}
