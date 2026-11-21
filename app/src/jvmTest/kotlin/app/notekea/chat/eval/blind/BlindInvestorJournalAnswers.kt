package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Investor Journal vault, three of them not answered by it. */
internal object BlindInvestorJournalAnswers {
    val all = listOf(
        AgentWideCase(
            task = "how much actually went into Tallow in October?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1600")
        ),
        AgentWideCase(
            task = "what's my average a month into Tallow from July to October, " +
                "only what went through",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1225")
        ),
        AgentWideCase(
            task = "in the FY2026 distributions how much came as cash rather than reinvested?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1011")
        ),
        AgentWideCase(
            task = "how many days until Ruth's binding nomination lapses?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("39 days")
        ),
        AgentWideCase(
            task = "how many days is Ruth's term deposit from opening to maturity?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("276 days")
        ),
        AgentWideCase(
            task = "how much bigger is my super than Ruth's, " +
                "going by the balances on the two Super notes?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("31420")
        ),
        AgentWideCase(
            task = "how much have I paid Tallow in fees since July, " +
                "not counting anything refunded?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("88")
        ),
        AgentWideCase(
            task = "what's Tallow's FX conversion fee?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("0.6")
        ),
        AgentWideCase(
            task = "who's the custodian for the Northgate fund?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Harbourline")
        ),
        AgentWideCase(
            task = "what's the buy/sell spread on NGI if I invested direct?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("0.08")
        ),
        AgentWideCase(
            task = "how much was my FY2026 tax refund in the end?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("1184.6"),
            lacks = listOf("refund was about $900")
        ),
        AgentWideCase(
            task = "how many BDX units did I buy in the Sep 2026 rebalance?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("43 units")
        ),
        AgentWideCase(
            task = "how far is my savings from the emergency fund target?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("8350")
        ),
        AgentWideCase(
            task = "what does the insurance in my super cost a week?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("11.40")
        ),
        AgentWideCase(
            task = "what did the Sep 2026 quarter distributions come to all up?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("217")
        ),
        AgentWideCase(
            task = "what's Ruth's super member number?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf(
                "ruth's member number is ks-448120",
                "her member number is ks-448120"
            )
        ),
        AgentWideCase(
            task = "what's Martin Osei's email?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("osei's email is martin@", "his email is martin@")
        ),
        AgentWideCase(
            task = "what rate will Ruth's term deposit roll over at?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("the rollover rate is 4.40", "will roll over at 4.40")
        )
    )
}
