package app.notekea.autotest.cases.m2

import androidx.compose.ui.input.key.Key
import app.notekea.autotest.openNote
import app.notekea.autotest.openVault
import app.notekea.autotest.pressInWindow
import com.testbuddy.ManualReason
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.SuiteBuilder

private const val SAMPLE = "Sample Vault"
private const val OPEN_GRAPH =
    "1. En Notekea, abre la Sample Vault (mira DATOS DE PRUEBA arriba) y espera a que la barra de abajo " +
        "diga \"41 notas listas para buscar\".\n2. Abre Recipes/Arepas y pulsa Ctrl+G (Cmd+G en macOS)."

/** The cases of 2.7, the graph (`MVP_SMOKE_TEST.md`): its tab, opened around a note or on the whole vault. */
fun SuiteBuilder.graphCases() {
    graphFromNote()
    aroundANote()
    wholeVault()
    keyboard()
    depthAndMotion()
    findANote()
    linkFromTheGraph()
    noNote()
    doubleClickAndSplit()
}

private fun SuiteBuilder.graphFromNote() {
    automated(
        id = "2.7-01",
        title = "The graph opens around a note, and a note chosen on it opens"
    ) {
        openVault(
            name = SAMPLE,
            shownText = "Home"
        )

        openNote(
            name = "Arepas",
            shownText = "pre-cooked corn flour"
        )

        pressInWindow(shortcut = Mod + Key.G)
        expectText(text = "This note")
        clickIcon(label = "Weekly menu")
        expectText(text = "Connected to")
        click(text = "Open note")
        expectNoText(text = "Connected to")
    }
}

private fun SuiteBuilder.aroundANote() {
    manual(
        id = "2.7-02",
        reason = ManualReason.EYES
    ) {
        prepare(SAMPLE)

        title(
            text =
            "Alrededor de una nota el grafo se lee bien, y elegir una nota la lleva al centro"
        )

        steps(
            text =
            "$OPEN_GRAPH\n3. Pasa el puntero sobre un círculo.\n4. Haz clic en otro círculo."
        )

        expected(
            text =
            "Arepas está en el centro con sus notas alrededor y un nombre bajo cada una. Los " +
                "enlaces son líneas continuas, las notas relacionadas discontinuas. Al pasar el " +
                "puntero se iluminan una nota y sus vecinas. La nota en la que hiciste clic se " +
                "desliza al centro y el panel de la derecha la muestra."
        )
    }
}

private fun SuiteBuilder.wholeVault() {
    manual(
        id = "2.7-03",
        reason = ManualReason.EYES
    ) {
        prepare(SAMPLE)
        title(text = "Toda la bóveda muestra sus enlaces, hace zoom y deja fuera una carpeta")

        steps(
            text =
            "$OPEN_GRAPH\n3. Haz clic en Toda la bóveda.\n4. Desplaza la rueda sobre el mapa " +
                "para acercar, y arrástralo.\n5. Haz clic en Mostrar huérfanas.\n6. Haz clic en " +
                "una carpeta de la leyenda de abajo; luego cierra y vuelve a abrir la bóveda."
        )

        expected(
            text =
            "Círculos de colores se reparten por el mapa, un color por carpeta. Los nombres " +
                "aparecen al acercar. Las notas sin enlaces aparecen con Mostrar huérfanas. Las " +
                "notas de la carpeta salen del mapa y siguen fuera después de reabrir la bóveda; " +
                "hacer clic en ella las devuelve."
        )
    }
}

private fun SuiteBuilder.keyboard() {
    manual(
        id = "2.7-04",
        reason = ManualReason.EYES
    ) {
        prepare(SAMPLE)
        title(text = "El grafo funciona con el teclado")

        steps(
            text =
            "$OPEN_GRAPH\n3. Pulsa las flechas y luego dos veces Espacio.\n4. Pulsa " +
                "Enter.\n5. Vuelve a la pestaña del grafo y pulsa Esc."
        )

        expected(
            text =
            "Las flechas se mueven entre notas conectadas. Espacio oculta y muestra el panel " +
                "lateral. Enter abre la nota. Esc vuelve a Arepas."
        )
    }
}

private fun SuiteBuilder.depthAndMotion() {
    manual(
        id = "2.7-05",
        reason = ManualReason.EYES
    ) {
        prepare(SAMPLE)

        title(
            text =
            "La profundidad agrega un anillo más lejano, y reducir movimiento detiene el " +
                "deslizamiento"
        )

        steps(
            text =
            "$OPEN_GRAPH\n3. Haz clic en 2 en la barra de herramientas.\n4. Arrastra el " +
                "mapa.\n5. Activa Reducir movimiento en Ajustes, Apariencia, y haz clic en un " +
                "círculo."
        )

        expected(
            text =
            "Aparece un segundo anillo, con sus notas más pequeñas y tenues, y se mueve un " +
                "poco menos al arrastrar. Con Reducir movimiento la nota salta al centro al " +
                "instante."
        )
    }
}

private fun SuiteBuilder.findANote() {
    manual(
        id = "2.7-06",
        reason = ManualReason.EYES
    ) {
        prepare(SAMPLE)
        title(text = "La búsqueda te lleva al grafo de una nota con un deslizamiento")

        steps(
            text =
            "$OPEN_GRAPH\n3. Haz clic en Toda la bóveda, arrastra el mapa a un lado y haz " +
                "zoom.\n4. Haz clic en la lupa de arriba a la derecha, o pulsa Ctrl+F (Cmd+F en " +
                "macOS), y escribe soup.\n5. Pulsa Enter y luego Esc."
        )

        expected(
            text =
            "Las notas que coinciden conservan su color y nombre mientras las demás se " +
                "desvanecen, y la barra las cuenta. Enter desliza el mapa al grafo de esa nota: " +
                "llega al centro, sus vecinas salen a su alrededor y las demás notas se " +
                "desvanecen. Esc cierra la barra y las flechas vuelven a recorrer el mapa."
        )
    }
}

private fun SuiteBuilder.linkFromTheGraph() {
    manual(
        id = "2.7-07",
        reason = ManualReason.EYES
    ) {
        prepare(SAMPLE)
        title(text = "Enlazar une dos notas desde el grafo, y Deshacer lo revierte")

        steps(
            text =
            "$OPEN_GRAPH\n3. Haz clic en círculos hasta que el panel lateral muestre " +
                "Relacionadas, sin enlazar.\n4. Haz clic en Enlazar en una fila.\n5. Haz clic en " +
                "Abrir nota y mira el final de la nota, la que mostraba el panel; luego vuelve a " +
                "la pestaña del grafo.\n6. Haz clic en Enlazar en otra fila y luego en Deshacer."
        )

        expected(
            text =
            "La línea discontinua se vuelve continua y el panel dice Enlazada con esa nota. " +
                "La nota termina con el nuevo [[enlace]] y el resto no cambia. Deshacer quita el " +
                "enlace de la nota y del mapa."
        )
    }
}

private fun SuiteBuilder.noNote() {
    automated(
        id = "2.7-08",
        title = "With no note open the graph asks to find one"
    ) {
        openVault(
            name = SAMPLE,
            shownText = "Home"
        )

        pressInWindow(shortcut = Mod + Key.G)
        expectText(text = "No note selected")
        click(text = "Find a note")
        type(text = "arepas")
        expectText(text = "1 of 1")
        press(key = Key.Enter)
        expectNoText(text = "No note selected")
        expectText(text = "Arepas")
    }
}

private fun SuiteBuilder.doubleClickAndSplit() {
    manual(
        id = "2.7-09",
        reason = ManualReason.EYES
    ) {
        prepare(SAMPLE)
        title(text = "Un doble clic abre esa nota, y Buscar se queda con el panel en uso")

        steps(
            text =
            "$OPEN_GRAPH\n3. Haz doble clic en un círculo que no esté seleccionado.\n4. Abre " +
                "la pestaña del grafo al lado de esa nota (arrastra su pestaña a la derecha), " +
                "haz clic en la nota y pulsa Ctrl+F (Cmd+F en macOS).\n5. Haz clic en el mapa " +
                "del grafo y pulsa Ctrl+F (Cmd+F en macOS)."
        )

        expected(
            text =
            "Se abre la nota en la que hiciste doble clic, no la que estaba seleccionada " +
                "antes. En el paso 4 Buscar se abre en la nota, y en el paso 5 la barra de " +
                "búsqueda se abre en el grafo."
        )
    }
}
