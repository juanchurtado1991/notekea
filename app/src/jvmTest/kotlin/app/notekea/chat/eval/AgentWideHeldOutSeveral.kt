package app.notekea.chat.eval

/** Held-out wider tasks that can be read two ways, or need several notes ([AgentWideHeldOutCases]). */
internal object AgentWideHeldOutSeveral {
    private val READ = listOf("outline", "read", "answer")
    private val LOOK_UP = listOf("search", "read", "answer")
    private val SUM = listOf("search", "read", "calculate", "answer")
    private val ASK_NOTE = listOf("list", "ask_user")
    private val ASK_NAME = listOf("search", "ask_user")

    // Two notes, people or things that answer to the same words: the question offers them.
    val ambiguous = listOf(
        AgentWideCase(
            task = "add a task to call Laura on her birthday",
            kind = AgentWideCase.Kind.AMBIGUOUS,
            tools = ASK_NAME,
            ends = AgentWideCase.End.ASK,
            holds = listOf("Méndez", "Castro")
        ),
        AgentWideCase(
            task = "¿cuál es el correo de Tomás?",
            kind = AgentWideCase.Kind.AMBIGUOUS,
            tools = ASK_NAME,
            ends = AgentWideCase.End.ASK,
            holds = listOf("Herrera", "Vargas")
        ),
        AgentWideCase(
            task = "marca como hecha la primera tarea de mis metas de 2027",
            kind = AgentWideCase.Kind.AMBIGUOUS,
            tools = ASK_NOTE,
            ends = AgentWideCase.End.ASK,
            holds = listOf("Personal", "Work")
        ),
        AgentWideCase(
            task = "add 'get a first-aid certificate' to my 2027 goals",
            kind = AgentWideCase.Kind.AMBIGUOUS,
            tools = ASK_NOTE,
            ends = AgentWideCase.End.ASK,
            holds = listOf("Personal", "Work")
        ),
        AgentWideCase(
            task = "put 'a shared calendar' in my ideas note",
            kind = AgentWideCase.Kind.AMBIGUOUS,
            tools = ASK_NOTE,
            ends = AgentWideCase.End.ASK,
            holds = listOf("Personal", "Work")
        ),
        AgentWideCase(
            task = "add 'check next week's calendar' to the weekly review",
            kind = AgentWideCase.Kind.AMBIGUOUS,
            tools = ASK_NOTE,
            ends = AgentWideCase.End.ASK,
            holds = listOf("Personal", "Work")
        ),
        AgentWideCase(
            task = "añade 'ordenar el escritorio' a mi revisión semanal",
            kind = AgentWideCase.Kind.AMBIGUOUS,
            tools = ASK_NOTE,
            ends = AgentWideCase.End.ASK,
            holds = listOf("Personal", "Work")
        ),
        AgentWideCase(
            task = "añade a Mario Solís, 8890-1122, a Contactos",
            kind = AgentWideCase.Kind.AMBIGUOUS,
            tools = ASK_NOTE,
            ends = AgentWideCase.End.ASK,
            holds = listOf("Club", "Personal")
        ),
        AgentWideCase(
            task = "when does the contract end?",
            kind = AgentWideCase.Kind.AMBIGUOUS,
            tools = ASK_NOTE,
            ends = AgentWideCase.End.ASK,
            holds = listOf("Service agreement", "arrendamiento")
        ),
        AgentWideCase(
            task = "how many days is the trip?",
            kind = AgentWideCase.Kind.AMBIGUOUS,
            tools = ASK_NOTE,
            ends = AgentWideCase.End.ASK,
            holds = listOf("Japan", "Lisbon")
        ),
        AgentWideCase(
            task = "add a task to pay for the flat",
            kind = AgentWideCase.Kind.AMBIGUOUS,
            tools = ASK_NAME,
            ends = AgentWideCase.End.ASK,
            holds = listOf("Lisbon")
        ),
        AgentWideCase(
            task = "crea una nota para preparar el torneo",
            kind = AgentWideCase.Kind.AMBIGUOUS,
            tools = ASK_NAME,
            ends = AgentWideCase.End.ASK,
            holds = listOf("Navidad", "Abierto")
        )
    )

    // The flat: the lease with the moving note and the landlady's note.
    val flat = listOf(
        AgentWideCase(
            task = "once Canela moves in, how much will Rosa hold in deposits, per the lease and " +
                "my moving note?",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = SUM,
            ends = AgentWideCase.End.ANSWER,
            // worked out: the 1300 USD deposit + 150 USD for a pet
            computed = listOf("1,450")
        ),
        AgentWideCase(
            task = "can Canela live in my new flat under the lease as it is written?",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = LOOK_UP,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("10", "12")
        ),
        AgentWideCase(
            task = "once I move, what will the flat cost me a month: the rent plus the " +
                "electricity and internet my moving note estimates?",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = SUM,
            ends = AgentWideCase.End.ANSWER,
            // worked out: 650 rent + 35 electricity + 30 internet USD; water is in the rent
            computed = listOf("715")
        ),
        AgentWideCase(
            task = "¿coincide el día en que anoté recoger las llaves con lo que dice el contrato " +
                "de arrendamiento?",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = LOOK_UP,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("30", "31")
        ),
        AgentWideCase(
            task = "agrega a la nota de Rosa Quesada el monto de la renta y hasta qué día del " +
                "mes se paga",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "append", "propose"),
            ends = AgentWideCase.End.PROPOSE,
            holds = listOf("Rosa Quesada", "650")
        )
    )

    // The water audit with the assembly that discussed it.
    val water = listOf(
        AgentWideCase(
            task = "which of the water audit's recommendations did the ASADA assembly approve, " +
                "and which one did it not discuss?",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = LOOK_UP,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("R1", "R2", "R3", "R6", "R4")
        ),
        AgentWideCase(
            task = "add a task to my ASADA assembly note for each audit recommendation the " +
                "assembly postponed or did not discuss",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "add_task", "propose"),
            ends = AgentWideCase.End.PROPOSE,
            holds = listOf("Pozo 2", "Los Ángeles")
        )
    )

    // People with the notes they appear in.
    val people = listOf(
        AgentWideCase(
            task = "what walking level is Laura Méndez at, per the shelter's handbook?",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = LOOK_UP,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("green")
        ),
        AgentWideCase(
            task = "how many pages of the book Laura Méndez lent me do I still have to read, and " +
                "by when does she want it back?",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = SUM,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("Lisbon"),
            // worked out: The Overstory, 502 pages, at page 310
            computed = listOf("192")
        ),
        AgentWideCase(
            task = "how much does Tomás still owe me from the Japan trip?",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = SUM,
            ends = AgentWideCase.End.ANSWER,
            // worked out: half the ryokan, 45,000 yen, less the 30,000 he paid back
            computed = listOf("15,000")
        ),
        AgentWideCase(
            task = "make a note Lisbon contacts with the phone of everyone coming to Lisbon",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = listOf("read", "links", "write_note", "propose"),
            ends = AgentWideCase.End.PROPOSE,
            holds = listOf("Lisbon contacts")
        ),
        AgentWideCase(
            task = "which people in my notes have their birthday in August?",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = listOf("find", "answer"),
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("Laura Castro", "Tomás Vargas")
        ),
        AgentWideCase(
            task = "¿cuánto le debo todavía al tesorero del club de ajedrez por la cuota de 2026?",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = SUM,
            ends = AgentWideCase.End.ANSWER,
            // worked out: the 45 USD fee of 2026 less the 25 USD paid to Tomás Vargas
            computed = listOf("20")
        ),
        AgentWideCase(
            task = "¿cuál es el teléfono de quien organiza el próximo club de lectura?",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = LOOK_UP,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("8823-4471")
        ),
        AgentWideCase(
            task = "Laura Castro y yo estudiamos para el final de estadística: ¿cuándo nos " +
                "vemos, y cuándo y dónde es el examen?",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = LOOK_UP,
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("18:00", "B-204")
        ),
        AgentWideCase(
            task = "write a note 2027 deadlines with every dated task of my two goals notes",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = listOf("tasks", "write_note", "propose"),
            ends = AgentWideCase.End.PROPOSE,
            holds = listOf("2027 deadlines")
        )
    )

    // Loose but clear, or all of them: one target or every one, so the turn must not ask.
    val clear = listOf(
        AgentWideCase(
            task = "link Tomás from my Japan journal",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "link", "propose"),
            ends = AgentWideCase.End.PROPOSE,
            holds = listOf("Tomás Herrera"),
            lacks = listOf("Vargas")
        ),
        AgentWideCase(
            task = "añade al viaje una tarea para cambiar euros una semana antes de salir",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = listOf("list", "read", "add_task", "propose"),
            ends = AgentWideCase.End.PROPOSE,
            holds = listOf("Lisbon"),
            lacks = listOf("Japan")
        ),
        AgentWideCase(
            task = "cambia el teléfono de Laura, la de estadística, a 8701-4455",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "set_property", "propose"),
            ends = AgentWideCase.End.PROPOSE,
            holds = listOf("Laura Castro"),
            lacks = listOf("Méndez")
        ),
        AgentWideCase(
            task = "list my open tasks due in January 2027",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = listOf("tasks", "answer"),
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("moving truck", "passport", "internet", "electricity", "clerks", "keys")
        ),
        AgentWideCase(
            task = "tag both of my Goals 2027 notes with #2027",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = listOf("list", "add_tag", "propose"),
            ends = AgentWideCase.End.PROPOSE,
            holds = listOf("Personal", "Work")
        ),
        AgentWideCase(
            task = "add 'buy a bike lock' to my personal ideas",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = listOf("list", "append", "propose"),
            ends = AgentWideCase.End.PROPOSE,
            holds = listOf("Ideas"),
            lacks = listOf("Work/Ideas")
        ),
        AgentWideCase(
            task = "which of the two Lauras has her birthday earlier in the year?",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "answer"),
            ends = AgentWideCase.End.ANSWER,
            holds = listOf("Méndez")
        ),
        AgentWideCase(
            task = "add the book chosen at the book club to my reading list",
            kind = AgentWideCase.Kind.SEVERAL_NOTES,
            tools = listOf("search", "read", "edit_note", "propose"),
            ends = AgentWideCase.End.PROPOSE,
            holds = listOf("The Remains of the Day")
        )
    )
}
