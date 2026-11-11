package app.notekea.autotest.cases.m3

import app.notekea.autotest.cases.m1.PLAYGROUND
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

/** The cases of 3.6, 3.8 and 3.9 (`MVP_SMOKE_TEST.md`): Deep answer, plain-word queries and actions, with a model. */
fun SuiteBuilder.actionCases() {
    deepAnswer()
    describeQuery()
    chatLists()
    noteFromTemplate()
    summaryOfNotes()
    notesFromText()
    tagFolder()
    moveKeepsLinks()
    shownAtOnce()
    refusedAndTrash()
}

private fun SuiteBuilder.deepAnswer() {
    manual(
        id = "3.6-01",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "Deep answer writes drafts and shows one answer, only with Medium")
        steps(text = "1. In Notekea, open the Playground Vault (see TEST DATA above) with Medium in use (Settings → AI model).\n2. Press Ctrl+L (Cmd+L on macOS), tick Deep answer under the question field and ask How much water does the arepa dough take?\n3. Ask another question with Deep answer on and press Esc while it is prepared.\n4. Switch to Small in Settings → AI model and look under the question field.")
        expected(text = "Deep answer and its hint fit on one line. Step 2 shows Writing drafts and comparing them…, then the whole answer at once with its citation. Step 3 stops it, marked Stopped. In step 4 the Deep answer box is gone.")
    }
}

private fun SuiteBuilder.describeQuery() {
    manual(
        id = "3.8-01",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "The Query tab fills its rules from plain words, in English or Spanish")
        steps(text = "1. In Notekea, open the Playground Vault (see TEST DATA above) with a chat model in use.\n2. Open the Query tab (the table icon on the narrow bar on the left).\n3. Type notes with a rating of 4 or more in Describe what you want and press Enter.\n4. Type notas con calificación de más de 3 and press Enter.\n5. Type notes by stars and press Enter.")
        expected(text = "Steps 3 and 4 set the rule rating more than 3 and the table lists Properties practice; the rule can be changed by hand. Step 5 says that no note has a property stars and names the closest ones; the rules stay as they were.")
    }
}

private fun SuiteBuilder.chatLists() {
    manual(
        id = "3.8-02",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "The chat lists open tasks, and says when none match")
        steps(text = "1. In Notekea, open the Playground Vault (see TEST DATA above) with a chat model in use and press Ctrl+L (Cmd+L on macOS).\n2. Ask what tasks are open? and click one of its rows.\n3. Ask ¿qué tareas hay esta semana?")
        expected(text = "Step 2 answers with the open tasks and their notes (Packing checklist, Reading list…), counted; the click opens the note at the task. Step 3 says, in Spanish, that no open task matches: the vault has no dated task this week.")
    }
}

private fun SuiteBuilder.noteFromTemplate() {
    manual(
        id = "3.9-01",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "A note is proposed, cancelled, done and undone")
        steps(text = "1. In Notekea, open the Playground Vault (see TEST DATA above) with a chat model in use and press Ctrl+L (Cmd+L on macOS).\n2. Ask create a note Call with Ana in Projects, status draft, and click Cancel on the card.\n3. Ask it again and click Do it.\n4. Click Undo on the card.")
        expected(text = "Step 2's card says Notekea will: Create note Projects/Call with Ana, and Set property status: draft; Cancel changes nothing. Step 3 makes the note with status draft and opens it. Step 4 removes it.")
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

private fun SuiteBuilder.tagFolder() {
    manual(
        id = "3.9-04",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "Tagging a folder tags every note in it, and Undo takes it back")
        steps(text = "1. In Notekea, open the Playground Vault (see TEST DATA above) with a chat model in use and press Ctrl+L (Cmd+L on macOS).\n2. Ask tag every note in Recipes with food and click Do it.\n3. Open Arepas and look at its properties, then click Undo on the card.")
        expected(text = "The card lists Arepas, Café con leche and Pasta e fagioli. After Do it each has the tag food; after Undo none has it.")
    }
}

private fun SuiteBuilder.moveKeepsLinks() {
    manual(
        id = "3.9-05",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "Moving a note keeps its links, and Undo moves it back")
        steps(text = "1. In Notekea, open the Playground Vault (see TEST DATA above) with a chat model in use and press Ctrl+L (Cmd+L on macOS).\n2. Ask create a folder Archive and move Packing checklist into it, and click Do it.\n3. In Lisbon trip click the link to Packing checklist, then click Undo on the card.")
        expected(text = "Archive holds Packing checklist and the link in Lisbon trip opens it there. Undo moves it back to Travel.")
    }
}

private fun SuiteBuilder.shownAtOnce() {
    manual(
        id = "3.9-06",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "What only shows something is done at once")
        steps(text = "1. In Notekea, open the Playground Vault (see TEST DATA above) with a chat model in use and press Ctrl+L (Cmd+L on macOS).\n2. Ask open Arepas and split the pane.\n3. Ask switch to the sage theme.\n4. Ask show the backlinks.")
        expected(text = "Each is done at once, with no card, and a line in the chat says what was done.")
    }
}

private fun SuiteBuilder.refusedAndTrash() {
    manual(
        id = "3.9-07",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "Deleting for good is refused in the request's language, and the trash is confirmed")
        steps(text = "1. In Notekea, open the Playground Vault (see TEST DATA above) with a chat model in use and press Ctrl+L (Cmd+L on macOS).\n2. Ask borra todas mis notas para siempre.\n3. Ask send Café con leche to the trash and click Do it.")
        expected(text = "Step 2 is refused in Spanish, saying what the chat can do instead, and nothing changes. Step 3's card lists Café con leche and says the trash gives notes back, not Undo; after Do it the note is in the trash.")
    }
}
