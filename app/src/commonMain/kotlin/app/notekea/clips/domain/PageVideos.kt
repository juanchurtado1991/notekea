package app.notekea.clips.domain

import app.notekea.core.notes.domain.YouTubeLinks

/**
 * The YouTube videos a page plays (the developer's request of 2026-11-11), as its HTML embeds them: YouTube's player in
 * an `<iframe>`, with the title it gives it, or a `<lite-youtube>` element; at most [MAX_VIDEOS], each once.
 */
object PageVideos {
    private const val MAX_VIDEOS = 10
    private val IFRAME = Regex(
        pattern = "<iframe\\b[^>]*>",
        option = RegexOption.IGNORE_CASE
    )
    private val LITE = Regex(
        pattern = "<lite-youtube\\b[^>]*>",
        option = RegexOption.IGNORE_CASE
    )

    private fun attribute(name: String) = Regex(
        pattern = "\\b$name\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)')",
        option = RegexOption.IGNORE_CASE
    )

    private val SOURCE = attribute(name = "(?:data-)?src")
    private val TITLE = attribute(name = "title")
    private val VIDEO_ID = attribute(name = "videoid")
    private val PLAY_LABEL = attribute(name = "playlabel")

    fun of(html: String): List<PageVideo> {
        val players = IFRAME.findAll(input = html).mapNotNull { tag ->
            val source = valueOf(
                tag = tag.value,
                attribute = SOURCE
            ) ?: return@mapNotNull null

            val id = YouTubeLinks.idOf(url = addressOf(source = source)) ?: return@mapNotNull null

            PageVideo(
                id = id,
                title = valueOf(
                    tag = tag.value,
                    attribute = TITLE
                )
            )
        }

        val lite = LITE.findAll(input = html).mapNotNull { tag ->
            val id = valueOf(
                tag = tag.value,
                attribute = VIDEO_ID
            ) ?: return@mapNotNull null

            PageVideo(
                id = id,
                title = valueOf(
                    tag = tag.value,
                    attribute = PLAY_LABEL
                )
            )
        }

        return (players + lite)
            .distinctBy { it.id }
            .take(n = MAX_VIDEOS)
            .toList()
    }

    // "//www.youtube.com/embed/…" as a page writes it, whole.
    private fun addressOf(source: String): String =
        if (source.startsWith(prefix = "//")) "https:$source" else source

    private fun valueOf(
        tag: String,
        attribute: Regex
    ): String? = attribute
        .find(input = tag)
        ?.groupValues
        ?.drop(n = 1)
        ?.firstOrNull { it.isNotEmpty() }
        ?.replace("&amp;", "&")
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
}
