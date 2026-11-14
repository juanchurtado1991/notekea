package app.notekea.autotest.cases.m4

import app.notekea.autotest.cases.m1.PLAYGROUND
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

/**
 * A vault left in the middle of a merge when Notekea was closed during a sync (left open on 2026-11-13, done on
 * 2026-11-14). By hand: the state is made with Git in a terminal, outside the app.
 */
internal fun SuiteBuilder.repairCases() {
    manual(
        id = "4.2-07",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)
        title(text = "Una bóveda que quedó a medio fusionar se avisa y se repara eligiendo cómo")

        steps(
            text =
            "1. Con una bóveda que sincroniza, cambia la misma nota en las dos computadoras y " +
                "sincroniza solo una.\n2. En la otra, en una terminal dentro de la bóveda, " +
                "ejecuta git fetch y luego git merge origin/main, sin resolver el conflicto.\n" +
                "3. Abre la bóveda en Notekea y haz clic en Reparar… en el aviso.\n4. Prueba " +
                "Conservar mis cambios; repite los pasos 1 y 2 y prueba Volver a como está en " +
                "el repositorio."
        )

        expected(
            text =
            "La barra de estado dice que la sincronización se interrumpió y el aviso dice que " +
                "nada se sincroniza hasta reparar. El diálogo ofrece los dos caminos, sugiere " +
                "Conservar mis cambios y nombra los archivos cambiados en esta computadora. " +
                "Conservar mis cambios deja la nota como la guardaste aquí, sin marcas de " +
                "conflicto, y la siguiente sincronización muestra el conflicto de siempre para " +
                "revisarlo. Volver al repositorio deja la nota del repositorio y una copia de " +
                "la local en la carpeta .trash de la bóveda. Ahora no no cambia nada."
        )
    }
}
