package app.notekea.core.notes.domain

/**
 * A YouTube video's address in its forms (`watch?v=`, `youtu.be/`, `shorts/`, `embed/`, `live/`, on `m.` and
 * `music.` too) and the addresses Notekea makes of its id: the player without tracking cookies and the page.
 */
object YouTubeLinks {
    private val ADDRESS = Regex(
        pattern = "^https?://(?:www\\.|m\\.|music\\.)?(?:youtube\\.com|youtube-nocookie\\.com|youtu\\.be)/(.*)$",
        option = RegexOption.IGNORE_CASE
    )
    private val WATCH = Regex(pattern = "^watch\\?(?:.*&)?v=([A-Za-z0-9_-]{11})(?:[&#].*)?$")
    private val PATH =
        Regex(pattern = "^(?:(?:shorts|embed|live|v)/)?([A-Za-z0-9_-]{11})(?:[?&#/].*)?$")

    /** The video's id in [url] ("dQw4w9WgXcQ"), or null when it is not a YouTube video's address. */
    fun idOf(url: String): String? {
        val rest = ADDRESS
            .matchEntire(input = url.trim())
            ?.groupValues
            ?.get(index = 1) ?: return null

        return (WATCH.matchEntire(input = rest) ?: PATH.matchEntire(input = rest))
            ?.groupValues
            ?.get(index = 1)
    }

    /** YouTube's own player for the video [id], without its tracking cookies, starting at once. */
    fun playerOf(id: String): String = "https://www.youtube-nocookie.com/embed/$id?autoplay=1"

    /** The video's page on YouTube. */
    fun pageOf(id: String): String = "https://www.youtube.com/watch?v=$id"
}
