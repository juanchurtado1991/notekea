package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindTeacherChanges {
    val all = listOf(
        AgentWideCase(
            task = "add a todo to the museum trip note: call the bus company to confirm pickup",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Labor History Museum Trip", "bus company")
        ),
        AgentWideCase(
            task = "set the museum trip status to confirmed",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Labor History Museum Trip", "confirmed")
        ),
        AgentWideCase(
            task = "Omar Haddad paid the rest of his trip fee, update the slips",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Permission Slips", "Omar Haddad", "18")
        ),
        AgentWideCase(
            task = "log yesterday's Teaching Hard History webinar in my PD hours, 2 hours, pending",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("PD Hours", "Teaching Hard History", "2026-11-10")
        ),
        AgentWideCase(
            task = "make a note listing everyone who returned a slip but still owes trip money",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Jayden Morales", "Jayden Price", "Omar Haddad"),
            lacks = listOf("Diego Ramirez", "Lucas Ferreira")
        ),
        AgentWideCase(
            task = "rename the Constitution Day Lesson note to Constitution Day 2026",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Constitution Day 2026")
        ),
        AgentWideCase(
            task = "move the Progressive Era DBQ note into the Classes folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Progressive Era DBQ", "Classes")
        ),
        AgentWideCase(
            task = "the curriculum review meeting is in room 214 now, fix it",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Curriculum review", "214")
        ),
        AgentWideCase(
            task = "tag every student note of a kid going on the museum trip with #field-trip",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Jayden Morales", "Jayden Price", "field-trip"),
            lacks = listOf("Aaliyah Grant")
        ),
        AgentWideCase(
            task = "add to yesterday's journal that Priya Raman's slip came in",
            kind = Kind.DOCUMENT,
            tools = listOf("date", "read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-11-10", "Priya Raman")
        ),
        AgentWideCase(
            task = "write a note called Sub Plan 2026-12-04 with the current unit of each class " +
                "the sub covers that day",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Sub Plan 2026-12-04", "Industrial Revolution", "Elections and Voting"),
            lacks = listOf("Reconstruction")
        ),
        AgentWideCase(
            task = "Isabella Cruz got 76 on her unit 2 retake, put it in the gradebook",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Period 3 Gradebook", "Isabella Cruz", "76")
        )
    )
}
