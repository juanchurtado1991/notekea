package app.notekea.sync.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** The code review of 2026-11-13: an address written with its token was kept, shown and logged with it. */
class RemoteAddressTest {
    @Test
    fun `a web address gives up the token written in it, any other address is kept as it is`() {
        val addresses = listOf(
            "https://ghp_secret@github.com/me/notes.git",
            "https://me:ghp_secret@github.com/me/notes.git",
            " https://github.com/me/notes.git ",
            "ssh://git@github.com/me/notes.git",
            "git@github.com:me/notes.git"
        )

        assertEquals(
            expected = listOf(
                RemoteAddress(
                    url = "https://github.com/me/notes.git",
                    secret = "ghp_secret"
                ),
                RemoteAddress(
                    url = "https://github.com/me/notes.git",
                    secret = "ghp_secret"
                ),
                RemoteAddress(
                    url = "https://github.com/me/notes.git",
                    secret = null
                ),
                RemoteAddress(
                    url = "ssh://git@github.com/me/notes.git",
                    secret = null
                ),
                RemoteAddress(
                    url = "git@github.com:me/notes.git",
                    secret = null
                )
            ),
            actual = addresses.map { RemoteAddress.of(written = it) }
        )
    }
}
