package app.notekea.selftest.cases.m1

import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val OPEN_PLAYGROUND =
    "1. Open Notekea and open the Playground Vault from the Test data folder (Open folder…)."

/** The cases of 1.14 a person must look at: the tool rail, the scrollbars and the workspace's details. */
internal fun SuiteBuilder.workspaceLookManualCases() {
    toolRail()
    scrollbars()
    workspaceLook()
}

private fun SuiteBuilder.toolRail() {
    manual(id = "1.14-10", reason = ManualReason.EYES) {
        prepare(PLAYGROUND)
        title(text = "The tool rail, About and Settings look right")
        steps(
            text = "$OPEN_PLAYGROUND\n2. Open two notes. Point at each icon of the narrow bar on the far left.\n3. " +
                "Click About Notekea (the ⓘ at the bottom), look at it and press Esc.\n4. Click Settings (the gear) " +
                "and close it with ✕.\n5. Click Side panel (the second icon) twice.\n6. Click Open another vault."
        )
        expected(
            text = "The bar's top has the Notekea logo, then Files, Side panel, Search notes and Today; its bottom " +
                "has Open another vault, About Notekea and Settings, each named when pointed at. The open note's tab " +
                "rises with rounded corners into the note; the other tabs are quiet, with a thin line between them " +
                "and their ✕ only when pointed at. About shows the logo, Notekea, \"Version 0.1.0-alpha\", GitHub " +
                "and this computer's shortcuts as keys (Notes, Writing, Workspace) with a scrollbar beside them; Esc " +
                "closes it. Settings shows its sections dimmed and \"Coming soon\". Side panel hides and shows the " +
                "panel on the right. Open another vault shows the welcome screen: the logo, Notekea and the actions " +
                "on the left, the recent vaults on the right."
        )
    }
}

private fun SuiteBuilder.scrollbars() {
    manual(id = "1.14-11", reason = ManualReason.EYES) {
        prepare(PLAYGROUND, "Personal Notes", "Work Obsidian", "Empty Vault", "Daily Vault", "Templates Vault")
        title(text = "Long lists show a thin scrollbar")
        steps(
            text = "1. Open Notekea. With Open another vault (bottom left) and Open folder…, open one after another " +
                "these vaults of the Test data folder: Personal Notes, Work Obsidian, Empty Vault, Playground Vault, " +
                "Daily Vault and Templates Vault.\n2. Click Open another vault and scroll the Recent list.\n3. Open " +
                "the Playground Vault, click Expand all folders (above the list of notes) and scroll the list.\n4. " +
                "Open Editor/Outline practice and scroll the outline on the right."
        )
        expected(
            text = "Recent shows five vaults with a thin scrollbar on its right and scrolls inside to the sixth. The " +
                "list of notes and the outline show a scrollbar only while they hold more than fits, and it darkens " +
                "when pointed at."
        )
    }
}

private fun SuiteBuilder.workspaceLook() {
    manual(id = "1.14-13", reason = ManualReason.EYES) {
        prepare(PLAYGROUND)
        title(text = "Tabs, the tree's selection and the status bar look right")
        steps(
            text = "$OPEN_PLAYGROUND\n2. Open ten notes in tabs and make the window narrow.\n3. Click the ⌄ at the " +
                "end of the row of tabs and pick a tab.\n4. Click a note in the list on the left, then click in the " +
                "note's text and type a word, watching the end of the bar at the bottom.\n5. Point at the vault's " +
                "name at the top of the list."
        )
        expected(
            text = "The row of tabs fades out at its end; the ⌄ lists every open tab, the open one checked, and " +
                "picks one; no tab is narrower than a short name. The selected note in the list is blue while the " +
                "list has the keyboard and grey once the text has it, with no ring around it. The list shows " +
                "BOOKMARKS and FILES headers. The panes meet at thin lines with no dark gap. The bottom bar ends " +
                "with \"Unsaved\" while you type, then \"Saved\". The vault's folder shows only when pointing at its " +
                "name."
        )
    }
}
