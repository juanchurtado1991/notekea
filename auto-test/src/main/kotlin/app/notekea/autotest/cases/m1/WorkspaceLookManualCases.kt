package app.notekea.autotest.cases.m1

import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val OPEN_PLAYGROUND =
    "1. En Notekea, abre la Playground Vault (mira DATOS DE PRUEBA arriba)."

/** The cases of 1.14 a person must look at: the tool rail, the scrollbars and the workspace's details. */
internal fun SuiteBuilder.workspaceLookManualCases() {
    toolRail()
    scrollbars()
    workspaceLook()
}

private fun SuiteBuilder.toolRail() {
    manual(
        id = "1.14-10",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "La barra de herramientas lateral, Acerca de y Ajustes se ven bien")

        steps(
            text =
            "$OPEN_PLAYGROUND\n2. Abre dos notas. Pasa el puntero por cada icono de la barra " +
                "estrecha de la izquierda.\n3. Haz clic en Acerca de Notekea (la ⓘ de abajo), " +
                "míralo y pulsa Esc.\n4. Haz clic en Ajustes (el engranaje) y ciérralo con " +
                "✕.\n5. Haz clic dos veces en Panel lateral (el segundo icono).\n6. Haz clic en " +
                "Abrir otra bóveda."
        )

        expected(
            text =
            "Arriba la barra tiene el logo de Notekea, luego Archivos, Panel lateral, Buscar " +
                "notas y Hoy; abajo tiene Abrir otra bóveda, Acerca de Notekea y Ajustes, cada " +
                "uno con su nombre al pasar el puntero. La pestaña de la nota abierta sube con " +
                "esquinas redondeadas hacia la nota; las demás pestañas son discretas, con una " +
                "línea fina entre ellas y su ✕ solo al pasar el puntero. Acerca de muestra el " +
                "logo, Notekea, \"Versión 0.1.0-alpha\", GitHub y los atajos de esta computadora " +
                "como teclas (Notas, Escritura, Espacio de trabajo) con una barra de " +
                "desplazamiento al lado; Esc lo cierra. Ajustes se abre en General, con cada " +
                "sección en una barra lateral de iconos de colores, como los Ajustes del Sistema " +
                "de macOS, y Pregunta sobre los Ajustes a su lado. Panel lateral oculta y " +
                "muestra el panel de la derecha. Abrir otra bóveda muestra la pantalla de " +
                "bienvenida: el logo, Notekea y las acciones a la izquierda, las bóvedas " +
                "recientes a la derecha."
        )
    }
}

private fun SuiteBuilder.scrollbars() {
    manual(
        id = "1.14-11",
        reason = ManualReason.EYES
    ) {
        prepare(
            PLAYGROUND,
            "Personal Notes",
            "Work Obsidian",
            "Empty Vault",
            "Daily Vault",
            "Templates Vault"
        )

        title(text = "Las listas largas muestran una barra de desplazamiento fina")

        steps(
            text =
            "1. En Notekea, con Abrir otra bóveda (abajo a la izquierda) y Abrir carpeta…, " +
                "abre una tras otra estas bóvedas de la carpeta Test data: Personal Notes, Work " +
                "Obsidian, Empty Vault, Playground Vault, Daily Vault y Templates Vault.\n2. Haz " +
                "clic en Abrir otra bóveda y desplaza la lista Recientes.\n3. Abre la Playground " +
                "Vault, haz clic en Expandir todas las carpetas (sobre la lista de notas) y " +
                "desplaza la lista.\n4. Abre Editor/Outline practice y desplaza el índice de la " +
                "derecha."
        )

        expected(
            text =
            "Recientes muestra cinco bóvedas con una barra de desplazamiento fina a su " +
                "derecha y se desplaza por dentro hasta la sexta. La lista de notas y el índice " +
                "muestran una barra solo mientras tienen más de lo que cabe, y se oscurece al " +
                "pasar el puntero."
        )
    }
}

private fun SuiteBuilder.workspaceLook() {
    manual(
        id = "1.14-13",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "Las pestañas, la selección del árbol y la barra de estado se ven bien")

        steps(
            text =
            "$OPEN_PLAYGROUND\n2. Abre diez notas en pestañas y haz la ventana estrecha.\n3. " +
                "Haz clic en la ⌄ al final de la fila de pestañas y elige una pestaña.\n4. Haz " +
                "clic en una nota de la lista de la izquierda, luego en el texto de la nota, y " +
                "escribe una palabra mirando el final de la barra de abajo.\n5. Pasa el puntero " +
                "por el nombre de la bóveda arriba de la lista."
        )

        expected(
            text =
            "La fila de pestañas se desvanece al final; la ⌄ lista todas las pestañas " +
                "abiertas, con la abierta marcada, y permite elegir una; ninguna pestaña es más " +
                "estrecha que un nombre corto. La nota seleccionada en la lista es azul mientras " +
                "la lista tiene el teclado y gris cuando lo tiene el texto, sin anillo " +
                "alrededor. La lista muestra los encabezados MARCADORES y ARCHIVOS. Los paneles " +
                "se unen con líneas finas, sin un hueco oscuro. La barra de abajo termina con " +
                "\"Sin guardar\" mientras escribes y luego \"Guardado\". La carpeta de la bóveda " +
                "solo aparece al pasar el puntero por su nombre."
        )
    }
}
