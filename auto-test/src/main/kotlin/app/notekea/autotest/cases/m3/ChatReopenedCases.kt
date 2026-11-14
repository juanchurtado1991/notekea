package app.notekea.autotest.cases.m3

import app.notekea.autotest.cases.m1.PLAYGROUND
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

/**
 * The chat kept as a conversation (`docs/agent-loop.md`, step 3): opened again, it shows what it looked up and how
 * a proposal ended, and knows it when asked. By hand: it needs the model and the app closed in the middle.
 */
internal fun SuiteBuilder.chatReopenedCases() {
    manual(
        id = "3.3-15",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "El chat reabierto muestra lo que buscó y propuso, y lo recuerda")

        steps(
            text =
            "1. Con un modelo de chat, en modo Agente pide: haz una nota con una tabla de mis " +
                "recetas.\n2. Cuando aparezca la tarjeta, haz clic en Cancelar.\n3. Pregunta " +
                "otra cosa y, mientras responde, cierra Notekea.\n4. Abre Notekea y el chat " +
                "(Mod+L).\n5. Pregunta: ¿qué hiciste con la tabla que te pedí?"
        )

        expected(
            text =
            "Al reabrir, la primera petición muestra sus pasos (lo que buscó y leyó) y la " +
                "línea Cancelado. No se cambió nada, en lugar de la tarjeta. La pregunta que " +
                "se cortó al cerrar está en el chat, marcada Detenida. La última respuesta " +
                "dice que propuso la nota y que se canceló, sin inventar que existe."
        )
    }
}
