package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindPodcastAsks {
    val all = listOf(
        ask(
            task = "add to Priya's note that she's away until January",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Nair", "Desai")
        ),
        ask(
            task = "push the recording back an hour",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Lucia", "Dev", "Marguerite")
        ),
        ask(
            task = "tick off the first item on my checklist",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user")
        ),
        ask(
            task = "mark Sam Whitford's invoice as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("142", "143")
        ),
        ask(
            task = "add a task to call Sam on Friday",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("Whitford", "Osei")
        ),
        ask(
            task = "mark the Northfield read as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("141", "142")
        ),
        ask(
            task = "mark the guest who never replied as declined",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "ask_user"),
            holds = listOf("Felix", "Rosalind")
        ),
        ask(
            task = "add 'review these in January' to my rates note",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "ask_user"),
            holds = listOf("sponsor")
        ),
        change(
            task = "tag Priya Nair's guest note #returning-guest",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_tag", "propose"),
            holds = listOf("Priya Nair", "returning-guest")
        ),
        change(
            task = "move Dev's recording to 11:00",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Dev Raman", "11:00")
        ),
        change(
            task = "add a task to Sam Osei's note: confirm Studio B for the 27th",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "add_task", "propose"),
            holds = listOf("Sam Osei", "Studio B")
        ),
        change(
            task = "mark both unpaid Northfield reads as paid",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "edit_note", "propose"),
            holds = listOf("Northfield", "141", "142")
        ),
        answer(
            task = "how much do I owe Sam Whitford on invoices he has already sent?",
            kind = Kind.AMBIGUOUS,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("560")
        )
    )
}
