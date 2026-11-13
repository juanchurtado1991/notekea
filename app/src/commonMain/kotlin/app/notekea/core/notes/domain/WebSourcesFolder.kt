package app.notekea.core.notes.domain

/** The folder the clipped web pages are kept in (4.4), known to the tree too, which offers to update what it holds. */
object WebSourcesFolder {
    val PATH = RelativePath(value = "Web sources")

    /** Whether [path] is the folder itself or something inside it. */
    fun holds(path: RelativePath): Boolean = path == PATH || path.isWithin(folder = PATH)
}
