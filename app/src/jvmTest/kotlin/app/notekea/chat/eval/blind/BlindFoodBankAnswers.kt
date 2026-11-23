package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

/** Questions answered (or honestly not answered) from the food bank vault. */
internal object BlindFoodBankAnswers {
    val all = listOf(
        answer(
            task = "how many pounds did Riverside give us in October?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("3075"),
            lacks = listOf("4180")
        ),
        answer(
            task = "total pounds donated in october?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("5067"),
            lacks = listOf("6172")
        ),
        answer(
            task = "how many households did we serve on the October distribution days?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("764")
        ),
        answer(
            task = "what was our average households per distribution day in oct",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("191")
        ),
        answer(
            task = "how many days do I have left before the grant report is due?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            computed = listOf("34 days")
        ),
        answer(
            task = "how long can thawed meat sit in the walk-in before we toss it?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("48 hours")
        ),
        answer(
            task = "who's our program officer at the Schuylkill fund again?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Renata Vos")
        ),
        answer(
            task = "who do we ask for when we pick up at Fairmount Bakery?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "answer"),
            holds = listOf("Teodoro")
        ),
        answer(
            task = "how big is Harlan's last delivery of the season, and what is it?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "search", "read", "answer"),
            holds = listOf("900", "sweet potatoes")
        ),
        answer(
            task = "october hours for individual volunteers only, leave out the school group",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("76")
        ),
        answer(
            task = "how many cases of canned food can we hand out right now? skip anything on hold",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("163")
        ),
        answer(
            task = "which donor has given us the most pounds so far this month?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            holds = listOf("Brigid"),
            computed = listOf("1390")
        ),
        answer(
            task = "how many days between Luis's orientation and his first shift?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "links", "read", "date", "answer"),
            computed = listOf("27 days")
        ),
        answer(
            task = "did we serve more households on Nov 8 than on our best October day?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            holds = listOf("223", "211")
        ),
        answer(
            task = "how many turkeys are we still waiting on for thanksgiving?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            computed = listOf("125")
        ),
        answer(
            task = "what's Marcus Bell's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("215-555-0148", "267-555-0192")
        ),
        answer(
            task = "how much is the chest freezer going to cost us?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("freezer costs $3200", "freezer is $3200")
        ),
        answer(
            task = "when's Grace Mensah's birthday?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("march 3")
        )
    )
}
