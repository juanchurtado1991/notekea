package app.notekea.notes.domain

import kotlin.jvm.JvmInline

/**
 * A file or folder inside a vault, relative to the vault folder, with `/` as the separator on every operating system
 * so paths compare equal everywhere and can be stored as they are. The vault folder itself is [ROOT] (`""`).
 */
@JvmInline
value class RelativePath(val value: String) {
    val isMarkdown: Boolean
        get() = value.endsWith(suffix = MARKDOWN_EXTENSION, ignoreCase = true)

    val isRoot: Boolean
        get() = value.isEmpty()

    /** The last segment: the file or folder name as it is on disk. */
    val name: String
        get() = value.substringAfterLast(delimiter = SEPARATOR)

    /** The name without the Markdown extension, as notes are shown and linked (`Tomato plan`). */
    val nameWithoutExtension: String
        get() = if (isMarkdown) name.dropLast(n = MARKDOWN_EXTENSION.length) else name

    /** The containing folder; [ROOT] for top-level entries and null for [ROOT] itself. */
    val parent: RelativePath?
        get() = when {
            isRoot -> null
            SEPARATOR in value -> RelativePath(value = value.substringBeforeLast(delimiter = SEPARATOR))
            else -> ROOT
        }

    fun child(
        name: String
    ): RelativePath = if (isRoot) RelativePath(value = name) else RelativePath(value = value + SEPARATOR + name)

    /** True when this path is [folder] itself or anything inside it; every path is inside [ROOT]. */
    fun isWithin(
        folder: RelativePath
    ): Boolean = folder.isRoot || value == folder.value || value.startsWith(prefix = folder.value + SEPARATOR)

    /** This path after [from] was renamed or moved to [to]; paths outside [from] stay as they are. */
    fun rebased(
        from: RelativePath,
        to: RelativePath
    ): RelativePath = if (isWithin(folder = from)) {
        RelativePath(value = to.value + value.removePrefix(prefix = from.value))
    } else {
        this
    }

    companion object {
        const val MARKDOWN_EXTENSION = ".md"
        const val SEPARATOR = "/"
        val ROOT = RelativePath(value = "")
    }
}
