package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindFreelanceWriterAsks {
    val all = listOf(
        ask(
            task = "what's Declan's number?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Moran", "Fitzgerald")
        ),
        ask(
            task = "add a task to send Declan his quotes to check",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Moran", "Fitzgerald")
        ),
        ask(
            task = "move the interview to 4pm",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Fitzgerald", "Cullen", "Brady")
        ),
        ask(
            task = "what's the next date on my timeline?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Research", "Book")
        ),
        ask(
            task = "add a section called Open questions at the end of my outline",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Research", "Book")
        ),
        ask(
            task = "push the Northlight deadline back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Canal swimmers", "Greenmoor")
        ),
        ask(
            task = "add a follow-up task for the pending pitch",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("night bus", "Seaweed", "Courts Service")
        ),
        ask(
            task = "mark the overdue invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("INV-2026-034", "INV-2026-038")
        ),
        ask(
            task = "change the transcription service expense to €75",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("August", "September", "October")
        ),
        // Traps: they look ambiguous, but their words settle them.
        answer(
            task = "what's Declan Moran's number?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("087 214 6630"),
            lacks = listOf("085 903 1172")
        ),
        change(
            task = "add to the research timeline: 2026-11-03 FOI-03 released, 14 pages of" +
                " complaints",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Timeline", "2026-11-03", "FOI-03")
        ),
        change(
            task = "tag both Declans with #followup",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_tag", "add_tag", "propose"),
            holds = listOf("Declan Moran", "Declan Fitzgerald", "followup")
        ),
        change(
            task = "move the Peter Cullen interview to 16:00, same day",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "edit_note", "propose"),
            holds = listOf("Peter Cullen", "16:00")
        ),
        answer(
            task = "if every chapter in the book outline runs 9,000 words, what's the total?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "count", "calculate", "answer"),
            computed = listOf("81000")
        )
    )
}
