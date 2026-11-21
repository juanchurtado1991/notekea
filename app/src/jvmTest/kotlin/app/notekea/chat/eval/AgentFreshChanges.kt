package app.notekea.chat.eval

import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** The changes of [AgentFreshCases]: to one note, or to the notes a condition picks out. */
internal object AgentFreshChanges {
    private val ONE = listOf("search", "read", "propose")
    private val GROUP = listOf("search", "read", "read", "read", "propose")

    private val RECIPES = listOf(
        "Garbanzos con espinacas",
        "Hummus de remolacha",
        "Curry de garbanzos",
        "Lentejas estofadas",
        "Tortilla de patatas"
    )

    val all = listOf(
        AgentWideCase(
            task = "Tag the clients who haven't paid me yet with #chase",
            kind = Kind.SEVERAL_NOTES,
            tools = GROUP,
            ends = End.PROPOSE,
            holds = listOf("Editorial Marea", "Café Brisa", "Ayuntamiento de Rincón"),
            lacks = listOf("Clients/Pedro Vidal", "Clients/Studio Norte")
        ),
        AgentWideCase(
            task = "Etiqueta con #garbanzos todas las recetas que llevan garbanzos",
            kind = Kind.SEVERAL_NOTES,
            tools = GROUP,
            ends = End.PROPOSE,
            holds = listOf("Garbanzos con espinacas", "Hummus de remolacha", "Curry de garbanzos"),
            lacks = listOf("Kitchen/Lentejas estofadas", "Kitchen/Tortilla de patatas")
        ),
        AgentWideCase(
            task = "Tag the recipes that take 35 minutes or less with #rapida",
            kind = Kind.SEVERAL_NOTES,
            tools = GROUP,
            ends = End.PROPOSE,
            holds = listOf("Garbanzos con espinacas", "Curry de garbanzos"),
            lacks = listOf("Kitchen/Lentejas estofadas", "Kitchen/Tortilla de patatas")
        ),
        AgentWideCase(
            task = "Café Brisa just paid, set their invoice to paid",
            kind = Kind.SEVERAL_NOTES,
            tools = ONE,
            ends = End.PROPOSE,
            holds = listOf("Café Brisa", "paid")
        ),
        AgentWideCase(
            task = "Add a task to Editorial Marea's note: chase the invoice, due 2026-12-02",
            kind = Kind.SEVERAL_NOTES,
            tools = ONE,
            ends = End.PROPOSE,
            holds = listOf("Editorial Marea", "2026-12-02")
        ),
        AgentWideCase(
            task = "put shin pads for Noa on the kit order",
            kind = Kind.SEVERAL_NOTES,
            tools = ONE,
            ends = End.PROPOSE,
            holds = listOf("Kit order", "shin pads")
        ),
        AgentWideCase(
            task = "I booked the small pitch for the 26th, tick that off",
            kind = Kind.SEVERAL_NOTES,
            tools = ONE,
            ends = End.PROPOSE,
            holds = listOf("small pitch")
        ),
        AgentWideCase(
            task = "Move all my recipes into a folder called Recetas",
            kind = Kind.SEVERAL_NOTES,
            tools = GROUP,
            ends = End.PROPOSE,
            holds = RECIPES + "Recetas",
            lacks = listOf("Kitchen/Menu semanal")
        ),
        AgentWideCase(
            task = "Make a note listing the girls who still owe the medical form",
            kind = Kind.SEVERAL_NOTES,
            tools = ONE,
            ends = End.PROPOSE,
            holds = listOf("Noa Fernández", "Sara Molina", "Daniela Prieto"),
            lacks = listOf("Carla Méndez", "Alba Torres")
        ),
        AgentWideCase(
            task = "Add to the route plan that on the rest day in Lagos I want to see " +
                "Ponta da Piedade",
            kind = Kind.SEVERAL_NOTES,
            tools = ONE,
            ends = End.PROPOSE,
            holds = listOf("Route plan", "Ponta da Piedade")
        ),
        AgentWideCase(
            task = "Ponle tiempo: 15 al hummus",
            kind = Kind.SEVERAL_NOTES,
            tools = ONE,
            ends = End.PROPOSE,
            holds = listOf("Hummus de remolacha", "tiempo")
        ),
        AgentWideCase(
            task = "Add bike lights, 30 €, to the trip budget",
            kind = Kind.AMBIGUOUS,
            tools = ONE,
            ends = End.PROPOSE,
            holds = listOf("lights", "30"),
            lacks = listOf("Shop/")
        ),
        AgentWideCase(
            task = "Add 'gouache on kraft paper' to my illustration ideas",
            kind = Kind.AMBIGUOUS,
            tools = ONE,
            ends = End.PROPOSE,
            holds = listOf("gouache"),
            lacks = listOf("Garden/")
        ),
        AgentWideCase(
            task = "Write me a note with what each client still owes me",
            kind = Kind.SEVERAL_NOTES,
            tools = GROUP,
            ends = End.PROPOSE,
            holds = listOf("950", "380", "700")
        ),
        AgentWideCase(
            task = "Ya compré el ajo, márcalo en las semillas",
            kind = Kind.SEVERAL_NOTES,
            tools = ONE,
            ends = End.PROPOSE,
            holds = listOf("Ajo morado")
        ),
        AgentWideCase(
            task = "Tag every note in the Care folder with #papa",
            kind = Kind.SEVERAL_NOTES,
            tools = GROUP,
            ends = End.PROPOSE,
            holds = listOf("Care plan Ramón", "Medication", "Appointments", "Carer hours October"),
            lacks = listOf("People/Ramón Alvarado", "People/Lucía Benítez")
        ),
        AgentWideCase(
            task = "Mark order #1044 as shipped",
            kind = Kind.SEVERAL_NOTES,
            tools = ONE,
            ends = End.PROPOSE,
            holds = listOf("1044", "shipped")
        ),
        AgentWideCase(
            task = "Add level: A2 to the CIPLE note",
            kind = Kind.SEVERAL_NOTES,
            tools = ONE,
            ends = End.PROPOSE,
            holds = listOf("level", "A2")
        ),
        AgentWideCase(
            task = "Add a task to book a taxi for dad's neurology appointment, due 2026-11-18",
            kind = Kind.SEVERAL_NOTES,
            tools = ONE,
            ends = End.PROPOSE,
            holds = listOf("taxi", "2026-11-18")
        ),
        AgentWideCase(
            task = "Create a note 'October carer invoice' with the carer's hours per week " +
                "and the total I have to pay her",
            kind = Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "read", "propose"),
            ends = End.PROPOSE,
            holds = listOf("October carer invoice"),
            computed = listOf("87 h", "870")
        ),
        AgentWideCase(
            task = "Haz una lista de la compra con los ingredientes de las recetas del menú " +
                "de esta semana",
            kind = Kind.SEVERAL_NOTES,
            tools = GROUP,
            ends = End.PROPOSE,
            holds = listOf("vinagre", "chorizo", "huevos", "leche de coco"),
            lacks = listOf("tahini")
        ),
        AgentWideCase(
            task = "Link the Christmas tournament from my Home note",
            kind = Kind.SEVERAL_NOTES,
            tools = ONE,
            ends = End.PROPOSE,
            holds = listOf("Home", "Torneo de Navidad")
        ),
        AgentWideCase(
            task = "Apunta en la nota de Marco que la próxima clase es el 17 de noviembre",
            kind = Kind.SEVERAL_NOTES,
            tools = ONE,
            ends = End.PROPOSE,
            holds = listOf("Marco Silva", "17")
        ),
        AgentWideCase(
            task = "Add season: 2026-27 to the notes in the Football folder that don't have it yet",
            kind = Kind.SEVERAL_NOTES,
            tools = GROUP,
            ends = End.PROPOSE,
            holds = listOf("Torneo de Navidad", "Kit order", "Club code of conduct"),
            lacks = listOf("Football/Roster", "Football/Match log", "Football/Training plan")
        ),
        AgentWideCase(
            task = "Move Studio Norte to an Archive folder, that project is dead",
            kind = Kind.SEVERAL_NOTES,
            tools = ONE,
            ends = End.PROPOSE,
            holds = listOf("Studio Norte", "Archive")
        ),
        AgentWideCase(
            task = "done with the gestor call, tick it off in my journal",
            kind = Kind.SEVERAL_NOTES,
            tools = ONE,
            ends = End.PROPOSE,
            holds = listOf("gestor")
        ),
        AgentWideCase(
            task = "Marco raised his rate to 30 € a lesson, update his note",
            kind = Kind.SEVERAL_NOTES,
            tools = ONE,
            ends = End.PROPOSE,
            holds = listOf("Marco Silva", "30")
        ),
        AgentWideCase(
            task = "Crea una nota «Resumen de la temporada» con los resultados de la liga",
            kind = Kind.LONG_NOTE,
            tools = ONE,
            ends = End.PROPOSE,
            holds = listOf("Rincón", "Teatinos", "Puerto de la Torre", "Los Olivos", "Churriana")
        )
    )
}
