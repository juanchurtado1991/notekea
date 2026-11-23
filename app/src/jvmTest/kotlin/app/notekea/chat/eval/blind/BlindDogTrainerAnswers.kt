package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindDogTrainerAnswers {
    val all = listOf(
        answer(
            task = "what's the average birth weight of juniper's litter?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("389")
        ),
        answer(
            task = "how much in puppy deposits am I actually holding right now?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("1750"),
            lacks = listOf("2250", "2750")
        ),
        answer(
            task = "how much does Priya still owe me at pickup for her pup?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            computed = listOf("2050")
        ),
        answer(
            task = "how many days between the puppies' first DHPP and their rabies shot?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            computed = listOf("70 days")
        ),
        answer(
            task = "how long is the health guarantee in my puppy contract?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            holds = listOf("26 months")
        ),
        answer(
            task = "if a buyer picks up late, what's the boarding fee per day in the contract?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            holds = listOf("35")
        ),
        answer(
            task = "how much is still owed to me on invoices?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("1780"),
            lacks = listOf("1875")
        ),
        answer(
            task = "what did the business really spend in october, litter included? " +
                "leave out my personal stuff and the refunded pen",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "calculate", "answer"),
            computed = listOf("708"),
            lacks = listOf("838", "950")
        ),
        answer(
            task = "did the litter cost me more in october or november so far? " +
                "don't count the refunded pen",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "calculate", "answer"),
            holds = listOf("October"),
            computed = listOf("353", "293")
        ),
        answer(
            task = "is Megan Ashby's puppy healthy? anything I have to tell her?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "search", "read", "answer"),
            holds = listOf("hernia")
        ),
        answer(
            task = "who's doing the temperament test and what time?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            holds = listOf("Joanne Ruiz", "10:00")
        ),
        answer(
            task = "how old are the puppies today in days?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "date", "answer"),
            computed = listOf("52 days")
        ),
        answer(
            task = "what med is Ranger on for vet visits?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            holds = listOf("trazodone")
        ),
        answer(
            task = "who still hasn't paid for puppy basics?",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "answer"),
            holds = listOf("Ines Varga", "Ray Colton")
        ),
        answer(
            task = "how much has the fall puppy basics class actually brought in? paid only",
            kind = Kind.DOCUMENT,
            tools = listOf("read", "count", "calculate", "answer"),
            computed = listOf("1050"),
            lacks = listOf("1680", "1260")
        ),
        answer(
            task = "what am I charging per dog for the reactive dog class in january?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("read", "read", "answer"),
            lacks = listOf("210")
        ),
        answer(
            task = "how much does Ranger weigh?",
            kind = Kind.LONG_NOTE,
            tools = listOf("read", "answer"),
            lacks = listOf("18 kg")
        ),
        answer(
            task = "how much did juniper's progesterone testing cost me?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            lacks = listOf("240")
        )
    )
}
