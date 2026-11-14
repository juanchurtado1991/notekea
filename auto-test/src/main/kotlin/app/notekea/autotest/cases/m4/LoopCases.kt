package app.notekea.autotest.cases.m4

import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

/**
 * Agent mode with the one loop (`docs/agent-loop.md`, step 5), turned on in Settings → General while it is a
 * preview. By hand: it needs the Medium model.
 */
private const val SAMPLE_VAULT = "Sample Vault"

internal fun SuiteBuilder.loopCases() {
    manual(
        id = "4.7-08",
        reason = ManualReason.COSTLY
    ) {
        prepare(SAMPLE_VAULT)
        title(text = "Con el nuevo bucle, el modo Agente pregunta con botones y hace las sumas")

        steps(
            text =
            "1. Con el modelo Mediano, abre Ajustes → General y activa Modo agente con el nuevo " +
                "bucle.\n2. Abre la Sample Vault y el chat; elige el modo Agente.\n3. Pregunta: " +
                "summarize the meeting\n4. Haz clic en uno de los botones que ofrece.\n5. Pregunta: " +
                "how many books do I still need to read for my reading challenge?\n6. Desactiva el " +
                "ajuste y repite el paso 3."
        )

        expected(
            text =
            "El paso 3 termina en una pregunta con un botón por cada reunión, sin resumir ninguna " +
                "por su cuenta; el clic manda esa reunión como siguiente mensaje y el chat la " +
                "resume. El paso 5 responde 7, con 17 de 24. Con el ajuste apagado, el modo Agente " +
                "responde como antes, sin botones."
        )
    }
}
