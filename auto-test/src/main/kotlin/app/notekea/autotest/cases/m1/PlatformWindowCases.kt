package app.notekea.autotest.cases.m1

import app.notekea.autotest.notekea
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val TITLE_WAIT_MILLIS = 10_000L

/** The cases of 1.1 about the window itself (`MVP_SMOKE_TEST.md`). */
internal fun SuiteBuilder.windowCases() {
    windowTitle()
    windowKept()
}

private fun SuiteBuilder.windowTitle() {
    automated(
        id = "1.1-12",
        title = "The window's title names the active note and the vault"
    ) {
        openPlaygroundNote(
            name = "Arepas",
            shownText = AREPAS_FLOUR
        )

        ui
            .waitUntil(timeoutMillis = TITLE_WAIT_MILLIS) {
                notekea().windowTitle == "Arepas — $PLAYGROUND — Notekea"
            }
    }
}

private fun SuiteBuilder.windowKept() {
    manual(
        id = "1.1-13",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)
        title(text = "La ventana vuelve como se dejó y se estrecha bien")

        steps(
            text =
            "1. En Notekea, abre la Playground Vault (mira DATOS DE PRUEBA arriba) y la nota " +
                "Welcome.\n2. Haz la ventana tan estrecha y tan baja como se pueda.\n3. Ponla de " +
                "unos 700 de ancho; muestra Archivos con el icono de arriba de la barra estrecha " +
                "si está oculto.\n4. Muévela a una esquina, sal de Notekea y vuelve a iniciarlo " +
                "desde Notekea QA.\n5. Maximízala, sal y vuelve a iniciarlo; luego quita la " +
                "maximización."
        )

        expected(
            text =
            "En el paso 2 la ventana se detiene en un tamaño donde todavía cabe una nota. En " +
                "el paso 3 Archivos flota sobre la nota, con una sombra, en lugar de apretarla. " +
                "Después del paso 4 la ventana está en el mismo lugar y con el mismo tamaño. " +
                "Después del paso 5 empieza maximizada y vuelve a ese tamaño. El título siempre " +
                "empieza con el nombre de la nota."
        )
    }
}
