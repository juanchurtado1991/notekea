package app.notekea.sync.data

import java.io.File
import java.nio.file.Files

/** A name no file has yet: [file] itself, or "Note (mine) 2.md", "Note (mine) 3.md"… beside it. */
internal object FreeFile {
    fun beside(file: File): File {
        if (!file.exists()) return file
        val stem = file.nameWithoutExtension

        val extension = file
            .extension
            .takeIf { it.isNotEmpty() }
            ?.let { ".$it" }
            .orEmpty()

        return generateSequence(seed = 2) {
            it + 1
        }
            .map { File(file.parentFile, "$stem $it$extension") }
            .first {
                !it.exists()
            }
    }

    /**
     * Moves [file] to [wanted], or to the free name beside it, and returns where it went. A move that cannot happen
     * (a file another program holds open) throws, so nothing goes on to write over the file left in place.
     */
    fun moveBeside(
        file: File,
        wanted: File
    ): File {
        val free = beside(file = wanted)
        Files.move(file.toPath(), free.toPath())

        return free
    }
}
