package app.notekea.autotest.cases.m1

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.performTextReplacement
import app.notekea.autotest.cursorToNoteStart
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ManualReason
import com.testbuddy.ShortcutModifier.Shift
import com.testbuddy.SuiteBuilder
import com.testbuddy.WindowSize

private const val PAN_LINE = "Remember that the pan stays hot for a long time."
private const val PLAIN_LINES = "Buy corn flour\nBuy fresh cheese\nSoak the beans"
private const val CHECKLIST = "- [ ] Buy corn flour\n- [ ] Buy fresh cheese\n- [x] Soak the beans"
private const val TIP = "> [!tip] Tip\n> Use a pan"
private const val TYPED_CODE = "val hello = \"World\" -> x // != y"

/** The cases of 1.17 about More's formats, lists and code (`MVP_SMOKE_TEST.md`). */
internal fun SuiteBuilder.formattingMoreCases() {
    callouts()
    tableCodeDividerQuote()
    narrowToolbar()
    undoAndObsidian()
    listEditing()
    codeTyping()
}

private fun SuiteBuilder.callouts() {
    automated(
        id = "1.17-06",
        title = "Callout wraps the line or inserts one, folded when asked"
    ) {
        openFormatting()
        select(words = PAN_LINE)
        openFromMore(item = "Callout")

        expectText(
            text = "A colored box for a tip, a warning",
            substring = true
        )

        click(text = "Start folded")
        click(text = "Warning")
        expectInNote(text = "> [!warning]- Warning\n> $PAN_LINE")
        cursorAtEnd()
        openFromMore(item = "Callout")
        click(text = "Start folded")
        click(text = "Tip")
        expectInNote(text = "> [!tip] Tip\n> Write here")
        expectSelected(words = "Write here")
        type(text = "Use a cast-iron pan")
        expectInNote(text = "> [!tip] Tip\n> Use a cast-iron pan")
        cursorToNoteStart()
        expectNoText(text = PAN_LINE)
        click(text = "Warning")
        expectText(text = PAN_LINE)
    }
}

private fun SuiteBuilder.tableCodeDividerQuote() {
    automated(
        id = "1.17-07",
        title = "More inserts a table, a code block, a divider and a quote"
    ) {
        openFormatting()
        cursorAtEnd()
        openFromMore(item = "Code block")

        listOf("Plain text", "Python", "JavaScript", "TypeScript", "Java", "C#", "C++", "Go", "Rust", "Shell")
            .forEach { expectText(text = it) }

        click(text = "Kotlin")
        expectInNote(text = "```kotlin\ncode\n```")
        expectSelected(words = "code")
        cursorAtEnd()
        openFromMore(item = "Divider")
        expectInNote(text = "\n---")
        cursorAtEnd()
        openFromMore(item = "Quote")
        expectInNote(text = "\n> Quote")
        cursorToNoteStart()
        expectText(text = "Quote")
    }
}

private fun SuiteBuilder.narrowToolbar() {
    automated(
        id = "1.17-08",
        title = "In a narrow window the buttons that do not fit move to the top of More",
        window = WindowSize(
            width = 800,
            height = 800
        )
    ) {
        openFormatting()

        if (hasLabel(label = "Checklist")) {
            throw CaseFailure(message = "expected Checklist inside More")
        }

        select(words = PLAIN_LINES)
        clickIcon(label = "More")

        listOf("Checklist", "Bulleted list", "Insert template", "Web link").forEach {
            expectText(text = it)
        }

        click(text = "Checklist")
        expectInNote(text = "- [ ] Buy corn flour")
    }
}

private fun SuiteBuilder.undoAndObsidian() {
    manual(
        id = "1.17-09",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)

        title(
            text =
            "Deshacer revierte un cambio de la barra, y Obsidian muestra los mismos formatos"
        )

        steps(
            text =
            "1. En Notekea, abre la Playground Vault (mira DATOS DE PRUEBA arriba).\n2. Abre " +
                "Editor/Formatting practice, selecciona \"corn flour\" y haz clic en Negrita (B) " +
                "en la barra bajo el título.\n3. Pulsa Ctrl+Z (Cmd+Z en una Mac) y luego " +
                "Ctrl+Shift+Z (Cmd+Shift+Z).\n4. Da formato a la nota con la barra: un " +
                "encabezado, una lista de tareas, un enlace, una etiqueta, un destacado, una " +
                "tabla, un bloque de código y una cita. Espera a que diga \"Guardado\" abajo a " +
                "la derecha.\n5. Si Obsidian está instalado, abre ahí la Playground Vault y la " +
                "misma nota."
        )

        expected(
            text =
            "Ctrl+Z quita la negrita en un paso y Ctrl+Shift+Z la vuelve a poner. Obsidian " +
                "muestra el mismo encabezado, negrita, lista de tareas, enlace, etiqueta, " +
                "destacado, tabla, bloque de código y cita que dibuja Notekea."
        )
    }
}

private fun SuiteBuilder.listEditing() {
    automated(
        id = "1.17-10",
        title = "Enter, Tab and Backspace continue, nest and end lists and callouts"
    ) {
        openFormatting()

        editor().performTextReplacement(
            text =
            editorText().replace(PLAIN_LINES, CHECKLIST) + "\n$TIP"
        )

        cursorAt(words = "- [x] Soak the beans")
        press(key = Key.Enter)
        type(text = "Buy salt")
        expectInNote(text = "- [x] Soak the beans\n- [ ] Buy salt")
        press(key = Key.Enter)
        press(key = Key.Enter)
        expectInNote(text = "- [ ] Buy salt\n\n")
        cursorAt(words = "Buy fresh cheese")
        press(key = Key.Tab)
        expectInNote(text = "- [ ] Buy corn flour\n  - [ ] Buy fresh cheese")
        press(shortcut = Shift + Key.Tab)
        expectInNote(text = "- [ ] Buy corn flour\n- [ ] Buy fresh cheese")

        cursorAt(
            words = "Buy salt",
            before = true
        )

        press(key = Key.Backspace)
        expectInNote(text = "\nBuy salt")
        cursorAt(words = "> Use a pan")
        press(key = Key.Enter)
        expectInNote(text = "> Use a pan\n> ")
    }
}

private fun SuiteBuilder.codeTyping() {
    automated(
        id = "1.17-11",
        title = "Typing in a code block keeps every character as typed"
    ) {
        openFormatting()
        editor().performTextReplacement(text = editorText() + "\n```kotlin\ncode\n```\n")
        select(words = "code\n```")
        select(words = "code")
        type(text = TYPED_CODE)
        expectInNote(text = "```kotlin\n$TYPED_CODE\n```")
        cursorAt(words = "Weekend arepas")
        type(text = " ->")
        expectInNote(text = "Weekend arepas ->")

        expectFile(
            path = FORMATTING_FILE,
            containing = TYPED_CODE
        )
    }
}

private fun CaseScope.openFromMore(item: String) {
    clickIcon(label = "More")
    click(text = item)
}
