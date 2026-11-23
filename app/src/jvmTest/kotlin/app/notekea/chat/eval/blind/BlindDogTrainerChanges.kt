package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindDogTrainerChanges {
    val all = listOf(
        change(
            task = "add a 40 lb bag of puppy kibble to my shopping list",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_task", "propose"),
            holds = listOf("Shopping List", "puppy kibble")
        ),
        change(
            task = "set the litter note's status to going home",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "set_property", "propose"),
            holds = listOf("Litter Juniper 2026", "going home")
        ),
        change(
            task = "tag Walt's note board-and-train",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "add_tag", "propose"),
            holds = listOf("Walt Hendricks", "board-and-train")
        ),
        change(
            task = "add to Lucy's note that Pip nailed her tunnel intro today",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "append", "propose"),
            holds = listOf("Lucy Fernsby", "tunnel")
        ),
        change(
            task = "make me a Pickup Day note: each buyer, their puppy's collar, pickup time " +
                "and the balance they owe at pickup",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "write_note", "propose"),
            holds = listOf("Pickup Day", "Tom Delgado", "Carla Benning"),
            computed = listOf("1900", "2050"),
            lacks = listOf("Gus Mallory")
        ),
        change(
            task = "puppy basics fall 2026 is over, put it in an Archive folder",
            kind = Kind.DOCUMENT,
            tools = listOf("create_folder", "move", "propose"),
            holds = listOf("Puppy Basics Fall 2026", "Archive")
        ),
        change(
            task = "Carla got married, rename her note to Carla Benning-Ortiz",
            kind = Kind.DOCUMENT,
            tools = listOf("rename", "propose"),
            holds = listOf("Carla Benning-Ortiz")
        ),
        change(
            task = "change Priya's pickup time to 12:00",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Priya", "12:00")
        ),
        change(
            task = "tag every client with an unpaid invoice #unpaid",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "add_tag", "propose"),
            holds = listOf("Walt Hendricks", "Megan Ashby", "Priya Nair"),
            lacks = listOf("Lucy Fernsby", "Megan Pruitt")
        ),
        change(
            task = "add a task to each puppy buyer's note: send the vaccination record",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "add_task", "propose"),
            holds = listOf("Tom Delgado", "Priya Nair", "Megan Ashby", "Carla Benning"),
            lacks = listOf("Gus Mallory", "Megan Pruitt")
        ),
        change(
            task = "check off the vacuum noise thing on the puppy socialization schedule",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("vacuum")
        )
    )
}
