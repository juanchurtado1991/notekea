package app.notekea.sync.domain

import app.notekea.core.notes.domain.RelativePath

/**
 * The names of a conflict's copies: our version of `Ideas/Plan.md` waits as `Ideas/Plan (conflict).md`, and "Keep
 * both" keeps it as `Ideas/Plan (mine).md`.
 */
object ConflictCopies {
    private const val CONFLICT = " (conflict)"
    private const val MINE = " (mine)"

    fun copyOf(path: RelativePath): RelativePath = suffixed(
        path = path,
        suffix = CONFLICT
    )

    fun mineOf(path: RelativePath): RelativePath = suffixed(
        path = path,
        suffix = MINE
    )

    /** The note [copy] is the conflict copy of, or null when it is not one. */
    fun noteOf(copy: RelativePath): RelativePath? {
        val name = copy.name

        val dot = name.lastIndexOf(char = '.').takeIf {
            it > 0
        } ?: name.length

        val stem = name.substring(0, dot)
        if (!stem.endsWith(suffix = CONFLICT)) return null

        val original = stem.removeSuffix(suffix = CONFLICT) + name.substring(dot)

        return copy.parent?.child(name = original) ?: RelativePath(value = original)
    }

    private fun suffixed(
        path: RelativePath,
        suffix: String
    ): RelativePath {
        val name = path.name

        val dot = name.lastIndexOf(char = '.').takeIf {
            it > 0
        } ?: name.length

        val renamed = name.substring(0, dot) + suffix + name.substring(dot)

        return path.parent?.child(name = renamed) ?: RelativePath(value = renamed)
    }
}
