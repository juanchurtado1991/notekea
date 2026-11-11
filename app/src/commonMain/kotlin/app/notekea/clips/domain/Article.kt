package app.notekea.clips.domain

/** The article taken out of a page: its [title] and its [paragraphs], without menus, ads or comments. */
data class Article(
    val title: String,
    val paragraphs: List<String>
) {
    val text: String get() = paragraphs.joinToString(separator = "\n\n")
}
