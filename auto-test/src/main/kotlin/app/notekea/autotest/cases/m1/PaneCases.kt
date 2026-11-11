package app.notekea.autotest.cases.m1

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasText
import app.notekea.autotest.SmokeScenarios
import app.notekea.autotest.clickInTree
import app.notekea.autotest.expectNotInOutline
import app.notekea.autotest.inOutline
import app.notekea.autotest.noteText
import app.notekea.autotest.openNote
import app.notekea.autotest.openVault
import app.notekea.autotest.pressInWindow
import app.notekea.autotest.relaunch
import app.notekea.autotest.typeAtEnd
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ManualReason
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.ShortcutModifier.Shift
import com.testbuddy.SuiteBuilder
import com.testbuddy.isShown

private const val WELCOME_TEXT = "This is a sample vault"
private const val SHARED_NOTE = "Shared note"
private const val SHARED_NOTE_TEXT = "Another app changes or deletes this note"

// A heading in the middle of the note, as the case says: the end of a note is where the scroll stops short.
private const val OUTLINE_MIDDLE = "Serving note 1:"
private const val TYPED_BEFORE_MOVE = "Typed just before the move"
private const val OPEN_PLAYGROUND =
    "1. En Notekea, abre la Playground Vault (mira DATOS DE PRUEBA arriba)."

/** The cases of 1.11 about two panes, the tabs' memory and the folders' cards (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.paneCases() {
    splitRight()
    tabBetweenPanes()
    placeKept()
    sessionKept()
    folderCards()
    nativeTitleBar()
    windowSize()
    quickFolderClicks()
    reorderTabs()
    tabSlides()
    tabMovedWhileTyping()
}

private fun SuiteBuilder.splitRight() {
    automated(
        id = "1.11-07",
        title = "Mod+Shift+D moves the tab to a pane on the right"
    ) {
        openTwoNotes()
        pressInWindow(shortcut = Mod + Shift + Key.D)
        expectShown(text = WELCOME_TEXT)
        expectShown(text = AREPAS_FLOUR)

        if (tabLeft(name = "Arepas") < tabLeft(name = WELCOME)) {
            throw CaseFailure(message = "expected Arepas in the right pane")
        }

        pressInWindow(shortcut = Mod + Shift + Key.D)
        expectShown(text = WELCOME_TEXT)
        rightClickTab(name = WELCOME)
        expectText(text = "Move to the other pane")
        expectNoText(text = "Split right")
        press(key = Key.Escape)
        noteText(part = WELCOME_TEXT).clickWhenStill()
        expectNotInOutline(heading = "Ingredients")
    }
}

private fun SuiteBuilder.tabBetweenPanes() {
    automated(
        id = "1.11-08",
        title = "A tab dragged to the other pane moves there, and an empty pane closes"
    ) {
        openTwoNotes()
        pressInWindow(shortcut = Mod + Shift + Key.D)
        expectShown(text = AREPAS_FLOUR)
        val target = tab(name = "Arepas").fetchSemanticsNode().boundsInRoot

        dragTab(
            name = WELCOME,
            by = Offset(
                x = target.right + DROP_MARGIN - tabCenterX(name = WELCOME),
                y = 0f
            )
        )

        expectTabOrder(names = listOf("Arepas", WELCOME))
        expectShown(text = WELCOME_TEXT)

        expectNoText(
            text = AREPAS_FLOUR,
            substring = true
        )
    }
}

private fun SuiteBuilder.placeKept() {
    automated(
        id = "1.11-09",
        title = "A tab shows the same part of its note when it comes back"
    ) {
        openPlaygroundNote(
            name = "Outline practice",
            shownText = "Arepas guide"
        )

        clickInOutline(heading = "Serving")
        expectOnScreen(text = OUTLINE_MIDDLE)
        clickInTree(text = WELCOME)
        tab(name = "Outline practice").clickWhenStill()
        expectOnScreen(text = OUTLINE_MIDDLE)
    }
}

private fun SuiteBuilder.sessionKept() {
    automated(
        id = "1.11-10",
        title = "The tabs and the split come back, without a note deleted while closed"
    ) {
        openTwoNotes()
        pressInWindow(shortcut = Mod + Shift + Key.D)
        expectShown(text = AREPAS_FLOUR)
        relaunch()
        expectShown(text = WELCOME_TEXT)
        expectShown(text = AREPAS_FLOUR)

        openNote(
            name = SHARED_NOTE,
            shownText = SHARED_NOTE_TEXT
        )

        relaunch {
            SmokeScenarios(
                smokeData = folders.data,
                scratch = folders.data
            ).deleteNote()
        }

        expectShown(text = AREPAS_FLOUR)
        expectNoTab(name = SHARED_NOTE)

        expectNoText(
            text = "couldn't be opened",
            substring = true
        )
    }
}

private fun SuiteBuilder.folderCards() {
    automated(
        id = "1.11-11",
        title = "A folder opens as a grid of cards that open what they show"
    ) {
        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        clickInTree(text = "Editor")

        listOf("Embeds", "Images and files", "Read mode").forEach { card(name = it) }

        if (card(name = "Embeds").top() > card(name = "Read mode").top()) {
            throw CaseFailure(message = "expected the subfolder Embeds before the notes")
        }

        card(name = "Read mode").clickWhenStill()
        expectShown(text = "Some **bold**")
        clickInTree(text = "attachments")

        listOf("Arepas plate.png", "Market photo.jpg", "broken.png").forEach { card(name = it) }
    }
}

private fun SuiteBuilder.nativeTitleBar() {
    manual(
        id = "1.11-12",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)
        title(text = "La parte de arriba de la ventana, con los botones del sistema")

        steps(
            text =
            "$OPEN_PLAYGROUND\n2. Abre algunas notas y mira la parte de arriba de la ventana.\n3. " +
                "Oculta la barra lateral (Ctrl+\\, Cmd+\\ en macOS) y vuelve a mirar.\n4. En macOS, " +
                "arrastra la ventana desde la fila de las pestañas y haz doble clic en un espacio vacío " +
                "de esa fila."
        )

        expected(
            text =
            "Los botones de cerrar, minimizar y maximizar son los de tu sistema. En Windows y Linux " +
                "las pestañas están debajo de la barra de título del sistema. En macOS la barra " +
                "lateral sube hasta arriba con los botones sobre su color y las pestañas ocupan la " +
                "fila del título; con la barra lateral oculta, las pestañas empiezan después de los " +
                "botones. Arrastrar la fila mueve la ventana y el doble clic la maximiza."
        )
    }
}

private fun SuiteBuilder.windowSize() {
    manual(
        id = "1.11-13",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)
        title(text = "La ventana mantiene un tamaño razonable")

        steps(
            text =
            "$OPEN_PLAYGROUND\n2. Sal de Notekea y ábrelo de nuevo.\n3. Haz clic en Abrir " +
                "otra bóveda abajo a la izquierda y mira la ventana; luego abre la Playground " +
                "Vault desde Recientes. Hazlo dos veces más.\n4. Cambia el tamaño de la ventana, " +
                "sal y abre Notekea una vez más."
        )

        expected(
            text =
            "La ventana se abre centrada, nunca más grande que la pantalla, mostrando las " +
                "herramientas, la lista de notas, una nota y el índice. Mantiene su tamaño " +
                "cuando una bóveda se abre o se cierra, y el tamaño que le diste se queda."
        )
    }
}

private fun SuiteBuilder.quickFolderClicks() {
    automated(
        id = "1.11-14",
        title = "Opening and closing folders quickly never shows an error"
    ) {
        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        clickIcon(label = "Collapse all folders")

        repeat(times = QUICK_CLICKS) {
            val isEven = it % 2 == 0

            clickInTree(text = if (isEven) "Recipes" else "Travel")
        }

        expectTextStaysAway(
            text = "Something went wrong",
            substring = true
        )
    }
}

private fun SuiteBuilder.reorderTabs() {
    automated(
        id = "1.11-15",
        title = "Tabs are reordered by dragging them along the row"
    ) {
        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        expandInTree(name = "Recipes")
        clickInTree(text = WELCOME)
        clickInTree(text = "Arepas")
        clickInTree(text = "Ideas")
        expectTabOrder(names = listOf(WELCOME, "Arepas", "Ideas"))

        dragTab(
            name = WELCOME,
            by = Offset(
                x = tabLeft(name = "Ideas") - DROP_MARGIN - tabCenterX(name = WELCOME),
                y = 0f
            )
        )

        expectTabOrder(names = listOf("Arepas", WELCOME, "Ideas"))

        dragTab(
            name = "Ideas",
            by = Offset(
                x = tabLeft(name = WELCOME) - DROP_MARGIN - tabCenterX(name = "Ideas"),
                y = 0f
            )
        )

        expectTabOrder(names = listOf("Arepas", "Ideas", WELCOME))
        // Dropping a tab in the first place does not register with synthetic mouse input, though it does with a
        // person's mouse: 1.11-16 walks the tester through it.
    }
}

private fun SuiteBuilder.tabMovedWhileTyping() {
    automated(
        id = "1.11-17",
        title = "A tab moved right after typing keeps the last keystrokes"
    ) {
        openTwoNotes()

        typeAtEnd(
            part = AREPAS_FLOUR,
            text = TYPED_BEFORE_MOVE
        )

        pressInWindow(shortcut = Mod + Shift + Key.D)

        expectText(
            text = TYPED_BEFORE_MOVE,
            substring = true
        )

        expectShown(text = WELCOME_TEXT)

        expectNoText(
            text = "This note changed outside Notekea",
            substring = true
        )
    }
}

private fun SuiteBuilder.tabSlides() {
    manual(
        id = "1.11-16",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "Una pestaña arrastrada se desliza por la fila")

        steps(
            text =
            "$OPEN_PLAYGROUND\n2. Abre tres notas y arrastra despacio la última pestaña por " +
                "la fila hasta soltarla en el primer lugar."
        )

        expected(
            text =
            "Mientras la arrastras, la pestaña se queda en la línea de la fila y las demás " +
                "se apartan para abrirle lugar; al soltarla, se desliza al primer lugar y se " +
                "queda ahí."
        )
    }
}

private fun CaseScope.openTwoNotes() {
    openVault(
        name = PLAYGROUND,
        shownText = WELCOME
    )

    expandInTree(name = "Recipes")
    clickInTree(text = WELCOME)
    clickInTree(text = "Arepas")
}

/** The card of a folder's grid showing [name]: of the elements showing it, the one furthest right. */
private fun CaseScope.card(name: String): SemanticsNodeInteraction {
    expectText(text = name)
    val nodes = ui.onAllNodes(matcher = hasText(text = name))
    val count = nodes.fetchSemanticsNodes().size

    return (0 until count)
        .map {
            nodes[it]
        }
        .filter { it.isShown() }
        .maxBy {
            it
                .fetchSemanticsNode()
                .boundsInRoot
                .left
        }
}

private fun SemanticsNodeInteraction.top(): Float = fetchSemanticsNode().boundsInRoot.top

private fun CaseScope.expectOnScreen(text: String) {
    ui
        .waitUntil(timeoutMillis = 10_000) {
            val nodes = ui.onAllNodes(
                matcher = hasText(
                    text = text,
                    substring = true
                )
            )

            val count = nodes.fetchSemanticsNodes().size

            (0 until count).any { nodes[it].isShown() }
        }
}

private fun CaseScope.clickInOutline(heading: String) {
    val row = inOutline(heading = heading)

    ui
        .waitUntil(timeoutMillis = 10_000) {
            ui
                .onAllNodes(matcher = row)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

    ui.onAllNodes(matcher = row)[0].clickWhenStill()
}

private const val DROP_MARGIN = 8f
private const val QUICK_CLICKS = 20
