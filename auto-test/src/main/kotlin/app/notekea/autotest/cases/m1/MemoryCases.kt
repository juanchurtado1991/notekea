package app.notekea.autotest.cases.m1

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.hasContentDescription
import app.notekea.autotest.clickInTree
import app.notekea.autotest.cursorToNoteStart
import app.notekea.autotest.expectOrderOnScreen
import app.notekea.autotest.openNote
import app.notekea.autotest.pressInWindow
import app.notekea.autotest.typeAtEnd
import app.notekea.autotest.waitForResult
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ManualReason
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.SuiteBuilder

private const val PLATE = "Arepas plate.png"
private const val IMAGES = "Images and files"
private val JOURNAL_DAYS = listOf("2026-10-20", "2026-10-21", "2026-10-22")

/** The cases of 1.18, what the editor lets go of and reads again (`MVP_SMOKE_TEST.md`). */
fun SuiteBuilder.memoryCases() {
    savedOnSwitchingVault()
    sharpPictures()
    picturesComeBack()
    namesInOrder()
}

private fun SuiteBuilder.savedOnSwitchingVault() {
    automated(
        id = "1.18-01",
        title = "A word typed just before switching vaults is saved"
    ) {
        openPlaygroundNote(
            name = "Arepas",
            shownText = AREPAS_FLOUR
        )

        typeAtEnd(
            part = AREPAS_FLOUR,
            text = "\nLast word."
        )

        clickIcon(label = "Open another vault")
        expectText(text = "Open folder…")
        click(text = PLAYGROUND)
        expectText(text = WELCOME)

        openNote(
            name = "Arepas",
            shownText = "Last word."
        )

        expectFile(
            path = "$PLAYGROUND/Recipes/Arepas.md",
            containing = "Last word."
        )
    }
}

private fun SuiteBuilder.sharpPictures() {
    manual(
        id = "1.18-02",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "Las tarjetas de una carpeta y una imagen abierta se ven nítidas")

        steps(
            text =
            "1. En Notekea, abre la Playground Vault (mira DATOS DE PRUEBA arriba).\n2. Haz " +
                "clic en la carpeta de adjuntos en la lista de la izquierda y mira la tarjeta de " +
                "Market photo.jpg.\n3. Haz clic en esa tarjeta."
        )

        expected(
            text =
            "La tarjeta muestra la foto recortada a la tarjeta y nítida: arriba se lee " +
                "\"Market photo 4000 x 3000\". La foto se abre en una pestaña y se ve entera y " +
                "nítida."
        )
    }
}

private fun SuiteBuilder.picturesComeBack() {
    automated(
        id = "1.18-03",
        title = "A note's pictures show again when its tab comes back"
    ) {
        openPlaygroundNote(
            name = IMAGES,
            shownText = "A picture at its own size"
        )

        cursorToNoteStart()
        expectPictures(atLeast = 2)

        openNote(
            name = "Arepas",
            shownText = AREPAS_FLOUR
        )

        tab(name = IMAGES).clickWhenStill()
        expectPictures(atLeast = 2)
    }
}

private fun SuiteBuilder.namesInOrder() {
    automated(
        id = "1.18-04",
        title = "Names with numbers are listed in order, in the quick switcher and the tree"
    ) {
        openPlaygroundNote(
            name = WELCOME,
            shownText = "This is a sample vault"
        )

        pressInWindow(shortcut = Mod + Key.P)
        type(text = "2026-10")

        JOURNAL_DAYS.forEach { waitForResult(name = it) }

        expectOrderOnScreen(texts = JOURNAL_DAYS)
        press(key = Key.Escape)
        clickInTree(text = "Journal")
        expectOrderOnScreen(texts = JOURNAL_DAYS)
    }
}

private fun CaseScope.expectPictures(atLeast: Int) {
    val holds = runCatching {
        ui
            .waitUntil(timeoutMillis = 10_000) {
                ui
                    .onAllNodes(matcher = hasContentDescription(value = PLATE))
                    .fetchSemanticsNodes()
                    .size >= atLeast
            }
    }.isSuccess

    if (!holds) throw CaseFailure(message = "expected the pictures of $IMAGES to show")
}
