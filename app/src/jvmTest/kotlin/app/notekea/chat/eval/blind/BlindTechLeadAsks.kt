package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindTechLeadAsks {
    val all = listOf(
        AgentWideCase(
            task = "add a task to the budget note: double-check the numbers before month end",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Hiring")
        ),
        AgentWideCase(
            task = "move my 1:1 with Priya to Friday 11:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Raman", "Castell")
        ),
        AgentWideCase(
            task = "reschedule Leo's interview to Monday at 10",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Brandt", "Ferraz")
        ),
        AgentWideCase(
            task = "push the renewal back two weeks",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Swarmline", "Lintora")
        ),
        AgentWideCase(
            task = "swap my next on-call week with Jonas",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("2026-11-16", "2026-12-07")
        ),
        AgentWideCase(
            task = "push the payouts milestone back a week",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Beta", "GA")
        ),
        AgentWideCase(
            task = "mark the open action item from the webhook postmortem as done",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("queue depth", "jitter")
        ),
        // Traps: they look ambiguous, but the words settle them.
        AgentWideCase(
            task = "what's the total hiring budget for this year?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("30000")
        ),
        AgentWideCase(
            task = "add a task for Priya Castell: review the rollback plan by 2026-11-20",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Priya Castell", "2026-11-20"),
            lacks = listOf("Raman")
        ),
        // Two "Open questions" notes, but only the project-level one takes an ownership question.
        AgentWideCase(
            task = "add 'should Jonas own the cutover runbook?' to my open questions",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "read", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("cutover runbook", "project"),
            lacks = listOf("Decisions")
        ),
        AgentWideCase(
            task = "what time is the onsite with Leo?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("14:00"),
            lacks = listOf("10:30")
        ),
        AgentWideCase(
            task = "when's my next on-call week?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("2026-11-23")
        ),
        AgentWideCase(
            task = "tag both Priyas' notes with #promo-cycle",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_tag", "add_tag", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Priya Raman", "Priya Castell", "promo-cycle")
        )
    )
}
