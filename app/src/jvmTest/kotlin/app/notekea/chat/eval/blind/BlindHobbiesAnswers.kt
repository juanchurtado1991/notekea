package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

internal object BlindHobbiesAnswers {
    val all = listOf(
        AgentWideCase(
            task = "how much did kyoto cost me in the end?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("3834")
        ),
        AgentWideCase(
            task = "if i'd slept seven nights at my kyoto hotel, how much more would that " +
                "have cost than my seven nights at the azores guesthouse?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("427")
        ),
        AgentWideCase(
            task = "what was the name of the boat on my whale watching in the azores?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Gaivota Azul")
        ),
        AgentWideCase(
            task = "how many days until i fly to scotland?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("184")
        ),
        AgentWideCase(
            task = "how many days do i have left to pay the edinburgh hotel deposit?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "date", "answer"),
            ends = End.ANSWER,
            computed = listOf("65")
        ),
        AgentWideCase(
            task = "how many pages long were the books i finished this year, all together?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1822"),
            lacks = listOf("1882")
        ),
        AgentWideCase(
            task = "how many pages were the books i finished this year outside the book " +
                "club, together?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("952")
        ),
        AgentWideCase(
            task = "where is the december book club meeting?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Alvalade")
        ),
        AgentWideCase(
            task = "when is my jlpt exam again?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("2026-12-06")
        ),
        AgentWideCase(
            task = "how much have i actually paid kenji for lessons so far?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("154"),
            lacks = listOf("176")
        ),
        AgentWideCase(
            task = "how many kilos of tomatoes did i pick this year in total?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("10.6")
        ),
        AgentWideCase(
            task = "how much are my pending sementes ribeira orders, all together?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("37.05"),
            lacks = listOf("49.05")
        ),
        AgentWideCase(
            task = "how much did i pay for the camera gear i still have?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("1596"),
            lacks = listOf("2006")
        ),
        AgentWideCase(
            task = "on average how many hours long was each of my october food bank shifts?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "calculate", "answer"),
            ends = End.ANSWER,
            computed = listOf("3.75")
        ),
        AgentWideCase(
            task = "what was the name of the hotel where i met margaret?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Kamogawa Rest")
        ),
        AgentWideCase(
            task = "what's kenji's phone number?",
            kind = Kind.DOCUMENT,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("213 870 442")
        ),
        AgentWideCase(
            task = "how many kilos of potatoes did i get this year?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("10.6")
        ),
        AgentWideCase(
            task = "how much did i spend on food in the azores?",
            kind = Kind.LONG_NOTE,
            tools = listOf("search", "read", "answer"),
            ends = End.ANSWER,
            lacks = listOf("610")
        )
    )
}
