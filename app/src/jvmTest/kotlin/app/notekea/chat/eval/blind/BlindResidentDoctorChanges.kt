package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the resident doctor vault, each clear from the words and the notes. */
internal object BlindResidentDoctorChanges {
    val all = listOf(
        AgentWideCase(
            task = "add a todo to my CCU note: return the CCU badge by monday",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Cardiology CCU", "return the CCU badge")
        ),
        AgentWideCase(
            task = "the abstract got in! set its status to accepted",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Abstract - Thiazide hyponatremia", "accepted")
        ),
        AgentWideCase(
            task = "tag the endocarditis case #infectious-disease",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Endocarditis", "infectious-disease")
        ),
        AgentWideCase(
            task = "add to today's journal: pre-rounded on 9 patients before 6am",
            kind = Kind.DOCUMENT,
            tools = listOf("date", "read", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-11-11", "pre-rounded on 9 patients")
        ),
        AgentWideCase(
            task = "make a new note called Step 3 snapshot with my exam dates and my " +
                "practice form scores",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Step 3 snapshot", "58", "74")
        ),
        AgentWideCase(
            task = "move the night float note into an Archive folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "create_folder", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Night float", "Archive")
        ),
        AgentWideCase(
            task = "rename the syncope case to Case 05 - Vasovagal syncope",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Vasovagal syncope")
        ),
        AgentWideCase(
            task = "form D was actually 75 not 74, fix it",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Form D", "75")
        ),
        AgentWideCase(
            task = "tag every case from the CCU with #ccu",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Endocarditis", "Syncope")
        ),
        AgentWideCase(
            task = "add a task to every attending's note: send a holiday card",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "add_task", "add_task", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Daniel Kim", "Grace Kim", "Okafor")
        ),
        AgentWideCase(
            task = "put a task in the conference trip note: apply for the travel grant by 11/24",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Conference trip", "travel grant")
        ),
        AgentWideCase(
            task = "tag the cases I already presented at morning report with #presented",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("DKA", "AKI on CKD")
        )
    )
}
