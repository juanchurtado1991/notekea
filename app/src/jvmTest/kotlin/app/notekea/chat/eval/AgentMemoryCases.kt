package app.notekea.chat.eval

import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/**
 * The chat's memory measured (`docs/agent-memory.md`, "How it is measured") over `Agent Eval` with a memory written
 * in: ambiguous tasks it settles (they act, not ask), clear tasks it must not change (the request wins), a wrong
 * memory (followed, the step showing it), and its other uses.
 */
internal object AgentMemoryCases {
    private val READ = listOf("search", "read", "answer")
    private val CHANGE = listOf("search", "read", "propose")

    private fun answers(
        task: String,
        vararg holds: String,
        lacks: List<String> = emptyList()
    ) = AgentWideCase(task, Kind.AMBIGUOUS, READ, End.ANSWER, holds.toList(), lacks = lacks)

    private fun changes(
        task: String,
        vararg holds: String,
        lacks: List<String> = emptyList()
    ) = AgentWideCase(task, Kind.AMBIGUOUS, CHANGE, End.PROPOSE, holds.toList(), lacks = lacks)

    /** A memory that says which note each ambiguous task of [AgentUnseenCases] means, among lines of other kinds. */
    val SETTLING = """
        ## People and names
        - "Laura" is [[Laura Castro]]
        - "Tomás" is [[Tomás Herrera]]
        - "the budget" is [[Work/Budget 2026]]
        - "el presupuesto" is [[Work/Budget 2026]]
        - "my goals" are [[Work/Goals 2027]]
        - "my ideas" are [[Personal/Ideas]]
        - "the weekly review" and "la revisión semanal" are [[Work/Weekly review]]
        - "the supplier meeting" is [[Meetings/2026-11-10 Supplier]]
        - "Contactos" is [[Club/Contactos]]
        - "my sister" is [[Rosa Quesada]]

        ## Where things go
        - Meeting notes go in `Meetings/`

        ## Areas
        - "personal" is the folder `Personal/`

        ## How I like answers
        - Give the figures the answer is worked out from
    """.trimIndent()

    /** The same names pointed at the other note: the turn follows it, as a memory is meant to be followed. */
    val WRONG = """
        ## People and names
        - "Laura" is [[Laura Méndez]]
        - "Tomás" is [[Tomás Vargas]]
        - "the budget" is [[Home/Budget 2026]]
    """.trimIndent()

    // Kind 1: settled by the memory, so they act on the note it names instead of asking.
    val settled = listOf(
        answers("what is Laura's phone number?", "8701-9032", lacks = listOf("8823-4471")),
        answers("¿cuándo es el cumpleaños de Laura?", "30", lacks = listOf("12 March", "12 de marzo")),
        changes("add a task to send Tomás the photos", "Tomás Herrera", lacks = listOf("Vargas")),
        answers("what is the total of the budget?", "68000", lacks = listOf("9600")),
        answers("¿cuánto suma el presupuesto 2026?", "68000", lacks = listOf("9600")),
        changes("rename the budget to Budget 2026 draft", "Work"),
        changes("add 'learn to play the guitar' to my goals", "Work"),
        changes("add a line 'call the bank' to my ideas", "Personal"),
        changes("añade 'revisar el correo' a la revisión semanal", "Work"),
        changes("move the weekly review to Archive", "Work"),
        answers("what did we agree at the supplier meeting?", "4250"),
        changes("add Ana Rojas, 8811-2233, to the Contactos note", "Club")
    )

    // Kind 3: a wrong line is followed; what makes it safe is the step that shows it (checked in the report).
    val followed = listOf(
        answers("what is Laura's phone number?", "8823-4471"),
        answers("what is Tomás's email?", "tvargas@correo.example"),
        answers("what is the total of the budget?", "9600")
    )

    // Kind 5: the memory's other uses: a person said by who they are, an area, where a kind of note goes.
    val uses = listOf(
        answers("what is my sister's phone?", "8855-2310"),
        answers("what is in my personal notes about saving for a car?", "3000"),
        changes("write a note with what we agreed at the 10 November supplier meeting", "Meetings/")
    )
}
