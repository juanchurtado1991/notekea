package app.notekea.chat.eval

/**
 * Held-out tasks over the committed Sample Vault, never tuned against: they measure whether the
 * agent generalizes beyond [AgentEvalCases], whose builders check them the same way.
 */
internal object AgentHeldOutCases {
    private val SERVE_FOUR = setOf("Black beans", "Tomato soup")
    private val WITH_ATTENDEES = setOf("2026-10-21 Kickoff", "2026-10-23 Design review")
    private val RATED_FOUR = setOf("Atomic Habits", "Sapiens")

    val all: List<AgentEvalCase> = listOf(
        // Tags: the notes a property, a link, a task or the text picks out
        AgentEvalCases.tags(
            task = "tag the recipes that serve four people with family",
            tag = "family",
            names = SERVE_FOUR
        ),
        AgentEvalCases.tags(
            task = "Could you tag every meeting that lists its attendees with group?",
            tag = "group",
            names = WITH_ATTENDEES
        ),
        AgentEvalCases.tags(
            task = "tag the notes in my Projects folder that have a status property with tracked",
            tag = "tracked",
            names = setOf("Garden plan", "Kitchen renovation", "Notekea launch")
        ),
        AgentEvalCases.tags(
            task = "tag every note that links to Budget 2026 with finance",
            tag = "finance",
            names = setOf("Kitchen renovation", "2026-10-24 Budget")
        ),
        AgentEvalCases.tags(
            task = "The books still unchecked in my Reading list: tag them backlog.",
            tag = "backlog",
            names = setOf("Deep Work", "Dune", "Sapiens")
        ),
        AgentEvalCases.tags(
            task = "tag the daily journal notes that mention the garden with garden-days",
            tag = "garden-days",
            names = setOf("2026-10-20", "2026-10-25")
        ),
        AgentEvalCases.tags(
            task = "tag the recipes the weekly menu uses with on-menu",
            tag = "on-menu",
            names = setOf("Arepas", "Tomato soup", "Black beans", "Pancakes")
        ),
        AgentEvalCases.tags(
            task = "etiqueta con dos-personas la receta que rinde dos raciones",
            tag = "dos-personas",
            names = setOf("Pancakes")
        ),
        AgentEvalCases.tags(
            task = "¿Puedes etiquetar con viaje todas las notas que escriben la palabra Lisbon?",
            tag = "viaje",
            names = setOf("Trip to Lisbon", "Café con leche")
        ),
        AgentEvalCases.tags(
            task = "etiqueta con pendiente los proyectos que todavía tienen tareas abiertas",
            tag = "pendiente",
            names = setOf("Notekea launch", "Trip to Lisbon")
        ),
        // Properties: one note or a set, found before it is changed
        AgentEvalCases.property(
            task = "the garden project is active now; update its status",
            property = "status",
            note = "Garden plan"
        ),
        AgentEvalCases.property(
            task = "in my Projects folder, set priority 2 on every note with a status but no priority",
            property = "priority",
            names = setOf("Garden plan", "Kitchen renovation")
        ),
        AgentEvalCases.property(
            task = "add a servings property of 1 to the drink recipe",
            property = "servings",
            note = "Café con leche"
        ),
        AgentEvalCases.property(
            task = "One book is checked off in my Reading list but still marked as not " +
                "finished. Set finished to true on it.",
            property = "finished",
            note = "Atomic Habits"
        ),
        AgentEvalCases.property(
            task = "my 1-1 with Ana has no date: set date 2026-10-28 on it",
            property = "date",
            note = "1-1 Ana"
        ),
        AgentEvalCases.property(
            task = "set read to 18 on the reading challenge",
            property = "read",
            note = "Reading challenge"
        ),
        AgentEvalCases.property(
            task = "put a vendor property, Stone & Co, on the project the vendor call is about",
            property = "vendor",
            note = "Kitchen renovation"
        ),
        AgentEvalCases.property(
            task = "baja a 3 el rating de todos los libros que tienen 4",
            property = "rating",
            names = RATED_FOUR
        ),
        AgentEvalCases.property(
            task = "añade al plan del jardín una propiedad budget con lo que el presupuesto " +
                "2026 planea para el jardín",
            property = "budget",
            note = "Garden plan"
        ),
        AgentEvalCases.property(
            task = "pon status done en el proyecto que vence el 15 de noviembre",
            property = "status",
            note = "Notekea launch"
        ),
        // Writes: a new note drawn from what the vault holds
        AgentEvalCases.writes(
            task = "write a note Garden budget with what the garden was planned to cost " +
                "and what has been spent on it",
            title = "Garden budget"
        ),
        AgentEvalCases.writes(
            task = "write a note Kickoff follow-up with the decisions and tasks of the kickoff",
            title = "Kickoff follow-up"
        ),
        AgentEvalCases.writes(
            task = "Make a note Lisbon plan: the places the trip will visit and what is " +
                "left to book.",
            title = "Lisbon plan"
        ),
        AgentEvalCases.writes(
            task = "create a note Spending so far with each item spent in Budget 2026 and " +
                "the total",
            title = "Spending so far"
        ),
        AgentEvalCases.writes(
            task = "write a note Authors listing every book with its author",
            title = "Authors"
        ),
        AgentEvalCases.writes(
            task = "could you write a note Meeting attendees with who was at each meeting " +
                "that lists attendees?",
            title = "Meeting attendees"
        ),
        AgentEvalCases.writes(
            task = "escribe una nota Lista de compras con los ingredientes de las arepas",
            title = "Lista de compras"
        ),
        AgentEvalCases.writes(
            task = "crea una nota Ideas resumidas con una línea por cada nota de la carpeta " +
                "Ideas",
            title = "Ideas resumidas"
        ),
        AgentEvalCases.writes(
            task = "escribe una nota Diario de octubre que resuma lo que pasó cada día del " +
                "diario",
            title = "Diario de octubre"
        ),
        AgentEvalCases.writes(
            task = "haz una nota Recetas para cuatro con las recetas que rinden cuatro " +
                "raciones y cómo se preparan",
            title = "Recetas para cuatro"
        ),
        // Tasks: added to a note named only by what it holds
        AgentEvalCases.task(
            task = "add a task 'buy basil seeds' to the project planned for spring",
            note = "Garden plan"
        ),
        AgentEvalCases.task(
            task = "add 'call Stone & Co' as a task to the project that waits for the vendor",
            note = "Kitchen renovation"
        ),
        AgentEvalCases.task(
            task = "add a task 'send the sketches' to the meeting about the sidebar",
            note = "2026-10-23 Design review"
        ),
        AgentEvalCases.task(
            task = "add a task 'buy corn flour' to the recipe that serves six",
            note = "Arepas"
        ),
        AgentEvalCases.task(
            task = "My reading challenge says where its progress lives. Add a task " +
                "'pick two new books' to that note.",
            note = "Reading list"
        ),
        AgentEvalCases.task(
            task = "añade la tarea 'llamar a Ana' a la reunión del presupuesto",
            note = "2026-10-24 Budget"
        ),
        AgentEvalCases.task(
            task = "agrega una tarea 'comprar leche' a la receta de bebida",
            note = "Café con leche"
        ),
        AgentEvalCases.task(
            task = "añade una tarea 'grabar la voz' al proyecto con prioridad 1",
            note = "Notekea launch"
        ),
        // Links: from the note the task names to one it describes
        AgentEvalCases.links(
            task = "in the garden plan, add a link to the recipe that uses tomatoes from " +
                "the beds",
            note = "Garden plan"
        ),
        AgentEvalCases.links(
            task = "add to the 1-1 with Ana a link to the book about focused work",
            note = "1-1 Ana"
        ),
        AgentEvalCases.links(
            task = "link Retro October to the book about habits",
            note = "Retro October"
        ),
        AgentEvalCases.links(
            task = "add to the Lisbon trip a link to the recipe that mentions Lisbon",
            note = "Trip to Lisbon"
        ),
        AgentEvalCases.links(
            task = "en la nota de la llamada con el proveedor, añade un enlace al proyecto " +
                "de la cocina",
            note = "Vendor call"
        ),
        AgentEvalCases.links(
            task = "en la nota Local AI, enlaza la otra idea de la carpeta Ideas",
            note = "Local AI"
        ),
        // Moves: the notes a look-up picks out, into a new folder
        AgentEvalCases.moves(
            task = "move the books I rated 4 into a folder Books/Later",
            names = RATED_FOUR
        ),
        AgentEvalCases.moves(
            task = "move every meeting that lists its attendees into Meetings/Team",
            names = WITH_ATTENDEES
        ),
        AgentEvalCases.moves(
            task = "move the breakfast recipe into a new folder Recipes/Breakfast",
            note = "Pancakes"
        ),
        AgentEvalCases.moves(
            task = "Archive my ideas: move the notes tagged idea into Archive/Ideas.",
            names = setOf("Local AI", "Related notes")
        ),
        AgentEvalCases.moves(
            task = "mueve el proyecto del viaje a una carpeta nueva Viajes",
            note = "Trip to Lisbon"
        ),
        AgentEvalCases.moves(
            task = "mueve los proyectos con la etiqueta money a la carpeta Finanzas",
            note = "Budget 2026"
        ),
        // Says: answers read from properties, tables and text, some worked out
        AgentEvalCases.says(
            // worked out: spent 0 + 120 + 450 + 190 = 760
            task = "How much has been spent so far across all of Budget 2026?",
            words = listOf("760")
        ),
        AgentEvalCases.says(
            // worked out: books planned 250 - spent 190 = 60
            task = "how much of the books budget is still unspent?",
            words = listOf("60")
        ),
        AgentEvalCases.says(
            task = "who wrote the book about desert politics?",
            words = listOf("Herbert")
        ),
        AgentEvalCases.says(
            task = "what goes in bed 2 of the garden plan?",
            words = listOf("peppers", "basil")
        ),
        AgentEvalCases.says(
            task = "Who set up the release builds?",
            words = listOf("Luis")
        ),
        AgentEvalCases.says(
            task = "which week number is the weekly menu for?",
            words = listOf("44")
        ),
        AgentEvalCases.says(
            task = "what counters did the vendor call settle on, and with which vendor?",
            words = listOf("quartz", "Stone")
        ),
        AgentEvalCases.says(
            task = "which places does the Lisbon trip plan to visit besides Belém?",
            words = listOf("Alfama", "Sintra")
        ),
        AgentEvalCases.says(
            // worked out: one open "Review the day" in each of the 6 daily notes
            task = "How many open tasks do my daily journal notes have in total?",
            words = listOf("6")
        ),
        AgentEvalCases.says(
            task = "which book by Yuval Noah Harari is in my notes, and who wrote Dune?",
            words = listOf("Sapiens", "Herbert")
        ),
        AgentEvalCases.says(
            // worked out: goal 24 - read 17 = 7
            task = "how many more books do I need to read to reach my reading challenge goal?",
            words = listOf("7")
        ),
        AgentEvalCases.says(
            // worked out: 4 + 5 + 5 + 4 + 5 = 23
            task = "What is the total of the ratings of my five books?",
            words = listOf("23")
        ),
        AgentEvalCases.says(
            task = "which kinds of tomatoes does the garden plan grow?",
            words = listOf("San Marzano", "cherry")
        ),
        AgentEvalCases.says(
            // worked out: servings 6 (Arepas) + 4 (Black beans) + 2 (Pancakes)
            // + 4 (Tomato soup) = 16
            task = "¿cuántas raciones suman todas las recetas que indican sus raciones?",
            words = listOf("16")
        ),
        AgentEvalCases.says(
            task = "¿Quién escribió el libro sobre hábitos?",
            words = listOf("James Clear")
        ),
        AgentEvalCases.says(
            task = "¿cuánto llevo gastado en viajes según el presupuesto 2026?",
            words = listOf("450")
        ),
        AgentEvalCases.says(
            // worked out: garden planned 300 - spent 120 = 180
            task = "¿cuánto le queda por gastar al jardín en el presupuesto 2026?",
            words = listOf("180")
        ),
        AgentEvalCases.says(
            task = "¿qué valor tiene la propiedad count en la guía de propiedades?",
            words = listOf("42")
        ),
        AgentEvalCases.says(
            task = "¿Qué toca de almuerzo el lunes según el menú semanal?",
            words = listOf("Arepas")
        ),
        AgentEvalCases.says(
            task = "¿qué libros están marcados como hechos en la lista de lectura?",
            words = listOf("Pragmatic", "Atomic Habits")
        )
    )
}
