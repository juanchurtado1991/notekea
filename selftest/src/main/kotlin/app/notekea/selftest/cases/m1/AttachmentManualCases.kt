package app.notekea.selftest.cases.m1

import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val OPEN_IMAGES =
    "1. In Notekea, open the Playground Vault from the Test data folder (Open folder…, after Open another vault at " +
        "the bottom left if a vault is open).\n2. Open the note Editor › Images and files."

/** The cases of 1.8 that need the system (its file dialogs, file manager, clipboard, PDF viewer) or a person's eyes. */
internal fun SuiteBuilder.attachmentManualCases() {
    pictureSizes()
    pdfOpens()
    locateMissing()
    attachFromMenu()
    dropFiles()
    pasteScreenshot()
    obsidianAttachmentFolder()
    sameInObsidian()
    pictureInTable()
    waylandClipboard()
    dropNote()
}

private fun SuiteBuilder.pictureSizes() {
    manual(id = "1.8-01", reason = ManualReason.EYES) {
        prepare(PLAYGROUND, "Outside files", PERSONAL_NOTES)
        title(text = "Pictures at the right size, files as chips")
        steps(text = "$OPEN_IMAGES\n3. Click Preview and scroll through the note.")
        expected(
            text = "The orange and blue plate shows three times: at its own size (480 wide, or the note's width if " +
                "narrower), 200 wide, and once more from the Markdown link. Weekly menu.pdf and broken.png show as " +
                "file chips with a paperclip and \"Opens with its app\". Lost photo.png says \"This file is " +
                "missing\" with its name and Locate…. In the last paragraph \"the menu\" is a link."
        )
    }
}

private fun SuiteBuilder.pdfOpens() {
    manual(id = "1.8-03", reason = ManualReason.SYSTEM) {
        prepare(PLAYGROUND, "Outside files", PERSONAL_NOTES)
        title(text = "A PDF opens in the system's PDF viewer")
        steps(
            text = "$OPEN_IMAGES\n3. Click the Weekly menu.pdf chip.\n4. Close the PDF viewer, click Preview and " +
                "click \"the menu\" in the last paragraph."
        )
        expected(text = "Both times the PDF \"Weekly menu: arepas\" opens in your system's PDF viewer.")
    }
}

private fun SuiteBuilder.locateMissing() {
    manual(id = "1.8-04", reason = ManualReason.SYSTEM) {
        prepare(PLAYGROUND, "Outside files", PERSONAL_NOTES)
        title(text = "Locating a missing picture")
        steps(
            text = "$OPEN_IMAGES\n3. Click Locate… under Lost photo.png.\n4. In the window that opens, choose Found " +
                "plate.png in the Outside files folder of the Test data folder."
        )
        expected(
            text = "The line becomes ![[Found plate.png]] and a green plate shows under it. Found plate.png is now " +
                "in the vault's attachments folder."
        )
    }
}

private fun SuiteBuilder.attachFromMenu() {
    manual(id = "1.8-05", reason = ManualReason.SYSTEM) {
        prepare(PLAYGROUND, "Outside files", PERSONAL_NOTES)
        title(text = "Adding a file from the toolbar")
        steps(
            text = "$OPEN_IMAGES\n3. Click at the end of the note, open More (⋯) in the toolbar and click Image or " +
                "file.\n4. Choose Shopping list.pdf in the Outside files folder of the Test data folder."
        )
        expected(
            text = "![[Shopping list.pdf]] is written on a line of its own with its chip under it, and the cursor is " +
                "on the next line. The PDF is copied into the vault's attachments folder."
        )
    }
}

private fun SuiteBuilder.dropFiles() {
    manual(id = "1.8-06", reason = ManualReason.SYSTEM) {
        prepare(PLAYGROUND, "Outside files", PERSONAL_NOTES)
        title(text = "Dropping pictures from the file manager")
        steps(
            text = "$OPEN_IMAGES\n3. In your file manager, open the Outside files folder of the Test data folder and " +
                "drag Found plate.png onto the note.\n4. Then drag Arepas plate.png from the vault's attachments " +
                "folder onto the note."
        )
        expected(
            text = "The first is copied as \"Found plate 1.png\" (the name was taken) and shows with its width, " +
                "![[Found plate 1.png|320]]. The second is already in the vault, so it is shown as ![[Arepas " +
                "plate.png|400]] without a copy. Changing one of the numbers resizes the picture."
        )
    }
}

private fun SuiteBuilder.pasteScreenshot() {
    manual(id = "1.8-07", reason = ManualReason.SYSTEM) {
        prepare(PLAYGROUND, "Outside files", PERSONAL_NOTES)
        title(text = "Pasting a screenshot")
        steps(
            text = "$OPEN_IMAGES\n3. Take a screenshot to the clipboard: Win+Shift+S on Windows, Ctrl+Shift+Cmd+4 on " +
                "macOS, your desktop's screenshot tool on Linux (copy to clipboard).\n4. Click an empty line of the " +
                "note and press Ctrl+V (Cmd+V on macOS).\n5. Copy a word from anywhere and paste it in the note too."
        )
        expected(
            text = "A line ![[Pasted image <date and time>.png|400]] appears with the screenshot under it, and the " +
                "file is in the attachments folder. The copied word is pasted as plain text."
        )
    }
}

private fun SuiteBuilder.obsidianAttachmentFolder() {
    manual(id = "1.8-08", reason = ManualReason.SYSTEM) {
        prepare(PLAYGROUND, "Outside files", PERSONAL_NOTES)
        title(text = "Pasted pictures follow Obsidian's attachment setting")
        steps(
            text = "$OPEN_IMAGES\n3. Close Notekea. In the Playground Vault folder, open the hidden folder .obsidian " +
                "(on macOS press Cmd+Shift+. in Finder to see it) and open app.json with a text editor.\n4. Add " +
                "\"attachmentFolderPath\": \"./\" inside its braces, save, and open the Playground Vault in Notekea " +
                "again.\n5. Paste a screenshot into Editor › Images and files, as in the case before."
        )
        expected(text = "The pasted picture is saved next to the note, in the Editor folder, as Obsidian would do it.")
    }
}

private fun SuiteBuilder.sameInObsidian() {
    manual(id = "1.8-09", reason = ManualReason.SYSTEM) {
        prepare(PLAYGROUND, "Outside files", PERSONAL_NOTES)
        title(text = "The same pictures in Obsidian")
        steps(
            text = "If Obsidian is not installed, press Skip for now.\n$OPEN_IMAGES\n3. Open the Playground Vault in " +
                "Obsidian and look at Editor › Images and files."
        )
        expected(
            text = "The text is unchanged and every picture, the PDFs and the pasted screenshot show as in Notekea. " +
                "Only Lost photo.png is gone, replaced by Found plate.png."
        )
    }
}

private fun SuiteBuilder.pictureInTable() {
    manual(id = "1.8-10", reason = ManualReason.SYSTEM) {
        prepare(PLAYGROUND, "Outside files", PERSONAL_NOTES)
        title(text = "A picture inside a table")
        steps(
            text = "$OPEN_IMAGES\n3. Click Preview and scroll to the table at the end.\n4. Click Edit, add a row | " +
                "Cachapa |  | under the table, put the cursor in its empty Photo cell and choose More (⋯) › Image or " +
                "file, picking Found plate.png from the Outside files folder."
        )
        expected(
            text = "The plate shows 80 wide inside its cell, not the text \"80\". The new picture is written inside " +
                "the cell with its width and the pipe escaped, the row keeps its two columns, and Preview shows the " +
                "green plate in it."
        )
    }
}

private fun SuiteBuilder.waylandClipboard() {
    manual(id = "1.8-11", reason = ManualReason.SYSTEM) {
        prepare(PLAYGROUND, "Outside files", PERSONAL_NOTES)
        title(text = "Pasting pictures on Linux with Wayland")
        steps(
            text = "Only on a Linux desktop with Wayland (GNOME, KDE, COSMIC). On Windows and macOS press Skip for " +
                "now.\n$OPEN_IMAGES\n3. If wl-clipboard is installed (wl-paste --version answers), uninstall it for " +
                "this case, copy a screenshot and press Ctrl+V in the note.\n4. Install wl-clipboard again, restart " +
                "Notekea and press Ctrl+V."
        )
        expected(
            text = "Without wl-clipboard, within a second a notice says how to install it and nothing is pasted. " +
                "With it, the screenshot is pasted at once. On GNOME, write in a note whether it pasted."
        )
    }
}

private fun SuiteBuilder.dropNote() {
    manual(id = "1.8-14", reason = ManualReason.SYSTEM) {
        prepare(PLAYGROUND, "Outside files", PERSONAL_NOTES)
        title(text = "Dropping a note from the file manager")
        steps(
            text = "$OPEN_IMAGES\n3. In your file manager, drag any .md note (for example Welcome.md of the Personal " +
                "Notes vault in the Test data folder) onto the note."
        )
        expected(
            text = "The note is copied into the attachments folder, embedded on a line of its own, and its text " +
                "shows in a frame."
        )
    }
}
