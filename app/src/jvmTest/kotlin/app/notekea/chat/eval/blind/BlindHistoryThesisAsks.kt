package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindHistoryThesisAsks {
    val all = listOf(
        AgentWideCase(
            task = "add a task to Prof. Grant's note: sign the annual review form",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Alasdair", "Helen")
        ),
        AgentWideCase(
            task = "rename my reading list note to Reading list 2026-27",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "move my NRS visit to the 26th",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Kirkcaldy", "Board of Trade")
        ),
        AgentWideCase(
            task = "add to Callum's note that he came to office hours today",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Reid", "Baird")
        ),
        AgentWideCase(
            task = "mark the archive request as arrived",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK,
            holds = listOf("Kirkcaldy", "Humber Dock")
        ),
        AgentWideCase(
            task = "move my tutorial to Room 3.11 from now on",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            ends = End.ASK
        ),
        AgentWideCase(
            task = "add a task to my conference note: book flights",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Utrecht", "Lisbon")
        ),
        AgentWideCase(
            task = "add a task to my chapter draft: recheck the footnotes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            ends = End.ASK,
            holds = listOf("Chapter 1", "Chapter 4")
        ),
        AgentWideCase(
            task = "add a task to Alasdair Grant's note: send him the cut chapter 2 by 2026-11-20",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Alasdair Grant", "2026-11-20")
        ),
        AgentWideCase(
            task = "change Helen Grant's role to co-supervisor",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "set_property", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Helen Grant", "co-supervisor")
        ),
        AgentWideCase(
            task = "which tutorial group is Callum Baird in?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Group B")
        ),
        AgentWideCase(
            task = "what's the week 9 reading on my tutorial reading list?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Bread and Ballots")
        ),
        AgentWideCase(
            task = "add a task to both Grants' notes: sign the annual review form",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            ends = End.PROPOSE,
            holds = listOf("Alasdair Grant", "Helen Grant")
        )
    )
}
