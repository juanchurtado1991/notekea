package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindFreelanceWriterAsks {
    val all = listOf(
        AgentWideCase(
            task = "what's Declan's number?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Moran", "Fitzgerald")
        ),
        AgentWideCase(
            task = "add a task to send Declan his quotes to check",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Moran", "Fitzgerald")
        ),
        AgentWideCase(
            task = "move the interview to 4pm",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Fitzgerald", "Cullen", "Brady")
        ),
        AgentWideCase(
            task = "what's the next date on my timeline?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Research", "Book")
        ),
        AgentWideCase(
            task = "add a section called Open questions at the end of my outline",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Research", "Book")
        ),
        AgentWideCase(
            task = "push the Northlight deadline back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Canal swimmers", "Greenmoor")
        ),
        AgentWideCase(
            task = "add a follow-up task for the pending pitch",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("night bus", "Seaweed", "Courts Service")
        ),
        AgentWideCase(
            task = "mark the overdue invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("INV-2026-034", "INV-2026-038")
        ),
        AgentWideCase(
            task = "change the transcription service expense to €75",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("August", "September", "October")
        ),
        // Traps: they look ambiguous, but their words settle them.
        AgentWideCase(
            task = "what's Declan Moran's number?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("087 214 6630"),
            lacks = listOf("085 903 1172")
        ),
        AgentWideCase(
            task = "add to the research timeline: 2026-11-03 FOI-03 released, 14 pages of" +
                " complaints",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Timeline", "2026-11-03", "FOI-03")
        ),
        AgentWideCase(
            task = "tag both Declans with #followup",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Declan Moran", "Declan Fitzgerald", "followup")
        ),
        AgentWideCase(
            task = "move the Peter Cullen interview to 16:00, same day",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Peter Cullen", "16:00")
        ),
        AgentWideCase(
            task = "if every chapter in the book outline runs 9,000 words, what's the total?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "count", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("81000")
        )
    )
}
