package app.notekea.editor.presentation.viewmodel

import androidx.compose.runtime.snapshots.Snapshot
import app.notekea.editor.presentation.model.PropertyIntent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlin.test.Test
import kotlin.test.assertEquals

/** What a save leaves behind: the file split again as it is read, and text that changed during the write. */
@OptIn(ExperimentalCoroutinesApi::class)
class EditorSaveSplitTest : EditorViewModelTestBase() {
    @Test
    fun `frontmatter typed at the top of the text becomes the note's properties, written once`() =
        runEditorTest {
            putOnDisk(
                path = TASKS,
                text = "Plain\n"
            )

            open(path = TASKS)
            type(text = "---\ntitle: x\n---\nPlain\n")
            autosave()
            type(text = "Plain, typed\n")
            autosave()

            assertEquals(
                expected = "---\ntitle: x\n---\nPlain, typed\n",
                actual = store.texts[TASKS]
            )
        }

    @Test
    fun `a delimiter typed in the properties' source keeps the lines after it in the note`() =
        runEditorTest {
            open(path = AREPAS)

            viewModel.onIntent(
                intent = PropertyIntent.OnSourceCommit(
                    path = AREPAS,
                    yaml = "tags: [recipe]\n---\nkept: yes"
                )
            )

            runCurrent()
            Snapshot.sendApplyNotifications()
            runCurrent()
            type(text = "${text()}More\n")
            autosave()

            assertEquals(
                expected = "---\ntags: [recipe]\n---\nkept: yes\n---\n# Arepas\nMore\n",
                actual = store.texts[AREPAS]
            )
        }

    @Test
    fun `an edit undone while the autosave writes is saved after all`() = runEditorTest {
        open(path = AREPAS)
        val slowDisk = CompletableDeferred<Unit>()
        type(text = "Changed\n")
        store.readGate = slowDisk
        autosave()

        type(text = "# Arepas\n")
        slowDisk.complete(value = Unit)
        store.readGate = null
        runCurrent()
        autosave()

        assertEquals(
            expected = AREPAS_TEXT,
            actual = store.texts[AREPAS]
        )
    }

    private fun TestScope.autosave() {
        advanceTimeBy(delayTimeMillis = SECOND_MILLIS + 1)
        runCurrent()
    }

    private fun text(): String = viewModel
        .buffer
        .text
        .toString()
}
