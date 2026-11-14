package app.notekea.core.notes.domain

/** The folder the clipped web pages are kept in (4.4), known to the tree too, which offers to update what it holds. */
object WebSourcesFolder {
    val PATH = RelativePath(value = "Web sources")

    /** The heading a clipped page's own text is kept under; above it are the notes a model wrote about the page. */
    const val ORIGINAL_HEADING = "## Original text"

    // The heading alone on its line: "### Original text" or "## Original text of the law" are the person's own.
    private val ORIGINAL = Regex(
        pattern = "^$ORIGINAL_HEADING[ \\t]*\\r?$",
        option = RegexOption.MULTILINE
    )

    /** Where the page's own text starts in a clipped note's [body]; null for a note that is no clip. */
    fun originalStartIn(body: String): Int? = ORIGINAL
        .find(input = body)
        ?.range
        ?.first

    /** Whether [path] is the folder itself or something inside it. */
    fun holds(path: RelativePath): Boolean = path == PATH || path.isWithin(folder = PATH)
}
