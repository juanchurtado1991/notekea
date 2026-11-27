package app.notekea.autotest.cases.m4

import app.notekea.autotest.cases.m1.SAMPLE
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

/**
 * The one loop (`docs/agent-loop.md`, step 5), which works every request of the whole vault. By hand: it needs the
 * Medium model.
 */

internal fun SuiteBuilder.loopCases() {
    manual(
        id = "4.7-08",
        reason = ManualReason.COSTLY
    ) {
        prepare(SAMPLE)
        title(text = "El chat pregunta con botones y hace las sumas")

        steps(
            text =
            "1. Con el modelo Mediano, abre la Sample Vault y el chat.\n2. Pregunta: " +
                "summarize the meeting\n3. Haz clic en uno de los botones que ofrece.\n4. Pregunta: " +
                "how many books do I still need to read for my reading challenge?"
        )

        expected(
            text =
            "El paso 2 termina en una pregunta con un botón por cada reunión, sin resumir ninguna " +
                "por su cuenta; el clic manda esa reunión como siguiente mensaje y el chat la " +
                "resume. El paso 4 responde 7, con 17 de 24."
        )
    }

    manual(
        id = "4.7-09",
        reason = ManualReason.COSTLY
    ) {
        prepare(SAMPLE)
        title(text = "El chat conversa, abre notas y paneles, y describe la bóveda")

        steps(
            text =
            "1. Con el modelo Mediano, abre la " +
                "Sample Vault y el chat.\n2. Escribe: hello!\n3. Escribe: open " +
                "today's note\n4. Escribe: show the backlinks\n5. Escribe: what does my vault hold?"
        )

        expected(
            text =
            "El saludo se responde como conversación, sin pasos listados. La nota de hoy se abre y el " +
                "panel de backlinks aparece al momento, sin tarjeta que confirmar. El paso 5 describe las " +
                "carpetas y los temas de la bóveda."
        )
    }
}
