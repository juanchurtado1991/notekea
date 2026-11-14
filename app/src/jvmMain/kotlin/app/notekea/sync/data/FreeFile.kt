package app.notekea.sync.data

import java.io.File

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
}
