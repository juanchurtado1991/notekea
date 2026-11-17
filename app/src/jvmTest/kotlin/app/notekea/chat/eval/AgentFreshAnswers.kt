package app.notekea.chat.eval

import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** The questions of [AgentFreshCases], answered from the `Fresh Eval` notes or honestly not. */
internal object AgentFreshAnswers {
    private val READ = listOf("search", "read", "answer")
    private val OUTLINE = listOf("search", "outline", "read", "answer")
    private val BOTH = listOf("search", "read", "read", "answer")

    val all = listOf(
        AgentWideCase(
            task = "How much did the shop sell in November, not counting the refunded order?",
            kind = Kind.LONG_NOTE,
            tools = READ,
            ends = End.ANSWER,
            computed = listOf("180"),
            lacks = listOf("216")
        ),
        AgentWideCase(
            task = "what did order #1045 come to with shipping?",
            kind = Kind.SEVERAL_NOTES,
            tools = BOTH,
            ends = End.ANSWER,
            computed = listOf("33")
        ),
        AgentWideCase(
            task = "How much have I budgeted for the Algarve bike trip in total?",
            kind = Kind.AMBIGUOUS,
            tools = READ,
            ends = End.ANSWER,
            computed = listOf("676"),
            lacks = listOf("950")
        ),
        AgentWideCase(
            task = "What's the total of the shop budget for 2027?",
            kind = Kind.AMBIGUOUS,
            tools = READ,
            ends = End.ANSWER,
            computed = listOf("950"),
            lacks = listOf("676")
        ),
        AgentWideCase(
            task = "How big was the deposit I paid for the studio?",
            kind = Kind.DOCUMENT,
            tools = OUTLINE,
            ends = End.ANSWER,
            holds = listOf("840")
        ),
        AgentWideCase(
            task = "What does it cost to replace a lost key at the studio?",
            kind = Kind.DOCUMENT,
            tools = OUTLINE,
            ends = End.ANSWER,
            holds = listOf("35")
        ),
        AgentWideCase(
            task = "¿Con cuántos días de antelación tengo que avisar si no quiero renovar " +
                "el estudio?",
            kind = Kind.DOCUMENT,
            tools = OUTLINE,
            ends = End.ANSWER,
            holds = listOf("60")
        ),
        AgentWideCase(
            task = "By what date do I have to tell the landlord if I don't want to renew " +
                "the studio?",
            kind = Kind.DOCUMENT,
            tools = OUTLINE,
            ends = End.ANSWER,
            computed = listOf("30"),
            lacks = listOf("31")
        ),
        AgentWideCase(
            task = "What did the studio cost me altogether in October, rent included?",
            kind = Kind.SEVERAL_NOTES,
            tools = BOTH,
            ends = End.ANSWER,
            computed = listOf("492")
        ),
        AgentWideCase(
            task = "Which players still haven't handed in their medical form?",
            kind = Kind.SEVERAL_NOTES,
            tools = READ,
            ends = End.ANSWER,
            holds = listOf("Noa", "Sara", "Daniela"),
            lacks = listOf("Carla", "Alba", "Irene")
        ),
        AgentWideCase(
            task = "who's our top scorer so far and how many goals has she got",
            kind = Kind.LONG_NOTE,
            tools = READ,
            ends = End.ANSWER,
            holds = listOf("Irene"),
            computed = listOf("4")
        ),
        AgentWideCase(
            task = "How many league points have we got this season?",
            kind = Kind.LONG_NOTE,
            tools = READ,
            ends = End.ANSWER,
            computed = listOf("10")
        ),
        AgentWideCase(
            task = "What will the Christmas tournament registration cost the team?",
            kind = Kind.SEVERAL_NOTES,
            tools = BOTH,
            ends = End.ANSWER,
            computed = listOf("180"),
            lacks = listOf("135")
        ),
        AgentWideCase(
            task = "How many days are left until the Christmas tournament?",
            kind = Kind.SEVERAL_NOTES,
            tools = READ,
            ends = End.ANSWER,
            computed = listOf("38")
        ),
        AgentWideCase(
            task = "Above what temperature do we have to give the girls water breaks, " +
                "and how often?",
            kind = Kind.DOCUMENT,
            tools = OUTLINE,
            ends = End.ANSWER,
            holds = listOf("30", "15")
        ),
        AgentWideCase(
            task = "What's the sibling discount on the club fee?",
            kind = Kind.DOCUMENT,
            tools = OUTLINE,
            ends = End.ANSWER,
            holds = listOf("20")
        ),
        AgentWideCase(
            task = "What position does the assistant coach's daughter play?",
            kind = Kind.SEVERAL_NOTES,
            tools = BOTH,
            ends = End.ANSWER,
            holds = listOf("Aitana", "defen")
        ),
        AgentWideCase(
            task = "¿Cuántas pastillas toma papá al día?",
            kind = Kind.SEVERAL_NOTES,
            tools = READ,
            ends = End.ANSWER,
            computed = listOf("5")
        ),
        AgentWideCase(
            task = "When is dad's next neurology appointment, and who with?",
            kind = Kind.SEVERAL_NOTES,
            tools = READ,
            ends = End.ANSWER,
            holds = listOf("Lamas", "10:30")
        ),
        AgentWideCase(
            task = "Is papá allergic to any medicine?",
            kind = Kind.DOCUMENT,
            tools = OUTLINE,
            ends = End.ANSWER,
            holds = listOf("penicil")
        ),
        AgentWideCase(
            task = "What should whoever is with dad do if he falls and hits his head?",
            kind = Kind.DOCUMENT,
            tools = OUTLINE,
            ends = End.ANSWER,
            holds = listOf("112")
        ),
        AgentWideCase(
            task = "¿Cuánto le debo a Lucía por las horas de octubre?",
            kind = Kind.AMBIGUOUS,
            tools = BOTH,
            ends = End.ANSWER,
            computed = listOf("870"),
            lacks = listOf("990")
        ),
        AgentWideCase(
            task = "How much do I owe Marco for the lessons I haven't paid yet?",
            kind = Kind.SEVERAL_NOTES,
            tools = BOTH,
            ends = End.ANSWER,
            computed = listOf("50")
        ),
        AgentWideCase(
            task = "¿Cómo se dice «grifo» en portugués según mis notas?",
            kind = Kind.SEVERAL_NOTES,
            tools = READ,
            ends = End.ANSWER,
            holds = listOf("torneira")
        ),
        AgentWideCase(
            task = "How much is the CIPLE exam fee?",
            kind = Kind.SEVERAL_NOTES,
            tools = READ,
            ends = End.ANSWER,
            holds = listOf("85")
        ),
        AgentWideCase(
            task = "How many kilos of tomatoes did I pick this year?",
            kind = Kind.LONG_NOTE,
            tools = READ,
            ends = End.ANSWER,
            computed = listOf("13")
        ),
        AgentWideCase(
            task = "What was my longest bike ride this year?",
            kind = Kind.LONG_NOTE,
            tools = OUTLINE,
            ends = End.ANSWER,
            holds = listOf("Ronda", "112")
        ),
        AgentWideCase(
            task = "At what odometer reading did I get the bike chain replaced?",
            kind = Kind.LONG_NOTE,
            tools = OUTLINE,
            ends = End.ANSWER,
            holds = listOf("4820")
        ),
        AgentWideCase(
            task = "how many km did I ride in 2026 according to my bike log",
            kind = Kind.LONG_NOTE,
            tools = READ,
            ends = End.ANSWER,
            computed = listOf("800")
        ),
        AgentWideCase(
            task = "How long is the Algarve route in km?",
            kind = Kind.SEVERAL_NOTES,
            tools = READ,
            ends = End.ANSWER,
            computed = listOf("320")
        ),
        AgentWideCase(
            task = "What should I quote for a rush full-page illustration?",
            kind = Kind.SEVERAL_NOTES,
            tools = READ,
            ends = End.ANSWER,
            computed = listOf("494")
        ),
        AgentWideCase(
            task = "How much money do clients still owe me?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "answer"),
            ends = End.ANSWER,
            computed = listOf("2030"),
            lacks = listOf("2630")
        ),
        AgentWideCase(
            task = "Which client invoice is due soonest?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Brisa")
        ),
        AgentWideCase(
            task = "Who prints my calendars, and what's their minimum order?",
            kind = Kind.SEVERAL_NOTES,
            tools = READ,
            ends = End.ANSWER,
            holds = listOf("Imprenta Sur", "20")
        ),
        AgentWideCase(
            task = "How much did Pedro Vidal pay me for the labels?",
            kind = Kind.AMBIGUOUS,
            tools = READ,
            ends = End.ANSWER,
            holds = listOf("600")
        ),
        AgentWideCase(
            task = "Which dinners on this week's menu are vegetarian?",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "read", "answer"),
            ends = End.ANSWER,
            holds = listOf("Garbanzos con espinacas", "Tortilla", "Curry de garbanzos"),
            lacks = listOf("Hummus")
        ),
        AgentWideCase(
            task = "What does Lucía Ortega charge per hour?",
            kind = Kind.SEVERAL_NOTES,
            tools = READ,
            ends = End.ANSWER,
            lacks = listOf("10 €", "€10", "10 euros", "10/h")
        ),
        AgentWideCase(
            task = "What was the November electricity bill at the studio?",
            kind = Kind.SEVERAL_NOTES,
            tools = READ,
            ends = End.ANSWER,
            holds = listOf("arriv")
        ),
        AgentWideCase(
            task = "How much did the train back from Ronda cost me?",
            kind = Kind.LONG_NOTE,
            tools = READ,
            ends = End.ANSWER,
            lacks = listOf("38")
        )
    )
}
