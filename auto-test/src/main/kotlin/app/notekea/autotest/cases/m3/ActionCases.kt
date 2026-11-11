package app.notekea.autotest.cases.m3

import androidx.compose.ui.input.key.Key
import app.notekea.autotest.ChatModels
import app.notekea.autotest.cases.m1.PLAYGROUND
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val TASKS = "what tasks are open?"
private const val WEEK_TASKS = "¿qué tareas hay esta semana?"
private const val CALL = "create a note Call with Ana in Projects, status draft"
private const val CALL_NOTE = "$PLAYGROUND/Projects/Call with Ana.md"
private const val DESCRIBE = "Describe what you want, like books I rated 4 or more…"
private const val MORE_THAN = "more than"
private const val RATING_EN = "notes with a rating of 4 or more"
private const val RATING_ES = "notas con calificación de más de 3"

/**
 * The cases of 3.6, 3.8 and 3.9 (`MVP_SMOKE_TEST.md`): Deep answer, plain-word queries and actions, with a chat model
 * on the machine ([ChatModels]); the actions that change notes are in `ActionChangeCases.kt`.
 */
fun SuiteBuilder.actionCases() {
    deepAnswer()
    describeQuery()
    chatLists()
    noteFromTemplate()
    summaryOfNotes()
    notesFromText()
    actionChangeCases()
}

private fun SuiteBuilder.deepAnswer() {
    automated(
        id = "3.6-01",
        title = "Deep answer writes drafts and shows one answer, with Medium",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.medium
    ) {
        openChat()
        expectText(text = "· slower")
        clickIcon(label = "Deep answer")
        send(question = WATER)
        expectText(text = "Writing drafts and comparing them…")
        awaitReply()

        val chat = copiedChat()
        expectInChat(chat, "Deep answer: true")

        expectInChat(
            replyTo(
                chat = chat,
                question = WATER
            ),
            *TWO_AND_A_HALF
        )

        expectInChat(
            replyTo(
                chat = chat,
                question = WATER
            ),
            "] Recipes/Arepas"
        )

        send(question = REST)
        awaitReplyStart()
        press(key = Key.Escape)
        expectText(text = "Stopped")
    }
}

private fun SuiteBuilder.describeQuery() {
    automated(
        id = "3.8-01",
        title = "The Query tab fills its rules from plain words, in English or Spanish",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.any
    ) {
        openChat(withArepas = false)
        clickIcon(label = "Query")

        listOf(RATING_EN, RATING_ES).forEach { request ->
            clickIcon(label = DESCRIBE)
            clearText()
            type(text = request)
            press(key = Key.Enter)
            awaitReply(running = "Filling the rules…")
            expectText(text = MORE_THAN)
            expectText(text = "3")
            expectText(text = "Properties practice")
            clickIcon(label = "Remove filter")
            expectNoText(text = MORE_THAN)
        }

        clickIcon(label = DESCRIBE)
        clearText()
        type(text = "notes by stars")
        press(key = Key.Enter)
        awaitReply(running = "Filling the rules…")

        expectText(
            text = "No note has a property “stars”",
            substring = true
        )
    }
}

private fun SuiteBuilder.chatLists() {
    automated(
        id = "3.8-02",
        title = "The chat lists open tasks, and says when none match",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.any
    ) {
        openChat(withArepas = false)
        ask(question = TASKS)

        val tasks = replyTo(
            chat = copiedChat(),
            question = TASKS
        )

        expectInChat(tasks, "Packing checklist")
        expectInChat(tasks, "Reading list")

        expectText(
            text = " tasks",
            substring = true
        )

        click(text = "Packing checklist")

        expectText(
            text = "Rain jacket",
            substring = true
        )

        ask(question = WEEK_TASKS)
        expectText(text = "Ninguna tarea pendiente coincide.")
    }
}

private fun SuiteBuilder.noteFromTemplate() {
    automated(
        id = "3.9-01",
        title = "A note is proposed, cancelled, done and undone",
        timeout = MODEL_CASE_TIMEOUT,
        precondition = ChatModels.any
    ) {
        openChat(withArepas = false)
        ask(question = CALL)
        expectText(text = "Notekea will:")
        expectText(text = "Create note")
        expectText(text = "Set property")
        click(text = "Cancel")
        expectText(text = "Cancelled. Nothing was changed.")
        expectNoFile(path = CALL_NOTE)
        ask(question = CALL)
        click(text = "Do it")

        expectFile(
            path = CALL_NOTE,
            containing = "status: draft"
        )

        click(text = "Undo")
        expectNoFile(path = CALL_NOTE)
    }
}

private fun SuiteBuilder.summaryOfNotes() {
    manual(
        id = "3.9-02",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "A summary of several notes is useful and sized to them")
        steps(text = "1. In Notekea, open the Playground Vault (see TEST DATA above) with a chat model in use and press Ctrl+L (Cmd+L on macOS).\n2. Ask write a summary of my Travel notes in a new note called Travel summary, read the card and click Do it.\n3. Read the new note, then click Undo.")
        expected(text = "The card names Lisbon trip and Packing checklist and shows the start of the summary. The note has headings, lists and links to both, says nothing they do not say, and reads well. Undo removes it.")
    }
}

private fun SuiteBuilder.notesFromText() {
    manual(
        id = "3.9-03",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "A pasted text becomes a folder of linked notes")
        steps(text = "1. In Notekea, open the Playground Vault (see TEST DATA above) with a chat model in use and press Ctrl+L (Cmd+L on macOS).\n2. Type Créame notas a partir de este texto: and paste five paragraphs of any article in Spanish, send it and click Do it.\n3. Open the folder it made and read its notes, then click Undo.")
        expected(text = "The card lists a folder with an index note and one note per topic. Each note keeps the facts of its part, formatted with headings, bold and lists, in Spanish, and the index links them all. Undo removes the folder's notes.")
    }
}
