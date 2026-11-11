package app.notekea.autotest.cases.m3

import androidx.compose.ui.input.key.Key
import app.notekea.autotest.cases.m1.AREPAS_FLOUR
import app.notekea.autotest.cases.m1.PLAYGROUND
import app.notekea.autotest.cases.m1.openPlaygroundNote
import app.notekea.autotest.pressInWindow
import com.testbuddy.ManualReason
import com.testbuddy.ShortcutModifier.Mod
import com.testbuddy.ShortcutModifier.Shift
import com.testbuddy.SuiteBuilder

private const val NOTE_INFO = "NOTE INFO"

private const val OPEN_CHAT =
    "1. In Notekea, open the Playground Vault (see TEST DATA above); a chat model must be in use (Settings → AI " +
        "model; download Medium first if none says In use).\n2. Open Recipes/Arepas and press Ctrl+L (Cmd+L on " +
        "macOS): the side panel shows the chat."

/** The cases of 3.3, the chat panel (`MVP_SMOKE_TEST.md`); those that need a downloaded model are checked by eye. */
fun SuiteBuilder.chatCases() {
    chatShortcut()
    answerWithCitations()
    scopes()
    stopAndHistory()
    notFound()
    noModel()
    talkAndFollowUps()
    copyChat()
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
    manual(
        id = "3.3-02",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "A question is answered from the notes as it is written, with citations that open the passage")

        steps(
            text =
            "$OPEN_CHAT\n3. Type How much water does the arepa dough take? and press Enter.\n4. Point at a " +
                "number chip in the answer, then click it.\n5. Click the second source under the answer, if " +
                "there is one."
        )

        expected(
            text =
            "Step 3 shows the question on the right, then one line with moving dots (Searching your notes…, " +
                "Reading 3 notes…), then the answer word by word with a blinking bar, in English, citing [1]-like " +
                "chips; Sources lists the cited notes with their headings. Step 4 shows the cited passage in a " +
                "tooltip, and the click opens Arepas with that passage selected. Step 5 does the same for its own " +
                "passage."
        )
    }
}

private fun SuiteBuilder.scopes() {
    manual(
        id = "3.3-03",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "This note and This folder ask only of the open note or its folder")

        steps(
            text =
            "$OPEN_CHAT\n3. Click Whole vault above the messages and choose This note (Arepas).\n4. Ask " +
                "What is in the packing checklist?\n5. Choose This folder (Recipes) and ask How long does the dough " +
                "rest?\n6. Open Travel/Lisbon trip and look at the scope."
        )

        expected(
            text =
            "Step 4 answers I couldn't find this in your notes, since the checklist is not in Arepas. Step 5 answers " +
                "from a recipe and cites only notes of Recipes. In step 6 the scope follows the open note: This " +
                "folder (Travel)."
        )
    }
}

private fun SuiteBuilder.stopAndHistory() {
    manual(
        id = "3.3-04",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "Esc stops an answer, the history comes back after a relaunch, and Clear history empties it")

        steps(
            text =
            "$OPEN_CHAT\n3. Ask Summarize my Lisbon trip and, while the answer is being " +
                "written, press Esc.\n4. Quit Notekea, open it again and show the chat (Ctrl+L, Cmd+L on macOS).\n" +
                "5. Click ⋯ at the top right of the chat, then Clear history."
        )

        expected(
            text =
            "Step 3 stops the answer where it was, marked Stopped, and the Send button comes back. Step 4 shows " +
                "the earlier questions and answers with their sources. Step 5 leaves the chat empty, with Ask your " +
                "notes."
        )
    }
}

private fun SuiteBuilder.notFound() {
    manual(
        id = "3.3-05",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "A question the notes do not answer is declined, in the question's language")

        steps(
            text =
            "$OPEN_CHAT\n3. Ask When is my dentist appointment?\n4. Ask ¿Cuánta agua lleva la masa de las " +
                "arepas?"
        )

        expected(
            text =
            "Step 3 answers I couldn't find this in your notes, with no sources and nothing made up. Step 4 " +
                "answers in Spanish (two and a half cups, 2½) with a citation of Arepas."
        )
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

private fun SuiteBuilder.talkAndFollowUps() {
    manual(
        id = "3.3-07",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "The chat talks, follows the conversation and looks further")

        steps(
            text =
            "$OPEN_CHAT\n3. Type hola.\n4. Ask How do I make arepas?, then and how long does it rest?\n5. Open " +
                "Travel/Lisbon trip and ask ¿qué opinas de este viaje?\n6. Ask what trips do I have?"
        )

        expected(
            text =
            "Step 3 gets a short greeting in Spanish, not a search. Step 4's follow-up answers five minutes: it " +
                "knows what \"it\" is. Step 5 gives an opinion in Spanish, said to be the assistant's view, with " +
                "Lisbon trip as its source. Step 6 answers from Lisbon trip instead of saying no notes match."
        )
    }
}

private fun SuiteBuilder.copyChat() {
    manual(
        id = "3.3-08",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)
        title(text = "Copy chat puts the whole conversation on the clipboard")

        steps(
            text =
            "$OPEN_CHAT\n3. Ask two questions.\n4. Click ⋯ at the chat's top right, then Copy chat.\n5. Paste " +
                "into a text editor."
        )

        expected(
            text =
            "The text holds each question, how it ended, the answer, and the passages it read with their note " +
                "and heading."
        )
    }
}
