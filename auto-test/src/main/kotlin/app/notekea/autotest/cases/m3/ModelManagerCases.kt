package app.notekea.autotest.cases.m3

import app.notekea.autotest.cases.m1.PLAYGROUND
import app.notekea.autotest.cases.m1.WELCOME
import app.notekea.autotest.openVault
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val OPEN_AI_MODEL =
    "1. En Notekea, abre la Playground Vault (mira DATOS DE PRUEBA arriba).\n2. Haz clic en Ajustes (el " +
        "engranaje abajo en la barra estrecha de la izquierda) y luego en Modelo de IA en la lista de la izquierda."

/** The cases of 3.1, the model manager (`MVP_SMOKE_TEST.md`): Settings → AI model. */
fun SuiteBuilder.modelManagerCases() {
    recommendedSize()
    startPauseAndCancel()
    downloadAndResume()
    cancelAndDelete()
    droppedConnection()
}

private fun SuiteBuilder.recommendedSize() {
    automated(
        id = "3.1-01",
        title = "Settings → AI model recommends a size, with the others folded under it"
    ) {
        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        clickIcon(label = "Settings")
        click(text = "AI model")
        expectText(text = "Recommended")
        expectText(text = "OTHER SIZES")
        click(text = "OTHER SIZES")
        expectText(text = "Small · Qwen3.5 2B")
        expectText(text = "Medium · Qwen3.5 4B")
        expectText(text = "Large · Qwen3.5 9B")
    }
}

private fun SuiteBuilder.startPauseAndCancel() {
    automated(
        id = "3.1-05",
        title = "A download shows its progress, pauses and is cancelled, leaving nothing behind"
    ) {
        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        clickIcon(label = "Settings")
        click(text = "AI model")
        click(text = "Download")
        expectText(text = "Pause")

        expectText(
            text = " GB of ",
            substring = true
        )

        click(text = "Pause")

        expectText(
            text = "Paused · ",
            substring = true
        )

        expectText(text = "Resume")
        click(text = "Cancel")
        expectText(text = "Download")
        expectNoText(text = "Resume")
    }
}

private fun SuiteBuilder.downloadAndResume() {
    manual(
        id = "3.1-02",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "El modelo recomendado se descarga, se pausa, se reanuda y queda en uso")

        steps(
            text =
            "$OPEN_AI_MODEL\n3. Haz clic en Descargar en la tarjeta marcada como " +
                "Recomendado.\n4. Cuando la barra pase de un cuarto, haz clic en Pausar, pulsa " +
                "Esc para cerrar Ajustes y vuelve a abrir Modelo de IA.\n5. Haz clic en Reanudar " +
                "y espera a que termine la descarga (de unos minutos a media hora, según la " +
                "conexión)."
        )

        expected(
            text =
            "La barra crece con los gigabytes descargados del total y la velocidad (por " +
                "ejemplo 1.9 GB de 3.2 GB · 24.5 MB/s). En el paso 4 se queda en pausa donde " +
                "estaba, también después de cerrar Ajustes. En el paso 5 sigue desde ahí, no " +
                "desde el principio, muestra Revisando el archivo… un momento, y luego la " +
                "tarjeta dice En uso, con Eliminar al lado."
        )
    }
}

private fun SuiteBuilder.cancelAndDelete() {
    manual(
        id = "3.1-03",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "Eliminar un modelo pregunta primero y libera el espacio")

        steps(
            text =
            "$OPEN_AI_MODEL\n3. En el modelo en uso (descarga uno primero si ninguno dice En " +
                "uso), haz clic en Eliminar y luego en Cancelar en la pregunta.\n4. Haz clic de " +
                "nuevo en Eliminar y confirma con Eliminar."
        )

        expected(
            text =
            "El paso 3 conserva el modelo. El paso 4 lo elimina: la tarjeta vuelve a mostrar " +
                "Descargar y ningún tamaño dice En uso."
        )
    }
}

private fun SuiteBuilder.droppedConnection() {
    manual(
        id = "3.1-04",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)

        title(
            text =
            "Si se cae la conexión, la descarga se detiene y luego sigue desde donde se " +
                "detuvo"
        )

        steps(
            text =
            "$OPEN_AI_MODEL\n3. Haz clic en Descargar en un tamaño que no esté " +
                "descargado.\n4. Mientras se descarga, apaga el Wi-Fi o desconecta el cable de " +
                "red y espera medio minuto.\n5. Vuelve a encender la conexión y haz clic en " +
                "Reintentar."
        )

        expected(
            text =
            "El paso 4 muestra que la descarga se detuvo y pide revisar la conexión, con " +
                "Reintentar. En el paso 5 la barra sigue desde donde se detuvo, no desde cero."
        )
    }
}
