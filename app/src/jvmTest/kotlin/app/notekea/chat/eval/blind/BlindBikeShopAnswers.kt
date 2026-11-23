package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered from the Blind Bike Shop vault, three of them not answered by the notes. */
internal object BlindBikeShopAnswers {
    val all = listOf(
        answer(
            task = "how much did Femke end up paying for her service after the bar tape refund?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("190")
        ),
        answer(
            task = "how many days until the christmas ride?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("39 days")
        ),
        answer(
            task = "how many days was Noor's wheel in the shop before it was ready?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("22 days")
        ),
        answer(
            task = "how much is still unpaid across all my invoices?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("411.50")
        ),
        answer(
            task = "how many people are in the ride group?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "count", "answer"),
            computed = listOf("19")
        ),
        answer(
            task = "whats the average number of riders on the autumn rides so far?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("13.5")
        ),
        answer(
            task = "how long are e-bike batteries under warranty?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("18 months")
        ),
        answer(
            task = "how long does a customer have to report a defect?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("30 days")
        ),
        answer(
            task = "what day do the old tyres get picked up?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Thursday")
        ),
        answer(
            task = "what time do we start the gooi ride?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("9:00")
        ),
        answer(
            task = "how many km longer is the gooi ride than the christmas ride?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            computed = listOf("46")
        ),
        answer(
            task = "who's supplying the pads for Lars de Wit's cargo bike?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("Remwerk")
        ),
        answer(
            task = "what do my monthly costs come to without the loan repayment?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("1864")
        ),
        answer(
            task = "how much rental money do I get from the november bookings, not counting " +
                "the cancelled ones?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("460")
        ),
        answer(
            task = "which repair tickets are still open?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            holds = listOf("T-1038", "T-1052", "T-1053")
        ),
        answer(
            task = "what's Lars de Wit's email?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("email is lars")
        ),
        answer(
            task = "how much is the deposit for a tandem?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("tandem deposit is €", "for a tandem is €")
        ),
        answer(
            task = "what time does the christmas ride start?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("christmas ride starts at 0", "christmas ride starts at 1")
        )
    )
}
