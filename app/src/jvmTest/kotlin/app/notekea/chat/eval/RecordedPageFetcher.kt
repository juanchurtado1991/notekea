package app.notekea.chat.eval

import app.notekea.clips.domain.ClipError
import app.notekea.clips.domain.FetchedPage
import app.notekea.clips.domain.PageFetcher
import app.notekea.core.domain.Outcome
import java.io.File
import java.security.MessageDigest

/**
 * The web as the evaluation of `docs/agent-web.md` sees it: each address downloaded once by [real] and kept in
 * [folder], so the three models read the same pages and a later run reads none again. Kept out of the repository:
 * the pages are other people's text. With [isReplayOnly], an address not kept fails as offline.
 */
internal class RecordedPageFetcher(
    private val real: PageFetcher,
    private val folder: File,
    private val isReplayOnly: Boolean = false
) : PageFetcher {
    override suspend fun fetch(url: String): Outcome<FetchedPage, ClipError> {
        val kept = File(folder, "${keyOf(url = url)}.page")

        if (kept.isFile) {
            val text = kept.readText()

            return Outcome.Success(
                value = FetchedPage(
                    url = text.substringBefore(delimiter = '\n'),
                    html = text.substringAfter(delimiter = '\n')
                )
            )
        }

        if (isReplayOnly) return Outcome.Failure(error = ClipError.Offline)

        val fetched = real.fetch(url = url)

        if (fetched is Outcome.Success) {
            folder.mkdirs()
            kept.writeText(text = "${fetched.value.url}\n${fetched.value.html}")
        }

        return fetched
    }

    private fun keyOf(url: String): String = MessageDigest
        .getInstance("SHA-256")
        .digest(url.toByteArray())
        .joinToString(separator = "") { "%02x".format(it) }
        .take(n = KEY_CHARS)

    private companion object {
        const val KEY_CHARS = 32
    }
}
