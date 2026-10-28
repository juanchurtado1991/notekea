package com.testbuddy

import java.nio.file.Files
import java.nio.file.Path

/**
 * The file checks of [CaseScope]. Apps save in the background, so each check polls the disk through [waitFor], which
 * lets the app run between polls and answers whether the condition held in time.
 */
internal class CaseFiles(
    private val folders: CaseFolders,
    private val waitFor: (condition: () -> Boolean) -> Boolean
) {
    fun resolve(path: String): Path = folders.data.resolve(path).normalize().also {
        require(it.startsWith(folders.data)) { "\"$path\" is outside the case's data folder" }
    }

    fun expectFile(
        path: String,
        content: String?,
        containing: String?
    ) {
        val file = resolve(path = path)
        val holds = waitFor { Files.isRegularFile(file) && matches(text = read(file = file), content, containing) }
        if (holds) return
        val failure = when {
            !Files.isRegularFile(file) -> "expected the file \"$path\", it does not exist"
            content != null -> "expected \"$path\" to read \"$content\", it reads \"${read(file = file)}\""
            else -> "expected \"$path\" to contain \"$containing\", it reads \"${read(file = file)}\""
        }
        throw CaseFailure(message = failure)
    }

    fun expectNoFile(path: String) {
        val file = resolve(path = path)
        if (!waitFor { Files.notExists(file) }) throw CaseFailure(message = "expected no file \"$path\", it is there")
    }

    private fun matches(
        text: String,
        content: String?,
        containing: String?
    ): Boolean = (content == null || text == content) && (containing == null || text.contains(containing))

    private fun read(file: Path): String = runCatching { Files.readString(file) }.getOrDefault("")
}
