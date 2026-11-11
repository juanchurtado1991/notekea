package app.notekea.autotest.cases.m2

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import app.notekea.autotest.openNote
import app.notekea.autotest.openVault
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val SAMPLE = "Sample Vault"
private const val TAB_CLICKS = 3
private const val TAB_MILLIS = 2_000L
private const val AREPAS = "Arepas"
private const val AREPAS_TEXT = "pre-cooked corn flour"
private const val TASKS_LINE = "Buy flour for the arepas"
private const val OPEN_AREPAS =
    "1. In Notekea, open the Sample Vault (see TEST DATA above) and wait until the bar at " +
        "the bottom says \"41 notes ready to search\".\n2. Open Recipes/Arepas and look at Connections in the " +
        "panel on the right."

/** The cases of 2.5, backlinks (`MVP_SMOKE_TEST.md`): Connections' Linked and Unlinked tabs. */
fun SuiteBuilder.backlinksCases() {
    linkedMentions()
    linkUnlinked()
    connectionsLook()
    rightClickMenus()
}

private fun SuiteBuilder.linkedMentions() {
    automated(
        id = "2.5-01",
        title = "Linked lists the notes that link to the open one, embeds and headings too"
    ) {
        openVault(
            name = SAMPLE,
            shownText = "Recipes"
        )

        openNote(
            name = AREPAS,
            shownText = AREPAS_TEXT
        )

        openConnections(tab = "Linked")
        // Weekly menu links to Arepas and embeds its ^dough block; Embeds embeds its Ingredients heading.
        expectText(text = "Recipes · 2 mentions")

        expectText(
            text = "Arepas#Ingredients",
            substring = true
        )

        expectText(
            text = "Best next to Arepas",
            substring = true
        )
    }
}

private fun SuiteBuilder.linkUnlinked() {
    automated(
        id = "2.5-02",
        title = "Link turns a plain mention into a link, and it moves to Linked"
    ) {
        openVault(
            name = SAMPLE,
            shownText = "Recipes"
        )

        openNote(
            name = AREPAS,
            shownText = AREPAS_TEXT
        )

        openConnections(tab = "Unlinked")

        expectText(
            text = TASKS_LINE,
            substring = true
        )

        click(text = "Link")

        expectFile(
            path = "$SAMPLE/Guides/Tasks.md",
            containing = "Buy flour for the [[Arepas|arepas]]"
        )

        openConnections(tab = "Linked")

        expectText(
            text = "Buy flour for the arepas",
            substring = true
        )
    }
}

/**
 * Clicks a [tab] of Connections until it is the selected one. The panel's sections still move while the note's
 * outline and counts come in, and a press that lands while they move takes the focus but selects nothing: macOS's
 * runner failed 2.5-01 and 2.5-02 that way, on Related with the focus on the tab clicked.
 */
private fun CaseScope.openConnections(tab: String) {
    val selected = hasText(text = tab) and isSelected()

    repeat(times = TAB_CLICKS) {
        click(text = tab)

        val isOpen = runCatching {
            ui.waitUntilAtLeastOneExists(
                matcher = selected,
                timeoutMillis = TAB_MILLIS
            )
        }.isSuccess

        if (isOpen) return
    }

    throw CaseFailure(message = "expected the $tab tab of Connections to open, it stayed closed")
}

private fun SuiteBuilder.rightClickMenus() {
    manual(
        id = "2.5-04",
        reason = ManualReason.EYES
    ) {
        prepare(SAMPLE)
        title(text = "A right click offers a menu on rows, headings, cards and recent vaults")

        steps(
            text =
            "$OPEN_AREPAS\n3. Click Linked and right-click a row.\n4. Right-click a heading of the " +
                "Outline.\n5. Click the Recipes folder in the tree to open its tab, and right-click a card.\n" +
                "6. Close the vault and right-click a recent vault."
        )

        expected(
            text =
            "The row offers Open and Open to the side, the heading Go to heading, the card Bookmark, " +
                "Rename, Show in Files and Delete, and the recent vault Open vault and Remove."
        )
    }
}

private fun SuiteBuilder.connectionsLook() {
    manual(
        id = "2.5-03",
        reason = ManualReason.EYES
    ) {
        prepare(SAMPLE)
        title(text = "Connections reads well, and a mention opens where it is")

        steps(
            text =
            "$OPEN_AREPAS\n3. Click Related, Linked and Unlinked in turn.\n4. On Linked, click the Weekly " +
                "menu row; go back to Arepas and, on another row, click Open to the side (it shows under the " +
                "pointer).\n5. Close the vault and open it again."
        )

        expected(
            text =
            "Each tab says how many it holds; each row shows the note, its folder and the line that names " +
                "Arepas with the name highlighted, without Markdown marks. Clicking Weekly menu opens it with the " +
                "mention selected and in view; Open to the side splits the editor. Note info reads as a small " +
                "table. After reopening, the tab you left chosen is still chosen."
        )
    }
}
