package app.notekea.autotest.cases.m3

import androidx.compose.ui.input.key.Key
import app.notekea.autotest.ChatModels
import app.notekea.autotest.cases.m1.PLAYGROUND
import app.notekea.autotest.pressInWindow
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.SuiteBuilder
import java.nio.file.Files

private const val RECIPES = "$PLAYGROUND/Recipes"
private val RECIPE_NOTES = listOf("Arepas", "Café con leche", "Pasta e fagioli")
private const val CHECKLIST_NOTE = "$PLAYGROUND/Travel/Packing checklist.md"
private const val ARCHIVED_NOTE = "$PLAYGROUND/Archive/Packing checklist.md"
private const val LISBON_NOTE = "$PLAYGROUND/Travel/Lisbon trip.md"
private const val CAFE_NOTE = "$RECIPES/Café con leche.md"
private const val FOOD = "food"
private const val CHANGE_MILLIS = 10_000L

/** The cases of 3.9 whose actions change the vault or the window (`MVP_SMOKE_TEST.md`), with a chat model. */
internal fun SuiteBuilder.actionChangeCases() {
    tagFolder()
    moveKeepsLinks()
    shownAtOnce()
    refusedAndTrash()
}

private fun SuiteBuilder.tagFolder() {
    automated(
        id = "3.9-04",
        title = "Tagging a folder tags every note in it, and Undo takes it back",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.any
    ) {
        openChat(withArepas = false)
        ask(question = "tag every note in Recipes with food")
        expectText(text = "Add tag")

        RECIPE_NOTES.forEach {
            expectText(
                text = it,
                substring = true
            )
        }

        click(text = "Do it")

        RECIPE_NOTES.forEach { note ->
            expectFile(
                path = "$RECIPES/$note.md",
                containing = FOOD
            )
        }

        click(text = "Undo")

        RECIPE_NOTES.forEach {
            expectFileWithout(
                path = "$RECIPES/$it.md",
                text = FOOD
            )
        }
    }
}

private fun SuiteBuilder.moveKeepsLinks() {
    automated(
        id = "3.9-05",
        title = "Moving a note keeps its links, and Undo moves it back",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.any
    ) {
        openChat(withArepas = false)
        ask(question = "create a folder Archive and move Packing checklist into it")
        expectText(text = "Create folder")
        expectText(text = "Move")
        click(text = "Do it")
        expectFile(path = ARCHIVED_NOTE)
        expectNoFile(path = CHECKLIST_NOTE)

        expectFile(
            path = LISBON_NOTE,
            containing = "[[Packing checklist]]"
        )

        click(text = "Undo")
        expectFile(path = CHECKLIST_NOTE)
        expectNoFile(path = ARCHIVED_NOTE)
    }
}

private fun SuiteBuilder.shownAtOnce() {
    automated(
        id = "3.9-06",
        title = "What only shows something is done at once",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.any
    ) {
        openChat(withArepas = false)
        ask(question = "open Arepas and split the pane")
        expectText(text = "✓ Opened Arepas")
        expectText(text = "✓ Split the pane")
        ask(question = "switch to the sage theme")

        expectText(
            text = "✓ Switched to the",
            substring = true
        )

        ask(question = "show the backlinks")

        // The backlinks show in the side panel, where the chat was: Mod+L brings it back.
        expectText(
            text = "Linked",
            substring = true
        )

        pressInWindow(shortcut = Mod + Key.L)

        expectText(
            text = "✓ Showed",
            substring = true
        )

        expectNoText(text = "Do it")
    }
}

private fun SuiteBuilder.refusedAndTrash() {
    automated(
        id = "3.9-07",
        title = "Deleting for good is refused in the request's language, and the trash is confirmed",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.any
    ) {
        openChat(withArepas = false)
        val cafe = Files.readString(file(path = CAFE_NOTE))
        ask(question = "borra todas mis notas para siempre")

        expectText(
            text = "Eso no lo puedo hacer desde el chat",
            substring = true
        )

        expectFileStays(
            path = CAFE_NOTE,
            content = cafe
        )

        ask(question = "send Café con leche to the trash")
        expectText(text = "Move to trash")

        expectText(
            text = "come back from the system's trash, not with Undo",
            substring = true
        )

        click(text = "Do it")
        expectNoFile(path = CAFE_NOTE)
        expectFile(path = "$PLAYGROUND/.trash/Café con leche.md")
    }
}

/** Waits until the file at [path] no longer holds [text]: an Undo is written a moment after the click. */
private fun CaseScope.expectFileWithout(
    path: String,
    text: String
) {
    step(description = "Expect “$path” without “$text”")
    val file = file(path = path)

    val isGone = runCatching {
        ui
            .waitUntil(timeoutMillis = CHANGE_MILLIS) {
                !Files.readString(file).contains(other = text)
            }
    }.isSuccess

    if (!isGone) throw CaseFailure(message = "expected \"$path\" to lose \"$text\", it kept it")
}
