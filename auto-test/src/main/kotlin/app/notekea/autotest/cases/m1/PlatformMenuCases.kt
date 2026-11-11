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
        title(text = "El menú de la app en macOS")

        steps(
            text =
            "Solo en macOS; en otro sistema márcalo como Pasó.\n1. En Notekea, abre la " +
                "Playground Vault (mira DATOS DE PRUEBA arriba).\n2. Abre el menú Notekea arriba " +
                "en la pantalla y haz clic en Acerca de Notekea y luego en Ajustes…"
        )

        expected(
            text =
            "El menú Notekea de macOS abre los mismos Acerca de y Ajustes que la barra de " +
                "herramientas lateral, y Notekea no agrega ningún otro menú a la barra."
        )
    }
}
