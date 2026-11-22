package app.notekea.core.notes.data

import kotlinx.coroutines.delay
import okio.Buffer
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.use
import kotlin.random.Random

private const val HEX_RADIX = 16
private const val HIDDEN_PREFIX = "."
private const val ID_LENGTH = 8
private const val ID_PADDING = '0'
private const val TEMPORARY_SUFFIX = ".notekea-tmp"
private const val MOVE_ATTEMPTS = 5
private const val MOVE_PAUSE_MILLIS = 50L

/**
 * [file] made [content], whole or not at all: a hidden temporary file beside it reaches the disk, is handed to
 * [beforeMove] and replaces [file] with an atomic move, tried again a few times as Windows refuses it while [file] is
 * read. On failure it throws and [file] is as it was; the temporary file is removed however the write ends.
 */
internal suspend fun FileSystem.writeWhole(
    file: Path,
    content: String,
    beforeMove: (temporary: Path) -> Unit = {}
) {
    val temporary = file.parent?.div(child = temporaryName())
        ?: throw IOException("$file has no folder to be written in")

    try {
        writeDurably(
            file = temporary,
            content = content
        )

        beforeMove(temporary)

        moveRetrying(
            source = temporary,
            target = file
        )
    } finally {
        removeLeftover(file = temporary)
    }
}

private fun FileSystem.removeLeftover(file: Path) {
    try {
        delete(
            path = file,
            mustExist = false
        )
    } catch (_: IOException) {
        // The file is as it should be; a locked temporary file stays hidden.
    }
}

/** The same length for every file; random, so two files saved at once never share one. */
private fun temporaryName(): String {
    val id = Random
        .nextInt()
        .toUInt()
        .toString(radix = HEX_RADIX)
        .padStart(
            length = ID_LENGTH,
            padChar = ID_PADDING
        )

    return HIDDEN_PREFIX + id + TEMPORARY_SUFFIX
}

private fun FileSystem.writeDurably(
    file: Path,
    content: String
) {
    val bytes = Buffer().writeUtf8(string = content)

    openReadWrite(
        file = file,
        mustCreate = true
    ).use { handle ->
        handle.write(
            fileOffset = 0L,
            source = bytes,
            byteCount = bytes.size
        )

        handle.flush()
    }
}

private suspend fun FileSystem.moveRetrying(
    source: Path,
    target: Path
) {
    repeat(times = MOVE_ATTEMPTS - 1) {
        try {
            atomicMove(
                source = source,
                target = target
            )

            return
        } catch (_: IOException) {
            delay(timeMillis = MOVE_PAUSE_MILLIS)
        }
    }

    atomicMove(
        source = source,
        target = target
    )
}
