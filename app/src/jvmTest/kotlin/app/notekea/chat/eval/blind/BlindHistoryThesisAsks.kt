package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindHistoryThesisAsks {
    val all = listOf(
        ask(
            task = "add a task to Prof. Grant's note: sign the annual review form",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Alasdair", "Helen")
        ),
        ask(
            task = "rename my reading list note to Reading list 2026-27",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user")
        ),
        ask(
            task = "move my NRS visit to the 26th",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Kirkcaldy", "Board of Trade")
        ),
        ask(
            task = "add to Callum's note that he came to office hours today",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Reid", "Baird")
        ),
        ask(
            task = "mark the archive request as arrived",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user"),
            holds = listOf("Kirkcaldy", "Humber Dock")
        ),
        ask(
            task = "move my tutorial to Room 3.11 from now on",
            kind = Kind.AMBIGUOUS,
            tools = listOf("read", "ask_user")
        ),
        ask(
            task = "add a task to my conference note: book flights",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Utrecht", "Lisbon")
        ),
        ask(
            task = "add a task to my chapter draft: recheck the footnotes",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Chapter 1", "Chapter 4")
        ),
        change(
            task = "add a task to Alasdair Grant's note: send him the cut chapter 2 by 2026-11-20",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Alasdair Grant", "2026-11-20")
        ),
        change(
            task = "change Helen Grant's role to co-supervisor",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "set_property", "propose"),
            holds = listOf("Helen Grant", "co-supervisor")
        ),
        answer(
            task = "which tutorial group is Callum Baird in?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Group B")
        ),
        answer(
            task = "what's the week 9 reading on my tutorial reading list?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Bread and Ballots")
        ),
        change(
            task = "add a task to both Grants' notes: sign the annual review form",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Alasdair Grant", "Helen Grant")
        )
    )
}
