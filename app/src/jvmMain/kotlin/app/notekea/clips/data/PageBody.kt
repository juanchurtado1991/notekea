package app.notekea.clips.data

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

/**
 * A page's body as text, read from its stream (the bug hunt of 2026-11-17, E5 and E11): never more than a cap kept in
 * memory, and in the charset the response declares, else the one the page's own `<meta>` names, else UTF-8.
 */
internal object PageBody {
    /** The text of [channel], or null when it is over [maxBytes] or [declaredLength] says it will be. */
    suspend fun read(
        channel: ByteReadChannel,
        maxBytes: Int,
        declaredLength: Long?,
        declaredCharset: Charset?
    ): String? {
        val isTooLong = declaredLength != null && declaredLength > maxBytes
        if (isTooLong) return null

        val bytes = ByteArrayOutputStream()
        val buffer = ByteArray(size = BUFFER)

        while (!channel.isClosedForRead) {
            val read = channel.readAvailable(buffer)
            if (read < 0) break

            bytes.write(buffer, 0, read)

            // Stop as soon as the cap is passed: the rest of a large page never reaches memory.
            if (bytes.size() > maxBytes) return null
        }

        val content = bytes.toByteArray()
        val charset = declaredCharset ?: sniffed(bytes = content) ?: Charsets.UTF_8

        return String(content, charset)
    }

    // The charset a page names in its first bytes: `<meta charset="iso-8859-1">` or the http-equiv Content-Type, which
    // an older site's Latin-1 page often has with no charset in its HTTP header.
    private fun sniffed(bytes: ByteArray): Charset? {
        val head = String(
            bytes,
            0,
            minOf(bytes.size, SNIFF_BYTES),
            Charsets.ISO_8859_1
        )

        val name = META_CHARSET
            .find(input = head)
            ?.groupValues
            ?.get(index = 1)
            ?: return null

        return runCatching { Charset.forName(name) }.getOrNull()
    }

    private const val BUFFER = 64 * 1024
    private const val SNIFF_BYTES = 1024

    private val META_CHARSET = Regex(
        pattern = "<meta[^>]*?charset\\s*=\\s*[\"']?([A-Za-z0-9_.:-]+)",
        option = RegexOption.IGNORE_CASE
    )
}
