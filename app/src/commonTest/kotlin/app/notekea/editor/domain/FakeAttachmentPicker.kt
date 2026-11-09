package app.notekea.editor.domain

import kotlinx.coroutines.CompletableDeferred

/**
 * The system's file picker as a test answers it: [picked] is the chosen file's path, null for a cancel. It stays open
 * until [closes] completes, when a test sets it.
 */
class FakeAttachmentPicker(var picked: String? = null) : AttachmentPicker {
    var opened = 0
    var closes: CompletableDeferred<Unit>? = null

    override suspend fun pickFile(): String? {
        opened++
        closes?.await()

        return picked
    }
}
