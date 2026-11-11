package app.notekea.autotest.cases.m1

import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val OPEN_PLAYGROUND =
    "1. En Notekea, abre la Playground Vault (mira DATOS DE PRUEBA arriba)."

/** The cases of 1.2, the theme and the design system (`MVP_SMOKE_TEST.md`): all of them need a person's eyes. */
fun SuiteBuilder.themeCases() {
    fonts()
    lightAndDark()
    roundedCorners()
    keyboardFocus()
    screenReader()
    noDeveloperTools()
    narrowWindow()
}

private fun SuiteBuilder.fonts() {
    manual(
        id = "1.2-01",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "Las fuentes de la app")

        steps(
            text =
            "$OPEN_PLAYGROUND\n2. Abre la nota Editor › Read mode y mira su texto y sus " +
                "bloques de código."
        )

        expected(
            text =
            "El texto usa la fuente limpia propia de Notekea (Inter), no la fuente habitual " +
                "de tu sistema; el código usa una fuente monoespaciada donde todas las letras " +
                "tienen el mismo ancho (JetBrains Mono)."
        )
    }
}

private fun SuiteBuilder.lightAndDark() {
    manual(
        id = "1.2-02",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "Colores en modo claro y oscuro")

        steps(
            text =
            "$OPEN_PLAYGROUND\n2. Con Ajustes → Apariencia como empieza (Modo Sistema, tema " +
                "Azul Workspace), mira alrededor; luego cambia tu sistema a modo oscuro (o " +
                "claro, si está en oscuro) y vuelve a mirar."
        )

        expected(
            text =
            "El azul es el color principal y el naranja marca detalles pequeños. En los dos " +
                "modos todos los textos se leen fácilmente."
        )
    }
}

private fun SuiteBuilder.roundedCorners() {
    manual(
        id = "1.2-03",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "Esquinas redondeadas")

        steps(
            text =
            "$OPEN_PLAYGROUND\n2. Mira los botones, los campos de texto y las filas de la " +
                "lista de la izquierda."
        )

        expected(text = "Tienen esquinas ligeramente redondeadas, no forma de píldora.")
    }
}

private fun SuiteBuilder.keyboardFocus() {
    manual(
        id = "1.2-04",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "Moverse por la ventana con el teclado")

        steps(
            text =
            "$OPEN_PLAYGROUND\n2. Sin el mouse, pulsa Tab varias veces, luego Shift+Tab, y " +
                "pulsa Enter o Espacio sobre un botón."
        )

        expected(
            text =
            "Cada vez, un anillo claro muestra qué elemento tiene el teclado, y Enter o " +
                "Espacio lo usa."
        )
    }
}

private fun SuiteBuilder.screenReader() {
    manual(
        id = "1.2-05",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)
        title(text = "Un lector de pantalla lee la ventana")

        steps(
            text =
            "1. Activa el lector de pantalla de tu sistema: Narrador (Ctrl+Win+Enter) en " +
                "Windows, VoiceOver (Cmd+F5) en macOS, Orca en Linux.\n2. Abre la Playground " +
                "Vault en Notekea y recórrela con Tab.\n3. Apaga el lector de pantalla."
        )

        expected(
            text =
            "Los botones, pestañas y filas se leen en voz alta con su nombre y lo que son " +
                "(\"botón\", \"pestaña\")."
        )
    }
}

private fun SuiteBuilder.noDeveloperTools() {
    manual(
        id = "1.2-06",
        reason = ManualReason.PACKAGED
    ) {
        title(text = "Sin herramientas de desarrollo en la app instalada")
        steps(text = "1. En Notekea, revisa Acerca de, Ajustes y los menús del clic derecho.")
        expected(text = "Nada ofrece una galería de diseño ni una pantalla de desarrollo.")
    }
}

private fun SuiteBuilder.narrowWindow() {
    manual(
        id = "1.2-07",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "Una ventana muy estrecha")

        steps(
            text =
            "$OPEN_PLAYGROUND\n2. Abre algunas notas y luego haz la ventana tan estrecha " +
                "como se pueda."
        )

        expected(
            text =
            "Las etiquetas de la barra inferior y de las pestañas se quedan en una línea y " +
                "terminan con … cuando no caben; los números nunca se parten en dos; nada queda " +
                "cortado."
        )
    }
}
