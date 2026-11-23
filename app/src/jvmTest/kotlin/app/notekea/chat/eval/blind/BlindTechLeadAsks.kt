package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindTechLeadAsks {
    val all = listOf(
        ask(
            task = "add a task to the budget note: double-check the numbers before month end",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Hiring")
        ),
        ask(
            task = "move my 1:1 with Priya to Friday 11:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Raman", "Castell")
        ),
        ask(
            task = "reschedule Leo's interview to Monday at 10",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Brandt", "Ferraz")
        ),
        ask(
            task = "push the renewal back two weeks",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Swarmline", "Lintora")
        ),
        ask(
            task = "swap my next on-call week with Jonas",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("2026-11-16", "2026-12-07")
        ),
        ask(
            task = "push the payouts milestone back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Beta", "GA")
        ),
        ask(
            task = "mark the open action item from the webhook postmortem as done",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("queue depth", "jitter")
        ),
        // Traps: they look ambiguous, but the words settle them.
        answer(
            task = "what's the total hiring budget for this year?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("30000")
        ),
        change(
            task = "add a task for Priya Castell: review the rollback plan by 2026-11-20",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Priya Castell", "2026-11-20"),
            lacks = listOf("Raman")
        ),
        // Two "Open questions" notes, but only the project-level one takes an ownership question.
        change(
            task = "add 'should Jonas own the cutover runbook?' to my open questions",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "add_task", "propose"),
            holds = listOf("cutover runbook", "project"),
            lacks = listOf("Decisions")
        ),
        answer(
            task = "what time is the onsite with Leo?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("14:00"),
            lacks = listOf("10:30")
        ),
        answer(
            task = "when's my next on-call week?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("2026-11-23")
        ),
        change(
            task = "tag both Priyas' notes with #promo-cycle",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_tag", "add_tag", "propose"),
            holds = listOf("Priya Raman", "Priya Castell", "promo-cycle")
        )
    )
}
