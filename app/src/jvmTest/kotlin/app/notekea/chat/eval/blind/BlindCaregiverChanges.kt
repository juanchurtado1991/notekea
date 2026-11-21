package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes the caregiver asks for, each settled by its words and the vault. Written blind. */
internal object BlindCaregiverChanges {
    val all = listOf(
        AgentWideCase(
            task = "add a task to dad's appointments: fast after midnight before " +
                "the december bloodwork",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Appointments", "fast")
        ),
        AgentWideCase(
            task = "the lab claim appeal went through, mark CL-4510 approved",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("CL-4510", "approved")
        ),
        AgentWideCase(
            task = "tag mom's health summary with memory-care",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Health Summary", "memory-care")
        ),
        AgentWideCase(
            task = "set reviewed to 2026-11-11 on the care plan",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Care Plan", "reviewed", "2026-11-11")
        ),
        AgentWideCase(
            task = "log today's PT for mom: 340 ft with the cane, 25 min, Kara happy",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Physical Therapy Log", "340")
        ),
        AgentWideCase(
            task = "make me a note with every upcoming appointment for both of them, in date order",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Okafor", "Ilic", "Moravec", "Amsel")
        ),
        AgentWideCase(
            task = "move the october bills note into an Archive folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "create_folder", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Bills Paid October", "Archive")
        ),
        AgentWideCase(
            task = "rename the november timesheets note to Aide Timesheets 2026-11",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Aide Timesheets 2026-11")
        ),
        AgentWideCase(
            task = "dad's podiatry appt is now at 15:30, change it",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("podiatry", "15:30")
        ),
        AgentWideCase(
            task = "whichever sister hasn't sent november's money yet, " +
                "add a reminder task to her note",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Theresa Harlan")
        ),
        AgentWideCase(
            task = "Dr. Vasquez upped dad's metformin to 1000 mg, update his meds",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("metformin", "1000 mg")
        ),
        AgentWideCase(
            task = "add Kara Lindqvist (mom's PT) 937-555-0188 to the emergency contacts",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Emergency Contacts", "Kara Lindqvist", "555-0188")
        )
    )
}
