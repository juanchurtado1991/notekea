package app.notekea.notes.data

import app.notekea.notes.domain.AttachmentPicker
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.path

/** [AttachmentPicker] on FileKit's native dialog. A dialog that fails to open is logged and treated as a cancel. */
class FileKitAttachmentPicker : AttachmentPicker {
    private val logger = Logger.withTag(tag = LOG_TAG)

    override suspend fun pickFile(): String? = try {
        FileKit.openFilePicker()?.path
    } catch (exception: FileKitDialogException) {
        logger.w(throwable = exception) { DIALOG_FAILED_MESSAGE }
        null
    }

    companion object {
        private const val DIALOG_FAILED_MESSAGE = "The native file picker could not be opened"
        private const val LOG_TAG = "FileKitAttachmentPicker"
    }
}
