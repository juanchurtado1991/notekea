package app.notekea.editor.domain

/** [YouTubeDetails] in memory: the titles and pictures YouTube would give, and the ids asked for. */
class FakeYouTubeDetails(
    val titles: MutableMap<String, String> = mutableMapOf(),
    val pictures: MutableMap<String, ByteArray> = mutableMapOf()
) : YouTubeDetails {
    val fetched = mutableListOf<String>()
    val read = mutableListOf<String>()

    override suspend fun fetch(id: String): String? {
        fetched += id

        return titles[id]
    }

    override suspend fun thumbnail(id: String): ByteArray? {
        read += id

        return pictures[id]
    }
}
