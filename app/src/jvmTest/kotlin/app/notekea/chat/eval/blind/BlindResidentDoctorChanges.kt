package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Changes to the resident doctor vault, each clear from the words and the notes. */
internal object BlindResidentDoctorChanges {
    val all = listOf(
        change(
            task = "add a todo to my CCU note: return the CCU badge by monday",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Cardiology CCU", "return the CCU badge")
        ),
        change(
            task = "the abstract got in! set its status to accepted",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "set_property", "propose"),
            holds = listOf("Abstract - Thiazide hyponatremia", "accepted")
        ),
        change(
            task = "tag the endocarditis case #infectious-disease",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            holds = listOf("Endocarditis", "infectious-disease")
        ),
        change(
            task = "add to today's journal: pre-rounded on 9 patients before 6am",
            kind = Kind.DOCUMENT,
            tools = listOf("date", "read", "append", "propose"),
            holds = listOf("2026-11-11", "pre-rounded on 9 patients")
        ),
        change(
            task = "make a new note called Step 3 snapshot with my exam dates and my " +
                "practice form scores",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "write_note", "propose"),
            holds = listOf("Step 3 snapshot", "58", "74")
        ),
        change(
            task = "move the night float note into an Archive folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "create_folder", "move", "propose"),
            holds = listOf("Night float", "Archive")
        ),
        change(
            task = "rename the syncope case to Case 05 - Vasovagal syncope",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            holds = listOf("Vasovagal syncope")
        ),
        change(
            task = "form D was actually 75 not 74, fix it",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Form D", "75")
        ),
        change(
            task = "tag every case from the CCU with #ccu",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "add_tag", "add_tag", "propose"),
            holds = listOf("Endocarditis", "Syncope")
        ),
        change(
            task = "add a task to every attending's note: send a holiday card",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "add_task", "add_task", "add_task", "propose"),
            holds = listOf("Daniel Kim", "Grace Kim", "Okafor")
        ),
        change(
            task = "put a task in the conference trip note: apply for the travel grant by 11/24",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "add_task", "propose"),
            holds = listOf("Conference trip", "travel grant")
        ),
        change(
            task = "tag the cases I already presented at morning report with #presented",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "add_tag", "add_tag", "propose"),
            holds = listOf("DKA", "AKI on CKD")
        )
    )
}
