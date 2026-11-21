package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindApplicantChanges {
    val all = listOf(
        AgentWideCase(
            task = "I just submitted Rainier State, set its status to submitted",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Rainier State University", "submitted")
        ),
        AgentWideCase(
            task = "tag the pacific robotics grant #stem",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Pacific Robotics Grant", "stem")
        ),
        AgentWideCase(
            task = "add to Dr. Abernathy's note that she prefers printed drafts with wide margins",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Ruth Abernathy", "printed drafts")
        ),
        AgentWideCase(
            task = "make a new note called Fees Paid listing the application fees I've " +
                "already paid",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Fees Paid", "Northbridge", "Cascade Pacific", "Sound Valley")
        ),
        AgentWideCase(
            task = "move the PTSA senior award note into the financial aid folder",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "move", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Lakeview PTSA Senior Award", "Financial Aid")
        ),
        AgentWideCase(
            task = "rename Feedback Log to Essay Feedback Log",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "rename", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Essay Feedback Log")
        ),
        AgentWideCase(
            task = "tag every scholarship whose deadline is still this year with #urgent",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf(
                "search",
                "read",
                "read",
                "read",
                "read",
                "add_tag",
                "add_tag",
                "propose"
            ),
            ends = End.PROPOSE,
            holds = listOf(
                "Greenlake Civic League Award",
                "Pacific Robotics Grant",
                "urgent"
            )
        ),
        AgentWideCase(
            task = "add a task 'send thank-you card' to the note of each recommender whose " +
                "letter is already submitted",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "add_task", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Daniel Ortiz", "Grace Lee", "thank-you card")
        ),
        AgentWideCase(
            task = "drop my rainier state tour rating to 6",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Rainier State Tour", "6")
        ),
        AgentWideCase(
            task = "add to my nov 10 journal that i submitted the css profile",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "append", "propose"),
            ends = End.PROPOSE,
            holds = listOf("2026-11-10", "CSS Profile")
        ),
        AgentWideCase(
            task = "write a note in financial aid comparing the sound valley offer with rainier " +
                "state's estimate",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "write_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Sound Valley", "Rainier State", "16,750")
        ),
        AgentWideCase(
            task = "Dr. Abernathy's letter is in, mark it submitted in my rec letters",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Recommendation Letters", "Abernathy", "submitted")
        )
    )
}
