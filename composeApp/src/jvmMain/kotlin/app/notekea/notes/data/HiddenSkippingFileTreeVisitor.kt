package app.notekea.notes.data

import io.methvin.watcher.visitor.FileTreeVisitor
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

/**
 * Walks the vault for the watcher without entering hidden folders. On Linux every watched folder costs an inotify
 * watch, and `.git` alone holds hundreds of folders; their changes are never shown, so they are never watched.
 * Folders that cannot be read are skipped instead of stopping the walk.
 */
class HiddenSkippingFileTreeVisitor : FileTreeVisitor {
    override fun recursiveVisitFiles(
        file: Path,
        onDirectory: FileTreeVisitor.Callback,
        onFile: FileTreeVisitor.Callback
    ) {
        if (isHidden(path = file)) return
        Files.walkFileTree(
            file,
            object : SimpleFileVisitor<Path>() {
                override fun preVisitDirectory(
                    dir: Path,
                    attrs: BasicFileAttributes
                ): FileVisitResult {
                    if (dir != file && isHidden(path = dir)) return FileVisitResult.SKIP_SUBTREE
                    onDirectory.call(dir)
                    return FileVisitResult.CONTINUE
                }

                override fun visitFile(
                    file: Path,
                    attrs: BasicFileAttributes
                ): FileVisitResult {
                    if (!isHidden(path = file)) onFile.call(file)
                    return FileVisitResult.CONTINUE
                }

                override fun visitFileFailed(
                    file: Path,
                    exc: IOException
                ): FileVisitResult = FileVisitResult.CONTINUE
            }
        )
    }

    private fun isHidden(
        path: Path
    ): Boolean = path.fileName?.toString()?.startsWith(prefix = HIDDEN_PREFIX) == true

    companion object {
        private const val HIDDEN_PREFIX = "."
    }
}
