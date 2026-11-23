package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes the caregiver asks for, each settled by its words and the vault. Written blind. */
internal object BlindCaregiverChanges {
    val all = listOf(
        change(
            task = "add a task to dad's appointments: fast after midnight before " +
                "the december bloodwork",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Appointments", "fast")
        ),
        change(
            task = "the lab claim appeal went through, mark CL-4510 approved",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("CL-4510", "approved")
        ),
        change(
            task = "tag mom's health summary with memory-care",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            holds = listOf("Health Summary", "memory-care")
        ),
        change(
            task = "set reviewed to 2026-11-11 on the care plan",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Care Plan", "reviewed", "2026-11-11")
        ),
        change(
            task = "log today's PT for mom: 340 ft with the cane, 25 min, Kara happy",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "append", "propose"),
            holds = listOf("Physical Therapy Log", "340")
        ),
        change(
            task = "make me a note with every upcoming appointment for both of them, in date order",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "write_note", "propose"),
            holds = listOf("Okafor", "Ilic", "Moravec", "Amsel")
        ),
        change(
            task = "move the october bills note into an Archive folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "create_folder", "move", "propose"),
            holds = listOf("Bills Paid October", "Archive")
        ),
        change(
            task = "rename the november timesheets note to Aide Timesheets 2026-11",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Aide Timesheets 2026-11")
        ),
        change(
            task = "dad's podiatry appt is now at 15:30, change it",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("podiatry", "15:30")
        ),
        change(
            task = "whichever sister hasn't sent november's money yet, " +
                "add a reminder task to her note",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "add_task", "propose"),
            holds = listOf("Theresa Harlan")
        ),
        change(
            task = "Dr. Vasquez upped dad's metformin to 1000 mg, update his meds",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("metformin", "1000 mg")
        ),
        change(
            task = "add Kara Lindqvist (mom's PT) 937-555-0188 to the emergency contacts",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "append", "propose"),
            holds = listOf("Emergency Contacts", "Kara Lindqvist", "555-0188")
        )
    )
}
