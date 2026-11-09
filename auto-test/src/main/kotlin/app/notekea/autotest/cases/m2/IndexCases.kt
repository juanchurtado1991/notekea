package app.notekea.autotest.cases.m2

import app.notekea.autotest.openNote
import app.notekea.autotest.openVault
import app.notekea.autotest.typeAtEnd
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val SAMPLE = "Sample Vault"
private const val GHOST = "Ghost Serializer"
private const val SAMPLE_READY = "40 notes ready to search"
private const val GHOST_READY = "371 notes ready to search"
private const val OPEN_GHOST =
    "1. In Notekea, open the Ghost Serializer vault (see TEST DATA above)."

/** The cases of 2.3, the hybrid index (`MVP_SMOKE_TEST.md`): the index follows the vault's notes. */
fun SuiteBuilder.indexCases() {
    vaultReady()
    editedNote()
    resumes()
    noticeAndDetails()
}

private fun SuiteBuilder.vaultReady() {
    automated(
        id = "2.3-01",
        title = "A vault's notes are indexed when it opens"
    ) {
        openVault(
            name = SAMPLE,
            shownText = "Recipes"
        )

        expectText(text = SAMPLE_READY)
    }
}

private fun SuiteBuilder.editedNote() {
    automated(
        id = "2.3-02",
        title = "An edited note is indexed again within seconds"
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

        typeAtEnd(
            part = "pre-cooked corn flour",
            text = "\nA pinch of salt."
        )

        expectText(text = "Saved")
        expectText(text = SAMPLE_READY)
        click(text = SAMPLE_READY)
        expectText(text = "Just now")
    }
}

private fun SuiteBuilder.resumes() {
    manual(
        id = "2.3-03",
        reason = ManualReason.PACKAGED
    ) {
        prepare(GHOST)
        title(text = "Indexing goes on after Notekea is closed in the middle")

        steps(
            text =
            "$OPEN_GHOST\n2. While the bar at the bottom says Indexing, click it and note how many notes are " +
                "done (\"120 of 371 notes\").\n3. Close Notekea before it ends, open it again and click Indexing " +
                "again."
        )

        expected(
            text =
            "It goes on from the notes it had done: the count and the percent do not start again from 0, " +
                "and it ends with \"$GHOST_READY\"."
        )
    }
}

private fun SuiteBuilder.noticeAndDetails() {
    manual(
        id = "2.3-04",
        reason = ManualReason.EYES
    ) {
        prepare(GHOST)
        title(text = "The first indexing's notice and the index's details")

        steps(
            text =
            "$OPEN_GHOST\n2. Look at the top of the window, then click Dismiss on the notice there.\n3. Click " +
                "Indexing in the bar at the bottom and look at the card that opens.\n4. When it says " +
                "\"$GHOST_READY\", click it, then click Rebuild index."
        )

        expected(
            text =
            "A notice says Notekea is reading your notes and you can keep writing; Dismiss hides it. The " +
                "card shows a bar, how many notes of 371 and about how long is left; the bar at the bottom shows " +
                "the percent with a small ring. Rebuild index starts again from 0 % and ends ready again."
        )
    }
}
