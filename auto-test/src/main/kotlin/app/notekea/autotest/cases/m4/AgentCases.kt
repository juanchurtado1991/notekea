package app.notekea.autotest.cases.m4

import androidx.compose.ui.test.hasText
import app.notekea.autotest.ChatModels
import app.notekea.autotest.cases.m1.PLAYGROUND
import app.notekea.autotest.cases.m3.MODEL_CASE_TIMEOUT
import app.notekea.autotest.cases.m3.ask
import app.notekea.autotest.cases.m3.chooseMode
import app.notekea.autotest.cases.m3.copiedChat
import app.notekea.autotest.cases.m3.expectInChat
import app.notekea.autotest.cases.m3.openChat
import app.notekea.autotest.cases.m3.replyTo
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val SAMPLE_VAULT = "Sample Vault"
private const val NOTES_ABOUT = "me podes contar de que tratan las notas?"
private const val TASK = "find my recipe made with corn flour and tag it with latin"
private const val AREPAS = "$PLAYGROUND/Recipes/Arepas.md"
private const val STEP_MILLIS = 60_000L
private val STEPS = listOf("Read Recipes/Arepas", "Searched the notes", "Listed notes")

/**
 * The case of 4.7, agent mode (`MVP_SMOKE_TEST.md`), with Medium, the smallest size that offers it: a task that
 * needs a look-up lists its steps, ends on the card, and changes the note only once Do it is clicked.
 */
fun SuiteBuilder.agentCases() {
    agentWorks()
    overviewInAgentMode()
    notOnSmall()
    autoPicks()
}

private fun SuiteBuilder.agentWorks() {
    automated(
        id = "4.7-01",
        title = "Agent mode lists its steps, ends on the card, and changes nothing before Do it",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.medium
    ) {
        openChat()
        chooseMode(mode = "Agent")
        ask(question = TASK)

        // Its steps vary (a search, notes listed, the recipe read): one of them is listed before the card.
        ui
            .waitUntil(timeoutMillis = STEP_MILLIS) {
                STEPS.any { step ->
                    ui
                        .onAllNodes(
                            matcher = hasText(
                                text = step,
                                substring = true
                            )
                        )
                        .fetchSemanticsNodes()
                        .isNotEmpty()
                }
            }

        expectText(text = "Do it")

        expectFileStays(
            path = AREPAS,
            content = file(path = AREPAS).toFile().readText()
        )

        click(text = "Do it")

        expectFile(
            path = AREPAS,
            containing = "latin"
        )
    }
}

// The developer's chat of 2026-11-11: in Agent mode "what are my notes about?" was searched for the word "notes".
private fun SuiteBuilder.overviewInAgentMode() {
    automated(
        id = "4.7-03",
        title = "In Agent mode, what the notes are about is told from the vault's folders, not searched",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.medium
    ) {
        openChat(withArepas = false)
        chooseMode(mode = "Agent")
        ask(question = NOTES_ABOUT)

        val reply = replyTo(
            chat = copiedChat(),
            question = NOTES_ABOUT
        )

        expectInChat(reply, "Answered with: fast")
        expectInChat(reply, "Recipes", "Recetas", "recetas")
    }
}

private fun SuiteBuilder.notOnSmall() {
    manual(
        id = "4.7-02",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)

        title(
            text =
            "Con el modelo Pequeño no se ofrece el modo Agente y el chat funciona como antes"
        )

        steps(
            text =
            "1. En Ajustes → Modelo de IA elige Pequeño (descárgalo si hace falta).\n2. Abre " +
                "la Playground Vault y el chat (el globo de diálogo en la barra estrecha de la " +
                "izquierda).\n3. Pregunta: what do my recipes need?"
        )

        expected(
            text =
            "Bajo el campo de pregunta dice Pequeño · Auto, y su menú ofrece solo Auto y " +
                "Rápido. El paso 3 se responde con las notas, con sus fuentes, como antes."
        )
    }
}

private fun SuiteBuilder.autoPicks() {
    manual(
        id = "4.7-04",
        reason = ManualReason.COSTLY
    ) {
        prepare(SAMPLE_VAULT)

        title(
            text =
            "Auto piensa a fondo una comparación y le pasa al agente una tarea cuya nota " +
                "debe encontrar"
        )

        steps(
            text =
            "1. Con el modelo Mediano, abre la Sample Vault y el chat; deja el modo en " +
                "Mediano · Auto.\n2. Pregunta: compare Deep Work and Atomic Habits\n3. Pregunta: " +
                "add a task 'buy beans' to the recipe that uses black beans\n4. Abre el menú ⋯ " +
                "del chat y Copiar el chat; pégalo en cualquier lugar."
        )

        expected(
            text =
            "El paso 2 dice que está escribiendo borradores y comparándolos; el paso 3 " +
                "muestra los pasos del agente y termina en una tarjeta que agrega la tarea a " +
                "Black beans. El chat copiado dice Answered with: deep para el paso 2 y Answered " +
                "with: agent para el paso 3."
        )
    }
}
