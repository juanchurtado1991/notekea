package app.notekea.autotest.cases.m3

import androidx.compose.ui.input.key.Key
import app.notekea.autotest.ChatModels
import app.notekea.autotest.cases.m1.AREPAS_FLOUR
import app.notekea.autotest.cases.m1.PLAYGROUND
import app.notekea.autotest.cases.m1.openPlaygroundNote
import app.notekea.autotest.openNote
import app.notekea.autotest.pressInWindow
import app.notekea.autotest.relaunch
import com.testbuddy.ManualReason
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.ShortcutModifier.Shift
import com.testbuddy.SuiteBuilder

private const val NOTE_INFO = "NOTE INFO"

/**
 * The cases of 3.3, the chat panel (`MVP_SMOKE_TEST.md`): those that ask the model run where a chat model is on the
 * machine ([ChatModels]) and are skipped elsewhere (CI); the rest are in `ChatReplyCases.kt`.
 */
fun SuiteBuilder.chatCases() {
    chatShortcut()
    answerWithCitations()
    scopes()
    stopAndHistory()
    noModel()
    chatReplyCases()
}

private fun SuiteBuilder.chatShortcut() {
    automated(
        id = "3.3-01",
        title = "Mod+L shows the chat from the note, and Mod+Shift+O goes back to the inspector"
    ) {
        openPlaygroundNote(
            name = "Arepas",
            shownText = AREPAS_FLOUR
        )

        expectText(text = NOTE_INFO)
        pressInWindow(shortcut = Mod + Key.L)
        expectNoText(text = NOTE_INFO)
        pressInWindow(shortcut = Mod + Shift + Key.O)
        expectText(text = NOTE_INFO)
    }
}

private fun SuiteBuilder.answerWithCitations() {
    automated(
        id = "3.3-02",
        title = "A question is answered from the notes as it is written, with citations that open the passage",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.any
    ) {
        openChat(withArepas = false)
        ask(question = WATER)

        val reply = replyTo(
            chat = copiedChat(),
            question = WATER
        )

        expectInChat(reply, "Outcome: answered")
        expectInChat(reply, *TWO_AND_A_HALF)
        expectInChat(reply, "] Recipes/Arepas")
        click(text = "Arepas")

        expectText(
            text = AREPAS_FLOUR,
            substring = true
        )
    }
}

private fun SuiteBuilder.scopes() {
    automated(
        id = "3.3-03",
        title = "This note and This folder ask only of the open note or its folder",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.any
    ) {
        openChat()
        click(text = "Whole vault")
        click(text = "This note (Arepas)")
        ask(question = CHECKLIST)
        expectText(text = NOT_FOUND)
        click(text = "This note (Arepas)")
        click(text = "This folder (Recipes)")
        ask(question = REST)
        val chat = copiedChat()

        expectInChat(
            replyTo(
                chat = chat,
                question = CHECKLIST
            ),
            "Outcome: not_found"
        )

        val rest = replyTo(
            chat = chat,
            question = REST
        )

        expectInChat(rest, "Scope: folder (Recipes)")
        expectInChat(rest, *FIVE_MINUTES)

        expectOnlyFrom(
            reply = rest,
            folder = "Recipes/"
        )

        openNote(
            name = "Lisbon trip",
            shownText = "Alfama walk"
        )

        expectText(text = "This folder (Travel)")
    }
}

private fun SuiteBuilder.stopAndHistory() {
    automated(
        id = "3.3-04",
        title = "Esc stops an answer, the history comes back after a relaunch, and Clear history empties it",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.any
    ) {
        openChat()
        ask(question = WATER)
        send(question = SUMMARY)
        awaitReplyStart()
        press(key = Key.Escape)
        expectText(text = "Stopped")
        awaitReply()
        relaunch()
        pressInWindow(shortcut = Mod + Key.L)
        expectText(text = WATER)
        expectText(text = SUMMARY)
        chatMenu()
        click(text = "Clear history")
        expectText(text = "Ask your notes")
        expectNoText(text = WATER)
    }
}

private fun SuiteBuilder.noModel() {
    manual(
        id = "3.3-06",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)
        title(text = "Without a model the chat says so and leads to Settings, where the download shows in the status bar")

        steps(
            text =
            "1. In Notekea, open the Playground Vault (see TEST DATA above). In Settings → AI model, delete the " +
                "model in use if there is one.\n2. Press Ctrl+L (Cmd+L on macOS).\n3. Click Choose a model, click " +
                "Download on the recommended size and press Esc.\n4. Click the AI model entry of the status bar, " +
                "then Cancel the download."
        )

        expected(
            text =
            "Step 2 shows No AI model yet, why, and Choose a model, with no question field. Step 3 opens Settings " +
                "at AI model; after Esc the status bar shows AI model · with the percent and a ring that fills. " +
                "Step 4 opens Settings at AI model again, and the entry leaves the status bar once cancelled."
        )
    }
}
