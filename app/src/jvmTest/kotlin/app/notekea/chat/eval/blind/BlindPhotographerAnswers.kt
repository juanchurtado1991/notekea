package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindPhotographerAnswers {
    val all = listOf(
        AgentWideCase(
            task = "what's the total of my unpaid invoices?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("5035")
        ),
        AgentWideCase(
            task = "how much did the paid invoices actually bring in, after the Calder refund?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("2150"),
            lacks = listOf("2285")
        ),
        AgentWideCase(
            task = "what did the business spend in october? skip the personal stuff",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("580"),
            lacks = listOf("672", "644", "608")
        ),
        AgentWideCase(
            task = "how much am I paying marcus for the pike gala and the ellis wedding together?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("495")
        ),
        AgentWideCase(
            task = "how many days do I have left to deliver the hollis gallery?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("32 days")
        ),
        AgentWideCase(
            task = "per my contract how long do galleries stay online, " +
                "and what's the reactivation fee?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("18 months", "75")
        ),
        AgentWideCase(
            task = "how much did I pay for the 85mm?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("2299")
        ),
        AgentWideCase(
            task = "where do vendors park for the ellis wedding?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("lot C")
        ),
        AgentWideCase(
            task = "which galleries still haven't been delivered?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Westbrook", "Sandoval", "Hollis"),
            lacks = listOf("Lowe")
        ),
        AgentWideCase(
            task = "of the upcoming bookings with the deposit paid, " +
                "which one has the biggest balance left?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Ellis", "2730")
        ),
        AgentWideCase(
            task = "how many days till teresa's wedding?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("157 days")
        ),
        AgentWideCase(
            task = "what did I pay in total for the gear I still own?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("16991"),
            lacks = listOf("18089")
        ),
        AgentWideCase(
            task = "is my editing laptop covered by the gear insurance?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("renter")
        ),
        AgentWideCase(
            task = "what photo did the ellis mom ask for on our call?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("quilt")
        ),
        AgentWideCase(
            task = "how many days until jordan ellis's balance is due?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("10 days")
        ),
        AgentWideCase(
            task = "what's marcus's instagram?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("@lenalights")
        ),
        AgentWideCase(
            task = "what did I charge for the lowe baby shower?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("1800")
        ),
        AgentWideCase(
            task = "what time does teresa's wedding start?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("13:00")
        )
    )
}
