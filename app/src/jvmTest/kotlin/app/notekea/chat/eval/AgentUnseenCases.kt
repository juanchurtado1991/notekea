package app.notekea.chat.eval

import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/**
 * Tasks the loop's checks were not written against (`docs/agent-loop.md`, items 3 and 4), over the `Agent Eval`
 * vault: half can mean several notes and should ask back, half look like them and should not. Written before the
 * checks were run on them, and not to be tuned on.
 */
internal object AgentUnseenCases {
    private val READ = listOf("search", "read", "answer")
    private val CHANGE = listOf("search", "read", "propose")
    private val ASK_NOTE = listOf("list", "ask_user")
    private val ASK_NAME = listOf("search", "ask_user")

    private fun asks(task: String, vararg holds: String) =
        AgentWideCase(task, Kind.AMBIGUOUS, ASK_NOTE, End.ASK, holds.toList())

    private fun asksWho(task: String, vararg holds: String) =
        AgentWideCase(task, Kind.AMBIGUOUS, ASK_NAME, End.ASK, holds.toList())

    private fun answers(task: String, vararg holds: String) =
        AgentWideCase(task, Kind.SEVERAL_NOTES, READ, End.ANSWER, holds.toList())

    private fun changes(task: String, vararg holds: String) =
        AgentWideCase(task, Kind.SEVERAL_NOTES, CHANGE, End.PROPOSE, holds.toList())

    // Two people of one first name, two notes of one name, said in words the tuned tasks do not use.
    private val ambiguous = listOf(
        asksWho("what is Laura's phone number?", "Castro", "Méndez"),
        asksWho("¿cuándo es el cumpleaños de Laura?", "Castro", "Méndez"),
        asksWho("add a task to send Tomás the photos", "Herrera", "Vargas"),
        asks("what is the total of the budget?", "Home", "Work"),
        asks("¿cuánto suma el presupuesto 2026?", "Home", "Work"),
        asks("rename the budget to Budget 2026 draft", "Home", "Work"),
        asks("add 'learn to play the guitar' to my goals", "Personal", "Work"),
        asks("add a line 'call the bank' to my ideas", "Personal", "Work"),
        asks("añade 'revisar el correo' a la revisión semanal", "Personal", "Work"),
        asks("move the weekly review to Archive", "Personal", "Work"),
        asks("what did we agree at the supplier meeting?", "03", "10"),
        asks("add Ana Rojas, 8811-2233, to the Contactos note", "Club", "Personal")
    )

    // The same names and notes, told apart by a word, by what the notes hold, or asked of all of them.
    private val clear = listOf(
        answers("what is Laura Castro's email?", "lcastro@cafetalnorte.example"),
        answers("what is the phone number of Tomás from the chess club?", "8712-6650"),
        answers("what did Laura Méndez lend me?", "Overstory"),
        answers("what is Rosa's phone?", "8855-2310"),
        answers("how much is the work budget for training?", "3000"),
        answers("what's the total of the household budget?", "9600"),
        answers("list the ideas in both of my Ideas notes", "herb", "newsletter"),
        answers("what is the status of the greenhouse project?", "active"),
        answers("¿cuánto se acordó pagar al mes en la reunión con el proveedor del 10 de noviembre?", "4250"),
        changes("add 'run 10 km' to my personal goals for 2027", "Personal", "10 km"),
        changes("add 'water the plants twice' to my Sunday weekly review", "Personal"),
        changes("move the irrigation project to Archive", "Irrigation")
    )

    val all: List<AgentWideCase> = ambiguous + clear
}
