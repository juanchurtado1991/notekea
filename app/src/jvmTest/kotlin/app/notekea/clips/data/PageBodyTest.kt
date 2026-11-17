package app.notekea.clips.data

import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.test.runTest
import java.nio.charset.Charset
import kotlin.test.Test
import kotlin.test.assertEquals

/** A page's body as text, read from its stream with no network (the bug hunt of 2026-11-17, E5 and E11). */
class PageBodyTest {
    private suspend fun read(
        bytes: ByteArray,
        declaredCharset: Charset? = null,
        maxBytes: Int = MAX
    ): String? = PageBody.read(
        channel = ByteReadChannel(bytes),
        declaredLength = null,
        declaredCharset = declaredCharset,
        maxBytes = maxBytes
    )

    // E5: the cap was checked after `get` had the whole body in memory. A length declared over it is refused before
    // reading, and a stream over it stops there, without waiting for its end (this one never ends).
    @Test
    fun `a body over the cap is refused by its declared length, or as soon as the stream passes it`() =
        runTest {
            val endless = ByteChannel(autoFlush = true)
            endless.writeFully(ByteArray(size = MAX + 1))

            val declared = PageBody.read(
                channel = ByteReadChannel(ByteArray(size = 0)),
                declaredLength = MAX + 1L,
                declaredCharset = null,
                maxBytes = MAX
            )

            val streamed = PageBody.read(
                channel = endless,
                declaredLength = null,
                declaredCharset = null,
                maxBytes = MAX
            )

            assertEquals(
                expected = listOf(null, null, "x".repeat(n = MAX)),
                actual = listOf(
                    declared,
                    streamed,
                    read(bytes = "x".repeat(n = MAX).toByteArray())
                )
            )
        }

    // E11: with no charset in the HTTP header, an older site's Latin-1 page lost its accents to UTF-8.
    @Test
    fun `with no charset in the header the page's meta names it, the header wins, UTF-8 is the default`() =
        runTest {
            val meta = "<meta charset=\"iso-8859-1\"><p>café</p>"

            val equiv =
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=windows-1252\">ñ"

            assertEquals(
                expected = listOf(meta, equiv, "<p>café</p>", "<p>café</p>"),
                actual = listOf(
                    read(
                        bytes = meta.toByteArray(charset = Charsets.ISO_8859_1),
                        maxBytes = PAGE
                    ),
                    read(
                        bytes = equiv.toByteArray(charset = Charsets.ISO_8859_1),
                        maxBytes = PAGE
                    ),
                    read(
                        bytes = "<p>café</p>".toByteArray(charset = Charsets.ISO_8859_1),
                        declaredCharset = Charsets.ISO_8859_1
                    ),
                    read(bytes = "<p>café</p>".toByteArray(charset = Charsets.UTF_8))
                )
            )
        }

    private companion object {
        const val MAX = 16
        const val PAGE = 1_024
    }
}
