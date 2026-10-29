package app.notekea.selftest

import app.notekea.notes.domain.AttachmentPicker

/** The file picker in a case: it answers [nextPick] (null as a cancel) instead of showing the system's dialog. */
class ScriptedAttachmentPicker : AttachmentPicker {
    var nextPick: String? = null

    override suspend fun pickFile(): String? = nextPick
}
