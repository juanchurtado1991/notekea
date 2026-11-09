package app.notekea.vault.data

import app.notekea.vault.domain.FolderPicker
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogException
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.github.vinceglb.filekit.path

/** [FolderPicker] on FileKit's native dialogs. A dialog that fails to open is logged and treated as a cancel. */
class FileKitFolderPicker : FolderPicker {
    private val logger = Logger.withTag(tag = LOG_TAG)

    override suspend fun pickFolder(
        initialPath: String?
    ): String? = try {
        FileKit.openDirectoryPicker(directory = initialPath?.let { PlatformFile(path = it) })?.path
    } catch (exception: FileKitDialogException) {
        logger.w(throwable = exception) { DIALOG_FAILED_MESSAGE }
        null
    }

    companion object {
        private const val DIALOG_FAILED_MESSAGE = "The native folder picker could not be opened"
        private const val LOG_TAG = "FileKitFolderPicker"
    }
}
