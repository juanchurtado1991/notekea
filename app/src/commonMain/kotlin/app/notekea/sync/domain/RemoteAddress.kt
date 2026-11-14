package app.notekea.sync.domain

/**
 * A remote's address apart from the token it may be written with ("https://ghp_x@github.com/me/notes"): the [url] is
 * kept and shown, the [secret] goes to the keychain. Kept whole, it showed the token in Settings and in errors
 * (2026-11-13). Only a web address carries one: "ssh://git@host" names a user.
 */
data class RemoteAddress(
    val url: String,
    val secret: String?
) {
    companion object {
        private val WITH_SECRET = Regex(
            pattern = "^(https?://)([^@/\\s]+)@(.+)$",
            option = RegexOption.IGNORE_CASE
        )

        fun of(written: String): RemoteAddress {
            val trimmed = written.trim()

            val match = WITH_SECRET.matchEntire(input = trimmed) ?: return RemoteAddress(
                url = trimmed,
                secret = null
            )

            val (scheme, user, rest) = match.destructured

            return RemoteAddress(
                url = scheme + rest,
                // "user:token" keeps the token; a token alone is written as the user.
                secret = user.substringAfter(delimiter = ':').takeIf { it.isNotBlank() }
            )
        }
    }
}
