package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered (or honestly not answered) from the Blind Housing Coop vault. */
internal object BlindHousingCoopAnswers {
    val all = listOf(
        AgentWideCase(
            task = "how much do we still owe contractors in open invoices?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("5080"),
            lacks = listOf("6010")
        ),
        AgentWideCase(
            task = "total fee arrears still open, leaving out the Okafors since they're on a plan?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("848"),
            lacks = listOf("1444")
        ),
        AgentWideCase(
            task = "how many days until the next assembly?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("22 days")
        ),
        AgentWideCase(
            task = "how many days has Anna Novak lived here now?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("528 days")
        ),
        AgentWideCase(
            task = "how much is the renovation fund short of the renovation budget total?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("48885")
        ),
        AgentWideCase(
            task = "who's the technician that looks after our boiler?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Brandstätter")
        ),
        AgentWideCase(
            task = "per the bylaws who deals with a dispute between members first?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("arbitration committee")
        ),
        AgentWideCase(
            task = "how long can a member sublet their flat under the bylaws?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("24 months")
        ),
        AgentWideCase(
            task = "who did we elect as auditor in September?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Stastny")
        ),
        AgentWideCase(
            task = "what's the average monthly fee across all ten units?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("492.7")
        ),
        AgentWideCase(
            task = "total floor area of all the units in m²?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("645")
        ),
        AgentWideCase(
            task = "which unit pays the highest monthly fee and how much?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            holds = listOf("3A", "649")
        ),
        AgentWideCase(
            task = "how many days late were the Sorgers with their October fee?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("34 days")
        ),
        AgentWideCase(
            task = "what will the elevator safety inspection cost us?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("inspection costs 685", "inspection will cost 685")
        ),
        AgentWideCase(
            task = "what's Anna Berger's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            ends = End.ANSWER,
            lacks = listOf(
                "her number is 0664",
                "berger's number is 0664",
                "berger's phone is 0664"
            )
        ),
        AgentWideCase(
            task = "by how much are the fees going up in 2027?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("increase of 3", "goes up by 3", "rise of 3", "increase is 0.90")
        ),
        AgentWideCase(
            task = "what colour did we pick for the new facade?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("light ivory")
        ),
        AgentWideCase(
            task = "who's making the new courtyard benches?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Holzwerk Mayr")
        )
    )
}
