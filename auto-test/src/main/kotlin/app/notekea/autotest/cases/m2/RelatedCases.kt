package app.notekea.autotest.cases.m2

import app.notekea.autotest.openNote
import app.notekea.autotest.openVault
import app.notekea.autotest.typeAtEnd
import com.testbuddy.CaseFailure
import com.testbuddy.CaseScope
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val SAMPLE = "Sample Vault"
private const val SAMPLE_READY = "40 notes ready to search"
private const val ALREADY_LINKED = "ALREADY LINKED"
private const val WAIT_MILLIS = 10_000L
private const val OPEN_AREPAS =
    "1. In Notekea, open the Sample Vault (see TEST DATA above) and wait until the bar at " +
        "the bottom says \"$SAMPLE_READY\".\n2. Open Recipes/Arepas and look at Connections, on Related, in the " +
        "panel on the right."

/** The cases of 2.4, the related notes panel (`MVP_SMOKE_TEST.md`): the inspector's Related section. */
fun SuiteBuilder.relatedCases() {
    linkedLeftOut()
    newLinkLeftOut()
    byMeaning()
    rowActions()
}

private fun SuiteBuilder.linkedLeftOut() {
    automated(
        id = "2.4-01",
        title = "The notes a note links are under Already linked, not among its cards"
    ) {
        openVault(
            name = SAMPLE,
            shownText = "Recipes"
        )

        expectText(text = SAMPLE_READY)

        openNote(
            name = "Weekly menu",
            shownText = "Monday"
        )

        expectAlreadyLinked()
    }
}

private fun SuiteBuilder.newLinkLeftOut() {
    automated(
        id = "2.4-02",
        title = "A link typed in the note moves that note to Already linked"
    ) {
        openVault(
            name = SAMPLE,
            shownText = "Recipes"
        )

        expectText(text = SAMPLE_READY)

        openNote(
            name = "Arepas",
            shownText = "pre-cooked corn flour"
        )

        if (runCatching { scrollTo(text = ALREADY_LINKED) }.isSuccess) {
            throw CaseFailure(message = "expected no \"$ALREADY_LINKED\" before the note links one")
        }

        typeAtEnd(
            part = "pre-cooked corn flour",
            text = "\nSee [[Pancakes]]."
        )

        expectAlreadyLinked()
    }
}

/** The header sits below Related's rows, in a lazy list that makes it only when it is scrolled to. */
private fun CaseScope.expectAlreadyLinked() {
    ui
        .waitUntil(timeoutMillis = WAIT_MILLIS) {
            runCatching { scrollTo(text = ALREADY_LINKED) }.isSuccess
        }
}

private fun SuiteBuilder.byMeaning() {
    manual(
        id = "2.4-03",
        reason = ManualReason.EYES
    ) {
        prepare(SAMPLE)
        title(text = "Related shows the notes closest in meaning, and follows the open note")

        steps(
            text = "$OPEN_AREPAS\n3. Open Recipes/Pancakes, then Guides/Embeds, and look at Related each time."
        )

        expected(
            text =
            "Each time, Related lists notes about the same subject first (for Arepas, other recipes), each " +
                "row with the note's name, its folder and a passage with the closest sentence highlighted. The " +
                "open note and the notes it links are not among the rows; the notes it links are under Already " +
                "linked. It changes within a second of opening another note."
        )
    }
}

private fun SuiteBuilder.rowActions() {
    manual(
        id = "2.4-04",
        reason = ManualReason.EYES
    ) {
        prepare(SAMPLE)
        title(text = "A related row opens its note, opens it beside, or inserts a link to it")

        steps(
            text =
            "$OPEN_AREPAS\n3. Put the pointer on the first row and click its last button, Insert link.\n" +
                "4. Put the pointer on another row and click its first button, Open to the side.\n5. Click a " +
                "row itself."
        )

        expected(
            text =
            "Two buttons show on the row under the pointer (Open to the side, Insert link). Insert link " +
                "writes a link to that note where the cursor is in Arepas (at the end of the note in Preview), " +
                "and the note moves to Already linked. Open to the side splits the editor and opens the note in " +
                "the other pane. Clicking a row opens its note."
        )
    }
}
