package app.notekea.notes.domain

/** The system's file picker as a test answers it: [picked] is the chosen file's path, null for a cancel. */
class FakeAttachmentPicker(var picked: String? = null) : AttachmentPicker {
    var opened = 0

    override suspend fun pickFile(): String? {
        opened++
        return picked
    }
}
