package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindDogTrainerChanges {
    val all = listOf(
        AgentWideCase(
            task = "add a 40 lb bag of puppy kibble to my shopping list",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Shopping List", "puppy kibble")
        ),
        AgentWideCase(
            task = "set the litter note's status to going home",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Litter Juniper 2026", "going home")
        ),
        AgentWideCase(
            task = "tag Walt's note board-and-train",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Walt Hendricks", "board-and-train")
        ),
        AgentWideCase(
            task = "add to Lucy's note that Pip nailed her tunnel intro today",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Lucy Fernsby", "tunnel")
        ),
        AgentWideCase(
            task = "make me a Pickup Day note: each buyer, their puppy's collar, pickup time " +
                "and the balance they owe at pickup",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Pickup Day", "Tom Delgado", "Carla Benning"),
            computed = listOf("1900", "2050"),
            lacks = listOf("Gus Mallory")
        ),
        AgentWideCase(
            task = "puppy basics fall 2026 is over, put it in an Archive folder",
            kind = Kind.DOCUMENT,
            tools = listOf("create_folder", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Puppy Basics Fall 2026", "Archive")
        ),
        AgentWideCase(
            task = "Carla got married, rename her note to Carla Benning-Ortiz",
            kind = Kind.DOCUMENT,
            tools = listOf("rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Carla Benning-Ortiz")
        ),
        AgentWideCase(
            task = "change Priya's pickup time to 12:00",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Priya", "12:00")
        ),
        AgentWideCase(
            task = "tag every client with an unpaid invoice #unpaid",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Walt Hendricks", "Megan Ashby", "Priya Nair"),
            lacks = listOf("Lucy Fernsby", "Megan Pruitt")
        ),
        AgentWideCase(
            task = "add a task to each puppy buyer's note: send the vaccination record",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Tom Delgado", "Priya Nair", "Megan Ashby", "Carla Benning"),
            lacks = listOf("Gus Mallory", "Megan Pruitt")
        ),
        AgentWideCase(
            task = "check off the vacuum noise thing on the puppy socialization schedule",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("vacuum")
        )
    )
}
