package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered (or honestly not answered) from the Blind Housing Coop vault. */
internal object BlindHousingCoopAnswers {
    val all = listOf(
        answer(
            task = "how much do we still owe contractors in open invoices?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("5080"),
            lacks = listOf("6010")
        ),
        answer(
            task = "total fee arrears still open, leaving out the Okafors since they're on a plan?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("848"),
            lacks = listOf("1444")
        ),
        answer(
            task = "how many days until the next assembly?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("22 days")
        ),
        answer(
            task = "how many days has Anna Novak lived here now?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            computed = listOf("528 days")
        ),
        answer(
            task = "how much is the renovation fund short of the renovation budget total?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            computed = listOf("48885")
        ),
        answer(
            task = "who's the technician that looks after our boiler?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "links", "read", "answer"),
            holds = listOf("Brandstätter")
        ),
        answer(
            task = "per the bylaws who deals with a dispute between members first?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("arbitration committee")
        ),
        answer(
            task = "how long can a member sublet their flat under the bylaws?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("24 months")
        ),
        answer(
            task = "who did we elect as auditor in September?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Stastny")
        ),
        answer(
            task = "what's the average monthly fee across all ten units?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("492.7")
        ),
        answer(
            task = "total floor area of all the units in m²?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("645")
        ),
        answer(
            task = "which unit pays the highest monthly fee and how much?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            holds = listOf("3A", "649")
        ),
        answer(
            task = "how many days late were the Sorgers with their October fee?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "date", "answer"),
            computed = listOf("34 days")
        ),
        answer(
            task = "what will the elevator safety inspection cost us?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("inspection costs 685", "inspection will cost 685")
        ),
        answer(
            task = "what's Anna Berger's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            lacks = listOf(
                "her number is 0664",
                "berger's number is 0664",
                "berger's phone is 0664"
            )
        ),
        answer(
            task = "by how much are the fees going up in 2027?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            lacks = listOf("increase of 3", "goes up by 3", "rise of 3", "increase is 0.90")
        ),
        answer(
            task = "what colour did we pick for the new facade?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("light ivory")
        ),
        answer(
            task = "who's making the new courtyard benches?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("Holzwerk Mayr")
        )
    )
}
