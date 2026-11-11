package app.notekea.autotest.cases.m1

import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

/** The case of 1.1 about the system's app menu on macOS (`MVP_SMOKE_TEST.md`). */
internal fun SuiteBuilder.menuCases() {
    manual(
        id = "1.1-10",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)
        title(text = "La barra de menús en macOS")

        steps(
            text =
            "Solo en macOS; en otro sistema márcalo como Pasó.\n1. En Notekea, abre la " +
                "Playground Vault (mira DATOS DE PRUEBA arriba).\n2. Abre el menú Notekea arriba " +
                "en la pantalla y haz clic en Acerca de Notekea y luego en Ajustes…\n3. Abre los " +
                "menús Archivo, Nota, Visualización, Ir y Ayuda, y elige Archivo › Nueva nota y " +
                "Visualización › Abrir el grafo.\n4. Pulsa ⌘N."
        )

        expected(
            text =
            "El menú Notekea abre los mismos Acerca de y Ajustes que la barra de herramientas " +
                "lateral. Cada menú muestra sus acciones con su atajo al lado; Nueva nota crea una " +
                "nota y Abrir el grafo abre el grafo, igual que sus atajos. ⌘N crea una sola nota, " +
                "no dos."
        )
    }
}
