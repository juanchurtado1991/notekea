package app.notekea.core.notes.data

import app.notekea.core.notes.domain.NoteFileAttributes
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.PosixFileAttributeView

/**
 * [NoteFileAttributes] on `java.nio`. Windows and macOS let a file's creation time be set; Linux does not, so there a
 * saved note is as new as its last save. POSIX permissions are kept on Linux and macOS; Windows has none to keep.
 */
class NioNoteFileAttributes : NoteFileAttributes {
    override fun copy(
        sourcePath: String,
        targetPath: String
    ) {
        val source = Path.of(sourcePath)
        val target = Path.of(targetPath)

        keeping {
            val created = Files.readAttributes(
                source,
                BasicFileAttributes::class.java
            ).creationTime()

            Files.getFileAttributeView(
                target,
                BasicFileAttributeView::class.java
            ).setTimes(null, null, created)
        }

        keeping {
            Files.getFileAttributeView(source, PosixFileAttributeView::class.java)?.let {
                Files.setPosixFilePermissions(target, it.readAttributes().permissions())
            }
        }
    }

    /** Each attribute apart: a volume that refuses a creation time still takes the permissions. */
    private inline fun keeping(attribute: () -> Unit) {
        try {
            attribute()
        } catch (_: IOException) {
            // A new note has nothing to keep, and a save never fails over its attributes.
        }
    }
}
