package app.notekea.selftest.cases.m2

import app.notekea.selftest.openNote
import app.notekea.selftest.openVault
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val SAMPLE = "Sample Vault"
private const val AREPAS = "Arepas"
private const val AREPAS_TEXT = "pre-cooked corn flour"
private const val TASKS_LINE = "Buy flour for the arepas"
private const val OPEN_AREPAS =
    "1. In Notekea, open the Sample Vault (see TEST DATA above) and wait until the bar at " +
        "the bottom says \"40 notes ready to search\".\n2. Open Recipes/Arepas and look at Connections in the " +
        "panel on the right."

/** The cases of 2.5, backlinks (`MVP_SMOKE_TEST.md`): Connections' Linked and Unlinked tabs. */
fun SuiteBuilder.backlinksCases() {
    linkedMentions()
    linkUnlinked()
    connectionsLook()
}

private fun SuiteBuilder.linkedMentions() {
    automated(id = "2.5-01", title = "Linked lists the notes that link to the open one, embeds and headings too") {
        openVault(name = SAMPLE, shownText = "Recipes")
        openNote(name = AREPAS, shownText = AREPAS_TEXT)
        click(text = "Linked")
        // Weekly menu links to Arepas and embeds its ^dough block; Embeds embeds its Ingredients heading.
        expectText(text = "Recipes · 2 mentions")
        expectText(text = "Arepas#Ingredients", substring = true)
        expectText(text = "Best next to Arepas", substring = true)
    }
}

private fun SuiteBuilder.linkUnlinked() {
    automated(id = "2.5-02", title = "Link turns a plain mention into a link, and it moves to Linked") {
        openVault(name = SAMPLE, shownText = "Recipes")
        openNote(name = AREPAS, shownText = AREPAS_TEXT)
        click(text = "Unlinked")
        expectText(text = TASKS_LINE, substring = true)
        click(text = "Link")
        expectFile(path = "$SAMPLE/Guides/Tasks.md", containing = "Buy flour for the [[Arepas|arepas]]")
        click(text = "Linked")
        expectText(text = "Buy flour for the arepas", substring = true)
    }
}

private fun SuiteBuilder.connectionsLook() {
    manual(id = "2.5-03", reason = ManualReason.EYES) {
        prepare(SAMPLE)
        title(text = "Connections reads well, and a mention opens where it is")
        steps(
            text = "$OPEN_AREPAS\n3. Click Related, Linked and Unlinked in turn.\n4. On Linked, click the Weekly " +
                "menu row; go back to Arepas and, on another row, click Open to the side (it shows under the " +
                "pointer).\n5. Close the vault and open it again."
        )
        expected(
            text = "Each tab says how many it holds; each row shows the note, its folder and the line that names " +
                "Arepas with the name highlighted, without Markdown marks. Clicking Weekly menu opens it with the " +
                "mention selected and in view; Open to the side splits the editor. Note info reads as a small " +
                "table. After reopening, the tab you left chosen is still chosen."
        )
    }
}
