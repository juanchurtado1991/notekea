package app.notekea.autotest.cases.m1

import androidx.compose.ui.input.key.Key
import app.notekea.autotest.cursorToNoteStart
import app.notekea.autotest.notekea
import app.notekea.autotest.openNote
import app.notekea.autotest.pressInWindow
import com.testbuddy.CaseFailure
import com.testbuddy.ManualReason
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.ShortcutModifier.Shift
import com.testbuddy.SuiteBuilder

private const val READ_MODE = "Read mode"
private const val RAW_BOLD = "Some **bold**"
private const val DRAWN_TASK = "A task to do"
private const val OPEN_READ_MODE =
    "1. En Notekea, abre la Playground Vault (mira DATOS DE PRUEBA arriba).\n2. Abre la nota Editor › Read mode."

/** The cases of 1.5 that show a note: how it looks, finding in it, Source and its links (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.editorViewCases() {
    editLook()
    findInNote()
    sourceAndBack()
    noteLinks()
    readingLook()
    createdKept()
}

private fun SuiteBuilder.editLook() {
    manual(
        id = "1.5-08",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "La línea donde está el cursor muestra sus signos de Markdown")

        steps(
            text =
            "$OPEN_READ_MODE\n3. Haz clic en las palabras Some bold de la nota y mira esa " +
                "línea y las demás.\n4. Haz clic en Fuente (</>) al final de la barra de " +
                "herramientas, mira, y haz clic de nuevo."
        )

        expected(
            text =
            "La línea en la que hiciste clic muestra sus signos de Markdown (**, *, `, #) " +
                "atenuados, con la negrita y la cursiva como tales, y la barra de herramientas " +
                "marca Negrita (B) en azul mientras el cursor está en la palabra en negrita; las " +
                "demás líneas se quedan como se leen. Fuente muestra toda la nota tal como está " +
                "escrita, con los signos atenuados, los enlaces y etiquetas en azul y el código " +
                "sobre fondo sombreado; el segundo clic dibuja la nota de nuevo."
        )
    }
}

private fun SuiteBuilder.findInNote() {
    automated(
        id = "1.5-09",
        title = "Finding in a note"
    ) {
        openPlaygroundNote(
            name = READ_MODE,
            shownText = RAW_BOLD
        )

        press(shortcut = Mod + Key.F)
        type(text = "arepas")
        expectText(text = "1 of 2")
        press(key = Key.Enter)
        expectText(text = "2 of 2")
        press(key = Key.Enter)
        expectText(text = "1 of 2")
        press(shortcut = Shift + Key.Enter)
        expectText(text = "2 of 2")
        press(key = Key.Escape)
        expectNoText(text = "2 of 2")
    }
}

private fun SuiteBuilder.sourceAndBack() {
    automated(
        id = "1.5-10",
        title = "Source shows the note as written, and its button draws it again"
    ) {
        openPlaygroundNote(
            name = READ_MODE,
            shownText = RAW_BOLD
        )

        // A task drawn in a block the cursor is not in is a text of its own; as written it is part of the note's.
        expectText(text = DRAWN_TASK)
        pressInWindow(shortcut = Mod + Key.E)
        expectNoText(text = DRAWN_TASK)
        clickIcon(label = "Source: the note as written")
        expectText(text = DRAWN_TASK)
    }
}

private fun SuiteBuilder.noteLinks() {
    automated(
        id = "1.5-11",
        title = "Links open notes, make missing ones and open the web"
    ) {
        openPlaygroundNote(
            name = READ_MODE,
            shownText = RAW_BOLD
        )

        // In the cursor's block the words are the editor's text, not links.
        cursorToNoteStart()

        clickWords(words = "Arepas")

        expectText(
            text = "2 cups pre-cooked white corn flour",
            substring = true
        )

        openNote(
            name = READ_MODE,
            shownText = "A finished task"
        )

        clickWords(words = "the coffee")

        expectText(
            text = "Half strong coffee",
            substring = true
        )

        openNote(
            name = READ_MODE,
            shownText = "A finished task"
        )

        clickWords(words = "A note that does not exist yet")
        expectFile(path = "$PLAYGROUND/A note that does not exist yet.md")

        openNote(
            name = READ_MODE,
            shownText = "A finished task"
        )

        clickWords(words = "in a new folder")
        expectFile(path = "$PLAYGROUND/Drafts/A note in a folder.md")

        openNote(
            name = READ_MODE,
            shownText = "A finished task"
        )

        clickWords(words = "Pasta e fagioli")
        expectText(text = "Pasta e fagioli")

        openNote(
            name = READ_MODE,
            shownText = "A finished task"
        )

        clickWords(words = "the web")

        ui
            .waitUntil(timeoutMillis = 10_000) {
                "https://example.com" in notekea().uriHandler.opened
            }

        val codeLink = "not a link inside code"

        if (notekea().uriHandler.opened.any {
                codeLink in it
            }
        ) {
            throw CaseFailure(message = "expected no link inside code")
        }
    }
}

private fun SuiteBuilder.readingLook() {
    manual(
        id = "1.5-12",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "Cómo se lee una nota")
        steps(text = "$OPEN_READ_MODE\n3. Mira la nota sin hacer clic en su texto.")

        expected(
            text =
            "Los encabezados, las listas, las casillas y la cita se dibujan como tales, y " +
                "los dos bloques de código se colorean según su lenguaje (Kotlin y Python)."
        )
    }
}

private fun SuiteBuilder.createdKept() {
    manual(
        id = "1.5-13",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)
        title(text = "Guardar una nota conserva la fecha en que se creó")

        steps(
            text =
            "$OPEN_READ_MODE\n3. Mira Creada en Información de la nota, en el panel " +
                "lateral.\n4. Escribe una palabra en la nota, espera a que diga Guardado, luego " +
                "abre otra nota y vuelve."
        )

        expected(
            text =
            "En Windows y macOS, Creada muestra la misma fecha y hora que antes. Linux no " +
                "puede conservarla, así que este caso no aplica ahí."
        )
    }
}
