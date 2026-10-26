package app.notekea.notes.domain

/**
 * Finds tags for the tag picker (`ROADMAP.md` 1.17) among the vault's [tags], already ranked by [TagRanking]: typed
 * text (a leading `#` ignored, letter case too) matches tags that start with it first, then tags that contain it,
 * each group in rank order. [newTag] is the typed text as a tag to create, when it is a valid tag the vault lacks.
 */
class TagSearch {
    fun search(
        tags: List<String>,
        query: String,
        limit: Int
    ): List<String> {
        val typed = query.trim().removePrefix(prefix = TAG_MARK).lowercase()
        if (typed.isEmpty()) return tags.take(n = limit)
        val (starting, others) = tags.filter { it.lowercase().contains(other = typed) }
            .partition { it.lowercase().startsWith(prefix = typed) }
        return (starting + others).take(n = limit)
    }

    /** [query] as a new tag (spaces become `-`), or null when it is empty, not a valid tag or already in [tags]. */
    fun newTag(
        tags: List<String>,
        query: String
    ): String? {
        val name = query.trim().removePrefix(prefix = TAG_MARK).replace(regex = SPACES, replacement = "-")
        val isValid = VALID.matches(input = name)
        return name.takeIf { isValid && tags.none { it.equals(other = name, ignoreCase = true) } }
    }

    companion object {
        private const val TAG_MARK = "#"
        private val SPACES = Regex(pattern = "\\s+")
        private val VALID = Regex(pattern = "[\\p{L}\\p{N}_/-]*[\\p{L}_/-][\\p{L}\\p{N}_/-]*")
    }
}
