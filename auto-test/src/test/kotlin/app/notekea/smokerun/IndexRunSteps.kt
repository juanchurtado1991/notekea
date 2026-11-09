@file:OptIn(ExperimentalTestApi::class)

package app.notekea.smokerun

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import java.io.File

private const val INDEX_MILLIS = 240_000L
private const val MILLIS = 1_000L
private const val MODEL_LET_GO_SECONDS = 200L
private const val NEW_NOTE = "Memory run note.md"
private const val NEW_NOTE_TEXT =
    "# Memory run note\n\nA new note, for the index to read while the app runs.\n"
private const val READY = "ready to search"
private const val RECENT = "Recent"
private const val SWITCH_VAULT = "Open another vault"

/**
 * The steps of an index memory run, each followed by a sample of [run]'s memory named after it: the first indexing of
 * [vault], whose [notes] end up ready to search, a search with the model loading and one with it loaded, the model let
 * go when idle, one note added and indexed, the app idle, and the vault left and opened again.
 */
internal fun ComposeUiTest.measureIndexing(
    app: SmokeRunApp,
    run: String,
    vault: String,
    notes: Int,
    search: Pair<String, String>
) {
    waitForText(
        text = "$notes notes $READY",
        substring = true,
        timeoutMillis = INDEX_MILLIS
    )

    SmokeRunReport.sample(
        run = run,
        moment = "first-index"
    )

    searchText(
        app = app,
        word = search.first,
        result = search.second
    )

    SmokeRunReport.sample(
        run = run,
        moment = "search"
    )

    searchText(
        app = app,
        word = search.first,
        result = search.second
    )

    SmokeRunReport.sample(
        run = run,
        moment = "search-again"
    )

    if (SmokeRunReport.idleSeconds > 0) {
        Thread.sleep(MODEL_LET_GO_SECONDS * MILLIS)

        SmokeRunReport.sample(
            run = run,
            moment = "model-let-go"
        )

        searchText(
            app = app,
            word = search.first,
            result = search.second
        )

        SmokeRunReport.sample(
            run = run,
            moment = "search-after-let-go"
        )
    }

    File(app.vaultPath(name = vault), NEW_NOTE).writeText(text = NEW_NOTE_TEXT)

    waitForText(
        text = "${notes + 1} notes $READY",
        substring = true,
        timeoutMillis = INDEX_MILLIS
    )

    SmokeRunReport.sample(
        run = run,
        moment = "note-indexed"
    )

    idle(run = run)
    onNode(matcher = hasContentDescription(value = SWITCH_VAULT)).performClick()
    waitForText(text = RECENT)

    SmokeRunReport.sample(
        run = run,
        moment = "vault-closed"
    )

    onNode(matcher = hasText(text = vault)).performClick()

    waitForText(
        text = READY,
        substring = true,
        timeoutMillis = INDEX_MILLIS
    )

    SmokeRunReport.sample(
        run = run,
        moment = "reopened"
    )
}
