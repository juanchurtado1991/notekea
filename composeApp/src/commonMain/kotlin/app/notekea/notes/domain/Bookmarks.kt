package app.notekea.notes.domain

/**
 * The entries a vault has bookmarked (`ROADMAP.md` 1.13), in the order the user gave them: notes, folders or other
 * files, each at most once. A bookmark follows its entry when it is renamed or moved in Notekea, and goes when the
 * entry is deleted there, folders taking the bookmarks inside them along.
 */
data class Bookmarks(val paths: List<RelativePath> = emptyList()) {
    operator fun contains(
        path: RelativePath
    ): Boolean = path in paths

    /** Adds [path] at the end, or removes it when it is already bookmarked. */
    fun toggled(
        path: RelativePath
    ): Bookmarks = if (path in paths) without(path = path) else Bookmarks(paths = paths + path)

    /** Only [path] removed, not what is inside it: the user's Remove. */
    fun without(
        path: RelativePath
    ): Bookmarks = Bookmarks(paths = paths - path)

    /** The bookmark at [from] moved to [to] among the others (both within range); the same bookmarks otherwise. */
    fun moved(
        from: Int,
        to: Int
    ): Bookmarks {
        if (from !in paths.indices || to !in paths.indices || from == to) return this
        val reordered = paths.toMutableList().apply { add(index = to, element = removeAt(index = from)) }
        return Bookmarks(paths = reordered)
    }

    /** After [from] was renamed or moved to [to]: its bookmark and those inside it follow. */
    fun renamed(
        from: RelativePath,
        to: RelativePath
    ): Bookmarks = Bookmarks(paths = paths.map { it.rebased(from = from, to = to) }.distinct())

    /** After [path] was deleted: its bookmark and those inside it go. */
    fun deleted(
        path: RelativePath
    ): Bookmarks = Bookmarks(paths = paths.filterNot { it.isWithin(folder = path) && !path.isRoot })
}
